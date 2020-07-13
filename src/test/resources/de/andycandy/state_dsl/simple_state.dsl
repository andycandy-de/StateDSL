
def result = []

def stateMachine = state.machine {
	state('InitState') init {
		add 'next' to 'NextState'
		enter { result << 'enter InitState' }
		leave { result << 'leave InitState' }
	}
	state('NextState') {
		add 'back' to 'InitState'
		enter { result << 'enter NextState' }
		leave { result << 'leave NextState' }
	}
}

stateMachine.input 'next'
stateMachine.input 'back'

result //should be: ['enter InitState', 'leave InitState', 'enter NextState', 'leave NextState', 'enter InitState']