package autamata;

import states.State;
import transtitions.Transition;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class NFA extends Autamaton {
    public static final String EPSILON = "$";
    public static final String REJECT_STATE = "Dead";

    public NFA(Set<State> states, Set<String> acceptStates, State startState, Set<String> alphabet) throws Exception {
        super(states, acceptStates, startState, alphabet);
    }

    public void validate() throws Exception {
        StringBuilder err = new StringBuilder();

        // Accept States Validation
        boolean found;
        for (String acceptState : this.getAcceptStates()) {
            found = false;
            for (State state : this.getStates()) {
                if (acceptState.equals(state.getName()) && !acceptState.trim().equals("")) {
                    found = true;
                }
            }
            if (!found) {
                err.append("\nInvalid accept state ").append(acceptState);
            }
        }

        // Start State Validations
        if (!this.getStates().contains(this.getStartState())) {
            err.append("\nInvalid start state");
        }

        // Invalid Transitions Validations
        for (State state : this.getStates()) {
            for (Transition transition : state.getTransitions()) {
                if (!(this.inAlphabet(transition.getInput()) || transition.getInput().equals(EPSILON))) {
                    err.append("\nInvalid transition ").append(state.getName()).append(",").append(transition.getNextState()).append(",").append(transition.getInput()).append(" input ").append(transition.getInput()).append(" is not in the alphabet");
                }
            }
        }
        if (!err.toString().equals("")) {
            throw new Exception(err.toString());
        }
    }

    private State findState(String name, List<State> states) {
        for (State state : states) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }

    private String getSetStateName(Set<State> states) {
        ArrayList<String> statesNamesList = new ArrayList<>();
        for (State state : states) {
            statesNamesList.add(state.getName());
        }
        statesNamesList.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join("*", statesNamesList);
    }

    private Set<State> epsilonClosureHelper(int i, State state, Set<State> epsilonStates) {
        if (!epsilonStates.contains(state)) {
            epsilonStates.add(state);
        }
        if (i == state.getTransitions().size()) {
            return epsilonStates;
        }
        Transition transition = state.getTransitions().get(i);
        if (transition.getInput().equals(EPSILON) && !epsilonStates.contains(transition.getNextState())) {
            epsilonStates.addAll(epsilonClosureHelper(0, transition.getNextState(), epsilonStates));
        }
        epsilonStates.addAll(epsilonClosureHelper(i + 1, state, epsilonStates));
        return epsilonStates;
    }

    private Set<State> epsilonClosure(State state) {
        return epsilonClosureHelper(0, state, new LinkedHashSet<>());
    }

    private Set<State> moveHelper(int i, State state, String inputCharacter, Set<State> reachableStates) {
        if (!state.hasTransition(inputCharacter) || i == state.getTransitions().size()) {
            return reachableStates;
        }
        Transition transition = state.getTransitions().get(i);
        if (transition.getInput().equals(inputCharacter) && !reachableStates.contains(transition.getNextState())) {
            reachableStates.add(transition.getNextState());
        }
        reachableStates.addAll(moveHelper(i + 1, state, inputCharacter, reachableStates));
        return reachableStates;
    }

    private Set<State> move(Set<State> newStateSet, String inputCharacter) {
        Set<State> reachableStates = new LinkedHashSet<>();
        for (State state : newStateSet) {
            reachableStates.addAll(moveHelper(0, state, inputCharacter, reachableStates));
        }
        return reachableStates;
    }

    private boolean isStateSetVisited
            (Set<State> statesSet, Map<Set<State>, Map<String, Set<State>>> visitedDFAStates) {
        for (Map.Entry<Set<State>, Map<String, Set<State>>> entry : visitedDFAStates.entrySet()) {
            if (statesSet.equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    private Map<Set<State>, Map<String, Set<State>>> subsetConstruction() {
        Set<Set<State>> dfaStatesSet = new CopyOnWriteArraySet<>();
        Map<Set<State>, Map<String, Set<State>>> visitedDFAStates = new LinkedHashMap<>();
        Set<String> doneAlpha = new HashSet<>();

        // Initialize a reject states set
        Set<State> rejectSet = new HashSet<>();
        State reject = new State(REJECT_STATE);
        for (String character : this.getAlphabet()) {
            reject.addTransition(character, reject);
        }
        rejectSet.add(reject);

        // Get DFA start states and start the algorithm with it
        dfaStatesSet.add(epsilonClosure(this.getStartState()));

        //Get the rest of the DFA states
        Map<String, Set<State>> tmpMap;
        Set<State> tmpSet;
        while (dfaStatesSet.size() > 0) {
            for (Set<State> currentSet : dfaStatesSet) {
                tmpMap = new LinkedHashMap<>();
                for (String inputCharacter : this.getAlphabet()) {
                    if (!doneAlpha.contains(inputCharacter)) {
                        tmpSet = new LinkedHashSet<>();
                        if (!currentSet.equals(rejectSet)) {
                            for (State state : move(currentSet, inputCharacter)) {
                                tmpSet.addAll(epsilonClosure(state));
                            }
                        }
                        // Handle the addition of a reject states
                        if (tmpSet.isEmpty()) {
                            tmpMap.put(inputCharacter, rejectSet);
                            if (!dfaStatesSet.contains(rejectSet)) {
                                dfaStatesSet.add(rejectSet);
                            }
                        } else {
                            tmpMap.put(inputCharacter, tmpSet);
                            if (!isStateSetVisited(tmpSet, visitedDFAStates)) {
                                dfaStatesSet.add(tmpSet);
                            }
                        }
                        doneAlpha.add(inputCharacter);
                    }
                }
                visitedDFAStates.put(currentSet, tmpMap);
                dfaStatesSet.remove(currentSet);
                doneAlpha.clear();
            }
        }
        return visitedDFAStates;
    }

    private List<State> getDFAStates(Map<Set<State>, Map<String, Set<State>>> constructedSets) {
        List<State> dfaStates = new ArrayList<>();
        State reject = null;
        String tmpStateName;
        for (Map.Entry<Set<State>, Map<String, Set<State>>> entry : constructedSets.entrySet()) {
            tmpStateName = getSetStateName(entry.getKey());
            dfaStates.add(new State(tmpStateName));
            if (tmpStateName.equals(REJECT_STATE)) {
                reject = dfaStates.get(dfaStates.size() - 1);
            }
        }
        State currentState, nextState;
        String currentStateName, nextStateName;
        for (Map.Entry<Set<State>, Map<String, Set<State>>> entry : constructedSets.entrySet()) {
            currentStateName = getSetStateName(entry.getKey());
            if (reject != null && currentStateName.equals(REJECT_STATE)) {
                currentState = reject;
            } else {
                currentState = findState(currentStateName, dfaStates);
            }
            if (currentState != null) {
                for (Map.Entry<String, Set<State>> subEntry : entry.getValue().entrySet()) {
                    nextStateName = getSetStateName(subEntry.getValue());
                    if (reject != null && nextStateName.equals(REJECT_STATE)) {
                        nextState = reject;
                    } else {
                        nextState = findState(nextStateName, dfaStates);
                    }
                    currentState.addTransition(subEntry.getKey(), nextState);
                }
            }
        }
        return dfaStates;
    }

    public DFA getEquivalentDFA() throws Exception {
        // Apply the subset construction algorithm to get the dfa states from nfa
        Map<Set<State>, Map<String, Set<State>>> constructedSets = subsetConstruction();

        // Get dfa states
        List<State> dfaStates = getDFAStates(constructedSets);

        // Make the reject state the last one if it exists
        State reject = findState(REJECT_STATE, dfaStates);
        if (reject != null) {
            dfaStates.remove(reject);
            dfaStates.add(reject);
        }

        // Set the dfa start states
        State dfaStartState = dfaStates.get(0);

        // Set the dfa accept states set
        List<String> dfaAcceptStates = new ArrayList<>();
        for (State state : dfaStates) {
            for (String acceptState : this.getAcceptStates()) {
                if (state.getName().contains(acceptState)) {
                    dfaAcceptStates.add(state.getName());
                    break;
                }
            }
        }
        dfaAcceptStates.sort(String.CASE_INSENSITIVE_ORDER);

        // Construct a new DFA object from the converted data
        return new DFA(new LinkedHashSet<>(dfaStates), new LinkedHashSet<>(dfaAcceptStates), dfaStartState, this.getAlphabet());
    }
}
