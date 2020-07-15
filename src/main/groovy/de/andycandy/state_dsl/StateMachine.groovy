package de.andycandy.state_dsl


import groovy.transform.CompileStatic

@CompileStatic
interface StateMachine {

	State input(String input)
	
	State getInitState()
	
	State getCurrentState()
	
	List<State> getStates()
	
	Map<String, State> getTransitions()

}
