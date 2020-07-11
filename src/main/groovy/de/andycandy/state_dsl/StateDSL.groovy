package de.andycandy.state_dsl

import java.security.PrivateKey

import org.codehaus.groovy.util.StringUtil

import groovy.json.StringEscapeUtils

class StateDSL {
	
	private Map<String, State> states = [:]
	
	private Map<String, Map<String, String>> statesTransitionToState = [:]
	
	private State initState = null;
	
	static class state {
		static StateMachine machine(@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
			stateMachine(closure)
		}
	}
	
	static StateMachine stateMachine(@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
		
		StateDSL stateDSL = new StateDSL()
		
		closure.delegate = stateDSL
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.call()
		
		return StateDSL.buildMachine(stateDSL)	
	}
	
	void state(String name, @DelegatesTo(value=StateDelegate, strategy=Closure.DELEGATE_ONLY) Closure closure) {
		
		if (!states.containsKey(name)) {
			states[name] = new State()
		}
		State state = states.get(name)
		state.name = name
		
		StateDelegate stateDelegate = new StateDelegate()
		stateDelegate.state = state
		
		closure.delegate = stateDelegate
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.call()		
	}
	
	InitState state(String name) {
		return new InitState(name)
	}
	
	class InitState {
		
		InitState(name) {
			this.name = name
		}
		
		String name
		
		void init(@DelegatesTo(value=StateDelegate, strategy=Closure.DELEGATE_ONLY) Closure closure) {
			if (initState != null && initState != states.get(name)) {
				throw new IllegalArgumentException("Just one state can be an init state!")
			}
			
			if (!states.containsKey(name)) {
				states[name] = new State()
			}
			initState = states.get(name)
			
			state(name, closure)
		}
	}
	
	private static StateMachine buildMachine(StateDSL stateDSL) {
		
		StateMachine stateMachine = new StateMachine()
		stateDSL.states.each {
			State state = it.value
			stateMachine.states << state
			stateDSL.statesTransitionToState.get(it.key)?.each {
				State followState = stateDSL.states.get(it.value)
				if (followState == null) {
					throw new IllegalStateException("Following state not defined!")
				}
				state.transitions[it.key] = followState
			}
		}
		stateMachine.initState = stateDSL.initState
		stateMachine.currentState = stateDSL.initState
		
		verifyMachine(stateMachine)
		
		return stateMachine;
	}
	
	private static verifyMachine(StateMachine stateMachine) {
		
		if (stateMachine.initState == null) {
			throw new IllegalStateException('No init state defined!')
		}
		
		Set<State> passed = []
		verifyMachine(stateMachine.initState, passed)
		
		Set<State> notPassed = [] + stateMachine.states
		notPassed.removeAll(passed);
		
		if (!notPassed.isEmpty()) {
			String joined = String.join(", ", notPassed.collect { it.name }.toListString());
			throw new IllegalStateException("State machine contains unattainable states: [${joined}]")
		}
	}
	
	private static verifyMachine(State state, Set<State> passed) {
		if (passed.contains(state)) {
			return
		}
		
		passed.add(state)
		state.transitions.each { verifyMachine(it.value, passed) }
	}
	
	private class StateDelegate {
		
		State state
		
		Map<String, Closure> add(String transition) {

			if (statesTransitionToState.containsKey(state) && statesTransitionToState.get(state).containsKey(transition)) {
				throw new IllegalArgumentException("Duplicate transitions!")
			}

			if (!statesTransitionToState.containsKey(state)) {
				statesTransitionToState[state.name] = [:]
			}
			return [to : {followState -> statesTransitionToState[state.name][transition] = followState}]
		}
		
		void enter(Closure closure) {
			state.enter = closure
		}
		
		void leave(Closure closure) {
			state.leave = closure
		}
	}
	
}
