package de.andycandy.state_dsl

import org.junit.Test

import de.andycandy.state_dsl.StateMachine.StateImpl
import spock.lang.Specification

class ProtectionUtilSpec extends Specification {

	@Test
	def "test write to not protected state object"() {
		setup:
		State state = new StateImpl()
		
		when:
		state.name = 'hallo'
		
		then:
		state.name == 'hallo'	
	}
	
	@Test
	def "test write to protected state object"() {
		setup:
		State state = new StateImpl()
		
		when:
		state = ProtectionUtil.protectObject(State, state)
		state.name = 'hallo'
		
		then:
		final ReadOnlyPropertyException exception = thrown()
	}
	
	@Test
	def "test read from protected state object"() {
		setup:
		State state = new StateImpl()
		
		when:
		state.name = 'hallo'
		state = ProtectionUtil.protectObject(State, state)
		
		then:
		state.name == 'hallo'
	}
}