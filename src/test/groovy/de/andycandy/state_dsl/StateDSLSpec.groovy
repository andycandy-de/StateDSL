package de.andycandy.state_dsl

import static de.andycandy.state_dsl.StateDSL.stateMachine

import org.junit.Test

import de.andycandy.state_dsl.StateDSL.state
import groovy.transform.CompileStatic
import de.andycandy.state_dsl.State
import spock.lang.Specification

class StateDSLSpec extends Specification {
	
	@Test
	def "test dsl with dot"() {
		setup:
		boolean leaveMyState = false
		boolean enterAnyState = false

		when:
		StateMachine stateMachine = state.machine {
			state('anyState') {
				add 'transition' to 'myState'
				enter {
					enterAnyState = true
				}
				leave {
				}
			}
			state('myState') init {
				add 'transition' to 'anyState'
				enter {
				}
				leave {
					leaveMyState = true
				}
			}
		}
		State curr = stateMachine.input('transition')

		then:
		stateMachine != null
		curr.name == 'anyState'
		leaveMyState
		enterAnyState
	}
	
	@Test
	def "test dsl camelCase"() {
		setup:
		boolean leaveMyState = false
		boolean enterAnyState = false
		boolean leaveAnyState = false
		boolean enterMyState = false

		when:
		StateMachine stateMachine = stateMachine {
			state('anyState') {
				add 'transitionBack' to 'myState'
				enter {
					enterAnyState = true
				}
				leave {
					leaveAnyState = true
				}
			}
			state('myState') init {
				add 'transition' to 'anyState'
				enter {
					enterMyState = true
				}
				leave {
					leaveMyState = true
				}
			}
		}
		stateMachine.input('transition')
		State curr = stateMachine.input('transitionBack')

		then:
		stateMachine != null
		curr.name == 'myState'
		leaveMyState
		enterAnyState
		leaveAnyState
		enterMyState
	}
	
	@Test
	def "test dsl unresolved state"() {
		when:
		StateMachine stateMachine = state.machine {
			state('anyState') {
				add 'transition' to 'myState'
				enter {
				}
				leave {
				}
			}
			state('myState') init {
				add 'transition' to 'anyState'
				enter {
				}
				leave {
				}
			}
			state('neverFound') {
			}
		}

		then:
		final IllegalStateException exception = thrown()
		exception.message == 'State machine contains unattainable states: [neverFound]'
	}
	
	@Test
	def "test dsl no init"() {
		when:
		StateMachine stateMachine = state.machine {
			state('anyState') {
				add 'transition' to 'myState'
				enter {
				}
				leave {
				}
			}
			state('myState') {
				add 'transition' to 'anyState'
				enter {
				}
				leave {
				}
			}	
		}

		then:
		final IllegalArgumentException exception = thrown()
		exception.message == 'Init state cannot be null!'
	}
	
	@Test
	def "test emergency off machine"() {
		setup:
		int enterOff = 0
		int leaveOff = 0
		int enterOn = 0
		int leaveOn = 0
		int enterEmergencyOff = 0
		int leaveEmergencyOff = 0
		
		when:
		StateMachine stateMachine = state.machine {
			state('OffState') init {
				add 'on' to 'OnState'
				add 'emergencyOff' to 'EmergencyOffState'
				add 'off' to 'OffState'
				enter { ++enterOff }
				leave { ++leaveOff}
			}
			state('OnState') {
				add 'on' to 'OnState'
				add 'emergencyOff' to 'EmergencyOffState'
				add 'off' to 'OffState'
				enter { ++enterOn }
				leave { ++leaveOn}
			}
			state('EmergencyOffState') {
				add 'on' to 'EmergencyOffState'
				add 'normal' to 'OffState'
				add 'off' to 'EmergencyOffState'
				enter { ++enterEmergencyOff }
				leave { ++leaveEmergencyOff}
			}
		}
		stateMachine.input('off')
		stateMachine.input('on')
		stateMachine.input('emergencyOff')
		State curr = stateMachine.input('normal')
		
		then:
		stateMachine != null
		curr.name == 'OffState'
		
		enterOff == 2
		leaveOff == 1
		enterOn == 1
		leaveOn == 1
		enterEmergencyOff == 1
		leaveEmergencyOff == 1
	}
	
