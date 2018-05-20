import autamata.DFA;
import autamata.FallbackDFA;
import autamata.NFA;
import states.State;
import transtitions.Transition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TestFallbackDFA {
    private static final int INPUT_DIVISIONS = 7;
    private static final String PRIMARY_INPUT_SPLIT = ",";
    private static final String SECONDARY_INPUT_SPLIT = "#";

    private static ArrayList<String> readInputFile(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        ArrayList<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        fr.close();
        br.close();
        return lines;
    }

    private static State findState(String name, Set<State> states) {
        for (State state : states) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }

    private static void printEquivalentDFA(DFA dfa, String inputs, Map<String, String> actionsMap) {
        StringBuilder result = new StringBuilder();

        // Print States
        for (State state : dfa.getStates()) {
            result.append(state.toString()).append(",");
        }
        System.out.println(result.substring(0, result.length() - 1));

        // Print Accept States
        result = new StringBuilder();
        for (String state : dfa.getAcceptStates()) {
            result.append(state).append(",");
        }
        System.out.print(result.toString().equals("") ? result.toString() : result.substring(0, result.length() - 1));

        // Print Actions
        System.out.println();
        result = new StringBuilder();
        for (Map.Entry<String, String> entry : actionsMap.entrySet()) {
            result.append(entry.getValue()).append(",");
        }
        System.out.println(result.toString());

        // Print Alphabet
        result = new StringBuilder();
        for (String alpha : dfa.getAlphabet()) {
            result.append(alpha).append(",");
        }
        System.out.println(result.substring(0, result.length() - 1));

        // Print start state
        System.out.println(dfa.getStartState());

        // Print transitions
        result = new StringBuilder();
        for (State state : dfa.getStates()) {
            for (Transition transition : state.getTransitions()) {
                result.append(state).append(",").append(transition).append("#");
            }
        }
        System.out.println(result.substring(0, result.length() - 1));

        // Print Inputs
        System.out.println(inputs);
    }

    private static void testInput(String[] input) {
        Set<State> states = new LinkedHashSet<>();
        for (String s : input[0].split(PRIMARY_INPUT_SPLIT)) {
            states.add(new State(s));
        }

        List<String> acceptStates = new ArrayList<>(Arrays.asList(input[1].split(PRIMARY_INPUT_SPLIT)));

        List<String> actions = new ArrayList<>(Arrays.asList(input[2].split(PRIMARY_INPUT_SPLIT)));

        Set<String> alphabet = new LinkedHashSet<>(Arrays.asList(input[3].split(PRIMARY_INPUT_SPLIT)));

        State startState = findState(input[4], states);
        startState = (startState != null ? startState : new State(""));

        String[] inputs = input[6].split(SECONDARY_INPUT_SPLIT);

        try {
            String[] currentTransitionArray;
            String[] transitionArray;
            String[] transitions = input[5].split(SECONDARY_INPUT_SPLIT);
            Set<String> addedStates = new HashSet<>();
            State currentState, nextState;
            for (String transition : transitions) {
                currentTransitionArray = transition.split(PRIMARY_INPUT_SPLIT);
                if (!addedStates.contains(currentTransitionArray[0])) {
                    currentState = findState(currentTransitionArray[0], states);
                    for (String innerTransition : transitions) {
                        transitionArray = innerTransition.split(PRIMARY_INPUT_SPLIT);
                        if (transitionArray[0].equals(currentTransitionArray[0])) {
                            nextState = findState(transitionArray[1], states);
                            currentState.addTransition(transitionArray[2], nextState);
                        }
                    }
                    addedStates.add(currentTransitionArray[0]);
                }
            }
            NFA nfa = new NFA(states, new HashSet<>(acceptStates), startState, alphabet);
            System.out.println("FBNFA Constructed");

            DFA dfa = nfa.getEquivalentDFA();
            System.out.println("Equivalent FBDFA:");

            // Map the accept states to actions
            Map<String, String> actionsMap = new HashMap<>();

            for (String acceptState : dfa.getAcceptStates()) {
                for (int i = 0; i < acceptStates.size(); i++) {
                    if (acceptState.contains(acceptStates.get(i))) {
                        actionsMap.put(acceptState, actions.get(i));
                        break;
                    }
                }
            }
            printEquivalentDFA(dfa, input[6], actionsMap);

            FallbackDFA fallbackDFA = new FallbackDFA(dfa, actionsMap);
            System.out.println("FBDFA constructed");
            for (String currentInput : inputs) {
                System.out.println(fallbackDFA.getTokens(currentInput));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("DFA Construction skipped and inputs are ignored");
        } finally {
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            ArrayList<String> lines = readInputFile("src/data/in0.in");
            String[] input = new String[INPUT_DIVISIONS];
            int count;
            for (int i = 0; i < lines.size(); i++) {
                count = (i % (INPUT_DIVISIONS + 1));
                if (count != INPUT_DIVISIONS) {
                    input[count] = lines.get(i);
                } else {
                    testInput(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
