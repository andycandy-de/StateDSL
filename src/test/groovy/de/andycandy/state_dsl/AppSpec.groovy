package de.andycandy.state_dsl

import spock.lang.Specification

class AppSpec extends Specification {
	
	def "test app"() {
		setup:
		String path = Thread.currentThread().getContextClassLoader().getResource('de/andycandy/state_dsl/simple_state.dsl').path
		
		expect:
		App.main(path)
	}
	
	
	def "test app evaluate"() {
		setup:
		File file = new File(Thread.currentThread().getContextClassLoader().getResource('de/andycandy/state_dsl/simple_state.dsl').path)
		
		when:
		def result = App.evaluate(file)
		
		then:
		result == 'enter NextState;leave NextState;enter NextState;leave NextState;enter NextState;'
	}
}
