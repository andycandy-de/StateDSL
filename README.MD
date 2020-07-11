# StateDSL

Einfache DSL um eine State-Maschine zu bauen.

```
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
```