package de.andycandy.state_dsl


import groovy.transform.CompileStatic

@CompileStatic
class SimpleStateMachine implements StateMachine {

	State initState

	State currentState

	List<State> states = []

	private boolean ignoreUnkownInput = false

	public State input(String input) {
		if (currentState.transitions[input] == null) {
			if (ignoreUnkownInput) {
				return currentState
			}
			throw new IllegalArgumentException("The input '$input' is not defined!")
		}

		if (currentState == currentState.transitions[input]) {
			return currentState
		}

		currentState.leave?.each {it?.call()}
		currentState = currentState.transitions[input]
		currentState.enter?.each {it?.call()}

		return currentState
	}

	private void setInitState(State state) {
		this.initState = state
		this.currentState = state
		initState.enter?.each {it?.call()}
	}

	public Map<String, State> getTransitions() {
		return currentState.transitions
	}

	@Override
	public String toString() {

		String string = "initState: ${initState.name} currentState: ${currentState.name}"\
			+ "\nstates: ${states.collect{ it.name }.toListString()}"

		return string;
	}

	static StateMachine createStateMachine(Collection<StateBuildHelper> stateBuildHelpers, String initState, boolean ignoreUnkownInput = false) {

		if (initState == null) {
			throw new IllegalArgumentException("Init state cannot be null!")
		}

		Map<String, StateImpl> stateImplMap = createStateImplMap(stateBuildHelpers)
		Map<String, State> protectedStateMap = createProtectedStateMap(stateImplMap)

		stateBuildHelpers.each {			
			Map<String, State> transitions = createTransitionMap(it, protectedStateMap)
			stateImplMap[it.name].transitions = Collections.unmodifiableMap(transitions)
		}

		SimpleStateMachine stateMachine = new SimpleStateMachine()
		stateMachine.states = Collections.unmodifiableList(protectedStateMap.values().toList())
		stateMachine.initState = protectedStateMap[initState]
		stateMachine.ignoreUnkownInput = ignoreUnkownInput

		verifyMachine(stateMachine)

		return ProtectionUtil.protectObject(StateMachine, stateMachine)
	}
	
	private static Map<String, State> createTransitionMap(StateBuildHelper stateBuildHelper, Map<String, State> protectedStateMap) {
		
		Map<String, State> transitions = [:]
		
		stateBuildHelper.transitions.each {
			
			transitions[it.key] = protectedStateMap[it.value]
			if (transitions[it.key] == null) {
				throw new IllegalStateException("Following state '${it.value}' not defined!")
			}
		}
		
		return transitions
	}
	
	private static Map<String, StateImpl> createStateImplMap(Collection<StateBuildHelper> stateBuildHelpers) {
		
		Map<String, StateImpl> stateMap = [:]
		
		stateBuildHelpers.each {
			State state = new StateImpl()
			state.name = it.name
			state.enter = it.enter
			state.leave = it.leave
			stateMap[it.name] = state
		}
		
		return stateMap
	}
	
	private static Map<String, State> createProtectedStateMap(Map<String, StateImpl> stateImplMap) {
		
		Map<String, State> protectedStateMap = [:]
		
		stateImplMap.each {
			protectedStateMap[it.key] = ProtectionUtil.protectObject(State, it.value)
		}
		
		protectedStateMap
	}

	private static verifyMachine(StateMachine stateMachine) {

		Set<State> passed = []
		verifyMachine(stateMachine.initState, passed)

		Set<State> notPassed = new HashSet(stateMachine.states)
		notPassed.removeAll(passed);

		if (!notPassed.isEmpty()) {
			String joined = notPassed.collect { it.name }.toListString()
			throw new IllegalStateException("State machine contains unattainable states: ${joined}")
		}
	}

	private static verifyMachine(State state, Set<State> passed) {
		if (passed.contains(state)) {
			return
		}

		passed.add(state)
		state.transitions.each { verifyMachine(it.value, passed) }
	}

	@CompileStatic
	static class StateImpl extends State {

		String name

		Map<String, State> transitions

		List<Closure> enter

		List<Closure> leave

		@Override
		public String toString() {
			String transitionString = ' empty'
			if (!transitions.isEmpty()) {
				transitionString = ''
				transitions.each { transitionString += "\n  ${it.key} -> ${it.value.name}" }
			}
			return "State: ${name}\nTransitions:$transitionString"
		}
	}

	@CompileStatic
	static class StateBuildHelper {

		String name

		Map<String, String> transitions = [:]

		List<Closure> enter = []

		List<Closure> leave = []
	}
}
