package de.andycandy.state_dsl

import spock.lang.Specification
import static de.andycandy.state_dsl.StateDSL.stateMachine
import de.andycandy.state_dsl.StateDSL.state

class StateDSLTest extends Specification {

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
		State curr = stateMachine.transition('transition')

		then:
		stateMachine != null
		curr.name == 'anyState'
		leaveMyState
		enterAnyState
	}
	
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
		stateMachine.transition('transition')
		State curr = stateMachine.transition('transitionBack')

		then:
		stateMachine != null
		curr.name == 'myState'
		leaveMyState
		enterAnyState
		leaveAnyState
		enterMyState
	}
	
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
		stateMachine.transition('transition')

		then:
		final IllegalStateException exception = thrown()
	}
	
	
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
		final IllegalStateException exception = thrown()
	}
	
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
		stateMachine.transition('off')
		stateMachine.transition('on')
		stateMachine.transition('emergencyOff')
		State curr = stateMachine.transition('normal')
		
		then:
		stateMachine != null
		curr.name == 'OffState'
		
		enterOff == 3
		leaveOff == 2
		enterOn == 1
		leaveOn == 1
		enterEmergencyOff == 1
		leaveEmergencyOff == 1
	}
}
