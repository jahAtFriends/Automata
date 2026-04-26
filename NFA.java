
/*
*
* Copyright (c) 2026 Joel Hammer
* Friends School of Baltimore
*
*
* Released under MIT License
* 
*
*
* */
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Non-Deterministic Finite Automaton
 */
public class NFA<S, A> {

    private final Set<S> states;
    private final Set<A> alphabet;
    private final Map<S, Map<A, Set<S>>> delta;
    private final Map<S, Set<S>> epsilonTransitions;
    private final S startState;
    private final Set<S> acceptStates;

    public NFA(Set<S> states,
            Set<A> alphabet,
            Map<S, Map<A, Set<S>>> delta,
            Map<S, Set<S>> epsilonTransitions,
            S startState,
            Set<S> acceptStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.delta = delta;
        this.epsilonTransitions = epsilonTransitions;
        this.startState = startState;
        this.acceptStates = acceptStates;
    }

    /* Getters for Basic Properties */
    public Set<S> getStates() {
        return states;
    }

    public Set<A> getAlphabet() {
        return alphabet;
    }

    public S getStartState() {
        return startState;
    }

    public Set<S> getAcceptStates() {
        return acceptStates;
    }

    /**
     * Compute possible states resulting from consuming the given symbol at the
     * given state.
     */
    public Set<S> transition(S state, A symbol) {
        return delta
                .getOrDefault(state, Collections.emptyMap())
                .getOrDefault(symbol, Collections.emptySet());
    }

    /**
     * Compute the possible states resulting from an epsilon transition at the
     * given state.
     */
    public Set<S> epsilonTransition(S state) {
        return epsilonTransitions.getOrDefault(state, Collections.emptySet());
    }

    /**
     * Returns the epsilon closure of a given state
     */
    public Set<S> epsilonClosure(S state) {
        Set<S> closure = new HashSet<>();
        Stack<S> stack = new Stack<>();

        closure.add(state);
        stack.push(state);

        while (!stack.isEmpty()) {
            S currentState = stack.pop();

            for (S next : epsilonTransition(currentState)) {
                if (closure.contains(next)) {
                    continue;
                }

                closure.add(next);
                stack.push(next);
            }
        }

        return closure;
    }

    /**
     * Return the epsilon closure of a set of states
     */
    public Set<S> epsilonClosure(Set<S> states) {
        Set<S> closure = new HashSet<>();

        for (S state : states) {
            closure.addAll(epsilonClosure(state));
        }

        return closure;
    }

    /**
     * Generate the clone-states resulting from consuming a given symbol from a
     * given set of clone-states.
     */
    public Set<S> move(Set<S> states, A symbol) {
        Set<S> result = new HashSet<>();

        for (S state : states) {
            result.addAll(transition(state, symbol));
        }

        return result;
    }

    /** Subset construction algorithm to simulate NFA with DFA */
    public DFA<Set<S>, A> toDFA() {
        final Set<S> DEAD = Collections.emptySet();

        Set<S> dfaStart = new HashSet<>(epsilonClosure(this.startState));
        if (dfaStart.isEmpty()) {
            dfaStart = DEAD;
        }

        Set<Set<S>> dfaStates = new HashSet<>();
        dfaStates.add(dfaStart);

        Map<Set<S>, Map<A, Set<S>>> dfaDelta = new HashMap<>();

        Stack<Set<S>> worklist = new Stack<>();
        worklist.push(dfaStart);

        Set<Set<S>> visited = new HashSet<>();

        while (!worklist.isEmpty()) {
            Set<S> t = new HashSet<>(worklist.pop());

            if (visited.contains(t)) {
                continue;
            }

            visited.add(t);

            Map<A, Set<S>> currentDelta = new HashMap<>();
            for (A a : alphabet) {
                Set<S> u = new HashSet<>(epsilonClosure(move(t, a)));

                if (u.isEmpty()) {
                    u = DEAD;
                }

                currentDelta.put(a, u);

                if (!dfaStates.contains(u)) {
                    dfaStates.add(u);
                    worklist.add(u);
                }
            }
            dfaDelta.put(t, currentDelta);
        }

        Set<Set<S>> dfaAcceptStates = new HashSet<>();
        for (Set<S> state : dfaStates) {
            for (S nfaState : state) {
                if (this.acceptStates.contains(nfaState)) {
                    dfaAcceptStates.add(state);
                    break;
                }
            }
        }

        return new DFA<>(dfaStates, alphabet, dfaDelta, dfaStart, dfaAcceptStates, DEAD);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (S state : this.states) {
            sb.append(state).append(": ");
            Map<A, Set<S>> currentMap = delta.getOrDefault(state, new HashMap<>());
            for (A symbol : alphabet) {
                Set<S> destinations = currentMap.getOrDefault(symbol, new HashSet<>());
                sb.append(symbol).append(" -> ").append(destinations).append(",    ");
            }
            Set<S> epTrans = this.epsilonTransition(state);
            sb.append("eps -> ").append(epTrans);
            sb.append("\n");
        }
        return sb.toString();
    }

