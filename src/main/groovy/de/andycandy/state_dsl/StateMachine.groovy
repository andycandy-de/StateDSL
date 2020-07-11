package de.andycandy.state_dsl

class StateMachine {
	
	State initState
	
	State currentState
	
	List<State> states = []
	
	public State transition(String transition) {
		currentState.leave?.call()
		currentState = currentState.transitions[transition]
		currentState.enter?.call()
		return currentState
	}
	
	void setInitState(State state) {
		this.initState = state
		initState?.enter?.call()
	}
	
	@Override
	public String toString() {
		
		String string = "initState: $initState"\
			+ "\ncurrentState: $currentState"\
			+ "\nstates: $states"
			
		return string;
	}
	
}
