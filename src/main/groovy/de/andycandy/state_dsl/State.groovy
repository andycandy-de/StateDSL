package de.andycandy.state_dsl

class State {
	
	String name
	
	Map<String, State> transitions = [:]
	
	Closure enter
	
	Closure leave
	
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
