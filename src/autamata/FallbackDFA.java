package autamata;

import states.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;

public class FallbackDFA {
    private DFA dfa;
    private Map<String, String> actions;

    public FallbackDFA(DFA dfa, Map<String, String> actions) {
        this.dfa = dfa;
        this.actions = actions;
    }

    public DFA getDfa() {
        return dfa;
    }

    public Map<String, String> getActions() {
        return actions;
    }

    private String getTokensHelper(String inputTape) {
        if (inputTape.length() <= 0) {
            return "";
        }
        int L = 0;
        Stack<State> stack = new Stack<>();
        ArrayList<String> inputCharacters = new ArrayList<>(Arrays.asList(inputTape.split(",")));
        State currentState = this.dfa.getStartState();
        stack.push(currentState);
        L++;
        for (String character : inputCharacters) {
            currentState = currentState.getTransition(character).getNextState();
            if (currentState.getName().equals(NFA.REJECT_STATE)) {
                break;
            }
            stack.push(currentState);
            L++;
        }
        if (this.dfa.getAcceptStates().contains(currentState.getName())) {
            return "<" + this.actions.get(currentState.getName()) + ",\"" + inputTape + "\">";
        }
        StringBuilder tokens = new StringBuilder();
        State peek;
        while (!stack.empty()) {
            peek = stack.pop();
            L--;
            if (L >= 0 && !stack.empty() && this.dfa.getAcceptStates().contains(peek.getName())) {
                return tokens
                        .append("<")
                        .append(this.actions.get(peek.getName()))
                        .append(",\"").append(String.join(",", inputCharacters.subList(0, L)))
                        .append("\">").append(getTokensHelper(String.join(",", inputCharacters.subList(L, inputCharacters.size())))).toString();
            }
        }
        return tokens.append("<Error,\"").append(inputTape).append("\">").toString();
    }

    public String getTokens(String inputTape) {
        return getTokensHelper(inputTape);
    }
}
