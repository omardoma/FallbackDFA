package states;

import transtitions.Transition;

import java.util.ArrayList;

public class State {
    private String name;
    private ArrayList<Transition> transitions;

    public State(String name) {
        this.name = name;
        this.transitions = new ArrayList<>();
    }

    public State(String name, ArrayList<Transition> transitions) {
        this.name = name;
        this.transitions = transitions;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Transition> getTransitions() {
        return this.transitions;
    }

    public void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
    }

    public Transition getTransition(String input) {
        for (Transition transition : this.transitions) {
            if (transition.getInput().equals(input)) {
                return transition;
            }
        }
        return null;
    }

    public boolean hasTransition(String input) {
        for (Transition transition : this.transitions) {
            if (transition.getInput().equals(input)) {
                return true;
            }
        }
        return false;
    }

    public void addTransition(String name, State nextState) {
        this.getTransitions().add(new Transition(name, nextState));
    }

    public String toString() {
        return this.name;
    }
}
