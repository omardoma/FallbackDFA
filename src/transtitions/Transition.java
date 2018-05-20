package transtitions;

import states.State;

public class Transition {
    private String input;
    private State nextState;

    public Transition(String input, State nextState) {
        this.input = input;
        this.nextState = nextState;
    }

    public String getInput() {
        return this.input;
    }

    public State getNextState() {
        return this.nextState;
    }

    public String toString() {
        return this.nextState + "," + this.input;
    }
}
