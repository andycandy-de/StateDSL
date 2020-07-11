# StateDSL

Einfache DSL um eine State-Maschine zu bauen.

```
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
```