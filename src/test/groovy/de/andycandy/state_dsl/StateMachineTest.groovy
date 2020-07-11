package de.andycandy.state_dsl

import spock.lang.Specification

class StateMachineTest extends Specification {
	
	def 'test state maschine'() {
		setup:
		boolean leave = false
		boolean enter = false
		StateMachine stateMachine = new StateMachine()
		
		State state1 = new State()
		state1.name = 'State1'
		state1.leave = { leave = true }
		
		State state2 = new State()
		state2.name = 'State2'
		state2.enter = { enter = true }
		
		state1.transitions << ['trans' : state2]
		
		stateMachine.initState = state1
		stateMachine.currentState = state1
		
		when:
		State retState = stateMachine.transition('trans')
		
		then:
		retState == state2
		stateMachine.currentState == state2
		stateMachine.initState == state1
		enter
		leave
	}
	
}
