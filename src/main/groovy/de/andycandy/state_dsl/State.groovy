package de.andycandy.state_dsl

import groovy.transform.CompileStatic

@CompileStatic
interface State {
	
	String getName();

	Map<String, State> getTransitions();
	
	List<Closure> getEnter();
	
	List<Closure> getLeave();
	
}