    /** Utility class to for easier design/construction of an NFA */
    public static class NFABuilder<S, A> {

        private final Set<S> states;
        private final Set<A> alphabet;
        private final Map<S, Map<A, Set<S>>> delta;
        private final Map<S, Set<S>> epsilonTransitions;
        private S startState;
        private final Set<S> acceptStates;

        public NFABuilder(Set<A> alphabet) {
            states = new HashSet<>();
            this.alphabet = alphabet;
            delta = new HashMap<>();
            epsilonTransitions = new HashMap<>();
            startState = null;
            acceptStates = new HashSet<>();
        }

        /** Add an NFA state */
        public void addState(S state) {
            this.states.add(state);
        }

        /** Add all of the given states to the set of states */
        public void addStates(Collection<S> states) {
            this.states.addAll(states);
        }

        /** Set the given state as the start state */
        public void setStartState(S start) {
            this.startState = start;
        }

        /** Add the given transition to the builder */
        public void addTransition(S from, A symbol, S to) {
            addTransition(from, symbol, Set.of(to));
        }

        /** Add all transitions to the configuration set from the given state */
        public void addTransition(S from, A symbol, Collection<S> to) {
            Map<A, Set<S>> transitions = delta.getOrDefault(from, new HashMap<>());
            Set<S> trans = transitions.getOrDefault(symbol, new HashSet<>());
            trans.addAll(to);
            transitions.put(symbol, trans);
            delta.put(from, transitions);
        }

        /** Add an epsilon transition between the given nodes */
        public void addEpsilonTransition(S from, S to) {
            addEpsilonTransition(from, Set.of(to));
        }

        /** Add epsilon transitions from a source to multiple destinations */
        public void addEpsilonTransition(S from, Collection<S> to) {
            Set<S> epDestinations = this.epsilonTransitions.getOrDefault(from, new HashSet<>());
            epDestinations.addAll(to);
            this.epsilonTransitions.put(from, epDestinations);
        }

        /** Set the given state as an accept state */
        public void setStateAsAccept(S state) {
            this.acceptStates.add(state);
        }

        /** Add all the states to the set of accept states */
        public void setStatesAsAccept(Collection<S> states) {
            this.acceptStates.addAll(states);
        }

        /** Return the built NFA */
        public NFA<S, A> toNFA() {
            NFA<S,A> n = new NFA<>(this.states, this.alphabet, this.delta, this.epsilonTransitions, this.startState, this.acceptStates);
            //System.out.println(n.states);
            return n;
        }
    }

    public static void main(String[] args) {
        Set<Character> alpha = new HashSet<>(3);
        alpha.add('a');
        alpha.add('b');
        alpha.add('c');

        NFABuilder<Integer, Character> bld = new NFABuilder<>(alpha);
        bld.addStates(Set.of(0,1,2,3));
        bld.addTransition(1, 'a', 2);
        bld.addEpsilonTransition(0, 1);
        bld.addEpsilonTransition(0, 3);
        bld.addEpsilonTransition(2, 3);
        bld.addEpsilonTransition(2, 1);

        bld.setStartState(0);
        bld.setStateAsAccept(3);

        NFA<Integer, Character> nfa = bld.toNFA();

        System.out.println(nfa);

        System.out.println("\n\n");

        DFA<Set<Integer>, Character> dfa = nfa.toDFA();
        System.out.println(dfa);

        System.out.println(dfa.denumerate());
    }
}
