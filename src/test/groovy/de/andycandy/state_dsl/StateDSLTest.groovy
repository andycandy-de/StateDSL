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
	
	def "test dsl emergency"() {
		setup:
		int enterOff = 0
		int leaveOff = 0
		int enterOn = 0
		int leaveOn = 0
		int enterEmergency = 0
		int leaveEmergency = 0
		
		when:
		StateMachine stateMachine = state.machine {
			state('OffState') init {
				add 'on' to 'OnState'
				add 'emergency' to 'EmergencyState'
				add 'off' to 'OffState'
				enter { ++enterOff }
				leave { ++leaveOff}
			}
			state('OnState') {
				add 'on' to 'OnState'
				add 'emergency' to 'EmergencyState'
				add 'off' to 'OffState'
				enter { ++enterOn }
				leave { ++leaveOn}
			}
			state('EmergencyState') {
				add 'on' to 'EmergencyState'
				add 'normal' to 'OffState'
				add 'off' to 'EmergencyState'
				enter { ++enterEmergency }
				leave { ++leaveEmergency}
			}
		}
		stateMachine.transition('off')
		stateMachine.transition('on')
		State curr = stateMachine.transition('emergency')
		
		then:
		stateMachine != null
		curr.name == 'EmergencyState'
		
		enterOff == 2
		leaveOff == 2
		enterOn == 1
		leaveOn == 1
		enterEmergency == 1
		leaveEmergency == 0
	}
}
