package autamata;

import states.State;

import java.util.Set;

public abstract class Autamaton {
    private Set<State> states;
    private Set<String> acceptStates;
    private State startState;
    private Set<String> alphabet;

    public Autamaton(Set<State> states, Set<String> acceptStates, State startState, Set<String> alphabet) throws Exception {
        this.states = states;
        this.acceptStates = acceptStates;
        this.startState = startState;
        this.alphabet = alphabet;
        validate();
    }

    public Set<State> getStates() {
        return this.states;
    }

    public Set<String> getAcceptStates() {
        return this.acceptStates;
    }

    public State getStartState() {
        return this.startState;
    }

    public Set<String> getAlphabet() {
        return this.alphabet;
    }

    public abstract void validate() throws Exception;

    public boolean inAlphabet(String input) {
        return this.alphabet.contains(input);
    }
}
