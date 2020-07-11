package de.andycandy.state_dsl

import spock.lang.Specification

class StateTest extends Specification {
	
	def "Create a State"() {
		when:
		State state = new State()
		state.name = "Test"
		
		then:
		state.toString() == 'State: Test\nTransitions: empty'
	}
	
	def "Create a State with transition"() {
		when:
		State state = new State()
		state.name = "Test"
		
		State state2 = new State()
		state2.name = "Test2"
		
		state.transitions << ['trans' : state2]
		
		then:
		state.toString() == 'State: Test\nTransitions:\n  trans -> Test2'
	}
	
}
