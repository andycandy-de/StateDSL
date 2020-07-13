package de.andycandy.state_dsl

import org.junit.Test

import de.andycandy.state_dsl.StateMachine.StateImpl
import spock.lang.Specification

class StateMachineSpec extends Specification {
	
	@Test
	def 'test state maschine'() {
		setup:
		boolean leave = false
		boolean enter = false
		StateMachine stateMachine = new StateMachine()
		
		StateImpl state1 = new StateImpl()
		state1.name = 'State1'
		state1.enter = []
		state1.leave = Arrays.asList({ leave = true })
		
		StateImpl state2 = new StateImpl()
		state2.name = 'State2'
		state2.enter = Arrays.asList({ enter = true })
		
		state1.transitions = ['trans' : state2]
		
		stateMachine.initState = state1
		stateMachine.currentState = state1
		
		when:
		State retState = stateMachine.input('trans')
		
		then:
		retState == state2
		stateMachine.currentState == state2
		stateMachine.initState == state1
		enter
		leave
	}
}