	@Test
	def "test ignoreUnkownInput"() {		
		when:
		StateMachine stateMachine = state.machine(ignoreUnkownInput: true) {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		State curr = stateMachine.input('stuff')
		
		then:
		stateMachine != null
		curr.name == 'InitState'
	}
	
	@Test	
	def "test dont ignoreUnkownInput"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		stateMachine.input('stuff')
		
		then:
		final IllegalArgumentException exception = thrown()
		exception.message == 'The input \'stuff\' is not defined!'
	}
	
	@Test
	def "test following state not defined"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
				add 'notDefined' to 'NotDefinedState'
			}
		}
		stateMachine.input('stuff')
		
		then:
		final IllegalStateException exception = thrown()
		exception.message == 'Following state \'NotDefinedState\' not defined!'
	}
	
	@Test
	def "test init state"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
			
			state('InitState') init {}
		}
		
		then:
		stateMachine.initState.name == 'InitState'
	}
	
	@Test
	def "test duplicate transition exeption"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
				add 'other' to 'OtherState'
			}
			state('OtherState') {
				add 'init' to 'InitState'
			}
			
			state('InitState') init {
				add 'any' to 'AnyState'
			}
		}
		
		then:
		final IllegalArgumentException exception = thrown()
		exception.message == 'Duplicate transitions!'
	}
	
	@Test
	def "test duplicate init exeption"() {
		when:
		StateMachine stateMachine = state.machine {
			state('AnyState') init {
				add 'init' to 'InitState'
				add 'other' to 'OtherState'
			}
			state('OtherState') {
				add 'init' to 'InitState'
			}
			
			state('InitState') init {
				add 'any' to 'AnyState'
			}
		}
		
		then:
		final IllegalArgumentException exception = thrown()
		exception.message == 'Just one state can be an init state!'
	}
	
	@Test
	def "test duplicate init on same state"() {
		setup:
		boolean enterInit = false
		
		when:
		StateMachine stateMachine = state.machine {
			state('AnyState') {
				add 'init' to 'InitState'
				add 'other' to 'OtherState'
			}
			state('OtherState') {
				add 'init' to 'InitState'
			}
			
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('InitState') init {
				enter {
					enterInit = true
				}
			}
		}
		
		then:
		enterInit
	}
	
	@Test
	def "test multiple enter and leave"() {
		setup:
		String enterInit = ''
		String leaveInit = ''
		String enterAny = ''
		String leaveAny = ''
		
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
				enter { enterInit += 'A' }
				enter { enterInit += 'B' }
				leave { leaveInit += 'A' }
				leave { leaveInit += 'B' }
			}
			state('AnyState') {
				add 'init' to 'InitState'
				enter { enterAny += 'A' }
				enter { enterAny += 'B' }
				leave { leaveAny += 'A' }
				leave { leaveAny += 'B' }
			}
		}
		stateMachine.input 'any'
		stateMachine.input 'init'
		stateMachine.input 'any'
		
		then:
		enterInit == 'ABAB'
		leaveInit == 'ABAB'
		enterAny == 'ABAB'
		leaveAny == 'AB'
		stateMachine.transitions == ['init' : stateMachine.initState]
		stateMachine.currentState.name == 'AnyState'
	}
		
	@Test
	def "test modify state name"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		State currentState = stateMachine.currentState
		currentState.name = 'hallo'
		
		then:
		final ReadOnlyPropertyException exception = thrown()
	}
	
	@Test
	def "test modify state transition map"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		Map<String, State> transitions = stateMachine.transitions
		transitions['what'] = [:].asType(State)
		
		then:
		final UnsupportedOperationException exception = thrown()
	}
	
	@Test
	def "test modify state list"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		List<State> list = stateMachine.states
		list << [:].asType(State)
		
		then:
		final UnsupportedOperationException exception = thrown()
	}
	
	@Test
	def "test set state list"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		stateMachine.states = []
		
		then:
		final ReadOnlyPropertyException exception = thrown()
	}
		
	@Test
	def "test set current state"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		stateMachine.currentState = [:].asType(State)
		
		then:
		final ReadOnlyPropertyException exception = thrown()
	}
	
	@Test
	def "test set init state"() {
		when:
		StateMachine stateMachine = state.machine {
			state('InitState') init {
				add 'any' to 'AnyState'
			}
			state('AnyState') {
				add 'init' to 'InitState'
			}
		}
		
		stateMachine.initState = [:].asType(State)
		
		then:
		final ReadOnlyPropertyException exception = thrown()
	}
}
