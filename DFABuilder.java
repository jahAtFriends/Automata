import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFABuilder<S, A> {
    private final Set<S> states;
    private final Set<A> alphabet;
    private final Map<S, Map<A, S>> delta;
    private S startState;
    private final Set<S> acceptStates;
    private S deadState;

    public DFABuilder(Set<A> alphabet) {
        states = new HashSet<>();
        this.alphabet = alphabet;
        this.delta = new HashMap<>();
        this.startState = null;
        this.acceptStates = new HashSet<>();
        this.deadState = null;
    }

    public void addState(S state) {
        this.states.add(state);
    }

    public void addStates(Collection<S> states) {
        this.states.addAll(states);
    }

    public void addTransition(S from, A a, S to) {
        Map<A, S> map = delta.getOrDefault(from, new HashMap<>());
        map.put(a, to);
        delta.put(from, map);
    }

    public void addDeadState(S state) {
        this.deadState = state;

        states.add(deadState);
    }

    public void setStartState(S state) {
        this.startState = state;
    }

    public void setStateAsAccept(S state) {
        this.acceptStates.add(state);
    }

    public void setStatesAsAccept(Collection<S> states) {
        this.acceptStates.addAll(states);
    }

    public DFA<S, A> toDFA() {
        return new DFA<>(states, alphabet, delta, startState, acceptStates, deadState);
    }
}
