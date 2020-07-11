# StateDSL

Einfache DSL um eine State-Maschine zu bauen.

```
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
```