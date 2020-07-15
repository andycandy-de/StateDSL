package de.andycandy.state_dsl

import de.andycandy.state_dsl.SimpleStateMachine.StateBuildHelper
import groovy.transform.CompileStatic
import groovy.transform.NamedParam
import groovy.transform.NamedParams

@CompileStatic
class StateDSL {
	
	private Map<String, StateBuildHelper> states = [:]
	
	private String initState = null;
	
	static class state {
		
		static StateMachine machine(@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
			stateMachine(closure)
		}
		
		static StateMachine machine(
			@NamedParams([@NamedParam(value='ignoreUnkownInput', type=Boolean)]) Map params,
			@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
			stateMachine(params, closure)
		}
	}
	
	static StateMachine stateMachine(@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
		return stateMachine([:], closure)
	}
	
	static StateMachine stateMachine(
		@NamedParams([@NamedParam(value='ignoreUnkownInput', type=Boolean)]) Map params,
		@DelegatesTo(value=StateDSL, strategy=Closure.DELEGATE_ONLY) Closure closure) {
		
		StateDSL stateDSL = new StateDSL()
		
		closure.delegate = stateDSL
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.call()
		
		boolean ignoreUnkownInput = Boolean.TRUE.equals(params['ignoreUnkownInput'])
		
		return SimpleStateMachine.createStateMachine(stateDSL.states.values(), stateDSL.initState, ignoreUnkownInput);
	}
	
	void state(String name, @DelegatesTo(value=StateDelegate, strategy=Closure.DELEGATE_ONLY) Closure closure) {
		
		if (!states.containsKey(name)) {
			states[name] = new StateBuildHelper()
		}
		StateBuildHelper state = states[name]
		state.name = name
		
		StateDelegate stateDelegate = new StateDelegate(state)
		
		closure.delegate = stateDelegate
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.call()		
	}
	
	InitState state(String name) {
		return new InitState(name)
	}
	
	class InitState {
		
		String name
		
		InitState(name) {
			this.name = name
		}
		
		void init(@DelegatesTo(value=StateDelegate, strategy=Closure.DELEGATE_ONLY) Closure closure) {
			if (initState != null && initState != name) {
				throw new IllegalArgumentException("Just one state can be an init state!")
			}
			
			if (!states.containsKey(name)) {
				states[name] = new StateBuildHelper()
			}
			initState = name
			
			state(name, closure)
		}
	}
	
	class StateDelegate {
		
		StateBuildHelper state
		
		private StateDelegate(StateBuildHelper state) {
			this.state = state
		}
		
		To add(String transition) {

			if (state.transitions[transition] != null) {
				throw new IllegalArgumentException("Duplicate transitions!")
			}

			return new To(transition)
		}
		
		void enter(@DelegatesTo(strategy = Closure.OWNER_ONLY) Closure closure) {
			
			closure.resolveStrategy = Closure.OWNER_ONLY
			
			state.enter << closure
		}
		
		void leave(@DelegatesTo(strategy = Closure.OWNER_ONLY) Closure closure) {
			
			closure.resolveStrategy = Closure.OWNER_ONLY
			
			state.leave << closure
		}
		
		class To {
			
			String transition
			
			private To(String transition) {
				this.transition = transition
			}
			
			void to (String followState) {
				state.transitions[transition] = followState
			}
		}
	}
}
