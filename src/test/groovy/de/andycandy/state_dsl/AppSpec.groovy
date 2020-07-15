package de.andycandy.state_dsl

import org.junit.Test

import spock.lang.Specification

class AppSpec extends Specification {
	
	@Test
	def "test app"() {
		setup:
		String path = Thread.currentThread().getContextClassLoader().getResource('de/andycandy/state_dsl/simple_state.dsl').path
		
		expect:
		App.main(path)
	}
	
	@Test
	def "test app exit on usage"() {
		setup:
		def systemExit = false
		def error = ''
		App app = new App() {
			@Override
			public void systemExit() {
				systemExit = true
			}
			
			@Override
			public void printErr(String text) {
				error = text
			}
		}
		
		when:
		app.start(new String[0])
		
		then:
		systemExit
		error == 'Usage: StateDSL [file]'
	}
	
	@Test
	def "test app exit on no file"() {
		setup:
		def systemExit = false
		def error = ''
		App app = new App() {
			@Override
			public void systemExit() {
				systemExit = true
			}
			
			@Override
			public void printErr(String text) {
				error = text
			}
		}
		def file = new File('dafuq')
		
		when:
		app.start([file.absolutePath].toArray(new String[1]))
		
		then:
		systemExit
		error == "File ${file.absolutePath} not exists!"
	}
	
	@Test
	def "test app evaluate"() {
		setup:
		App app = new App()
		File file = new File(Thread.currentThread().getContextClassLoader().getResource('de/andycandy/state_dsl/simple_state.dsl').path)
		
		when:
		def result = app.evaluate(file)
		
		then:
		result == ['enter InitState', 'leave InitState', 'enter NextState', 'leave NextState', 'enter InitState']
	}
}
