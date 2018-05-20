package autamata;

import exceptions.InvalidInputException;
import states.State;
import transtitions.Transition;

import java.util.LinkedHashSet;
import java.util.Set;

public class DFA extends Autamaton {

    public DFA(Set<State> states, Set<String> acceptStates, State startState, Set<String> alphabet) throws Exception {
        super(states, new LinkedHashSet<>(acceptStates), startState, alphabet);
    }

    public void validate() throws Exception {
        // Accept States Validation
        String invalidAcceptState = null;
        for (String acceptState : this.getAcceptStates()) {
            if (this.getStates().stream().noneMatch(state -> acceptState.equals(state.getName())) && !acceptState.trim().equals("")) {
                invalidAcceptState = acceptState;
                break;
            }
        }
        if (invalidAcceptState != null) {
            throw new Exception("Invalid accept states " + invalidAcceptState);
        }

        // Start State Validations
        if (!this.getStates().contains(this.getStartState())) {
            throw new Exception("Invalid start state");
        }

        // Missing Transitions Validations
        String wrongInputValue = null;
        String nextState = null;
        for (State state : this.getStates()) {
            if (state.getTransitions().size() == this.getAlphabet().size()) {
                // Invalid Transitions Validations
                for (Transition transition : state.getTransitions()) {
                    if (!this.inAlphabet(transition.getInput())) {
                        wrongInputValue = transition.getInput();
                        nextState = transition.getNextState().getName();
                        break;
                    }
                }
                if (!(wrongInputValue == null && nextState == null)) {
                    throw new Exception(
                            "Invalid transition " + state.getName() + "," + nextState + "," + wrongInputValue + " input " + wrongInputValue + " is not in the alphabet");
                }
            } else {
                throw new Exception("Missing transition for states " + state.getName());
            }
        }
    }

    public boolean testString(String inputTape) throws InvalidInputException {
        State currentState = this.getStartState();
        String[] inputs = inputTape.split(",");
        for (String head : inputs) {
            if (!this.inAlphabet(head)) {
                throw new InvalidInputException("Invalid input string at " + head);
            }
            currentState = currentState.getTransition(head).getNextState();
        }
        return this.getAcceptStates().contains(currentState.getName());
    }
}
