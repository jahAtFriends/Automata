import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Deterministic Finite State Automaton.
 * This class represents a state machine with state labels of type S (probably)
 * integers and an alphabet of type A (usually characters)
 */
public class DFA<S, A> {
    private final Set<S> states;
    private final Set<A> alphabet;
    private final Map<S, Map<A, S>> delta;
    private final S startState;
    private final Set<S> acceptStates;
    private final S deadState;

    public DFA(Set<S> states,
            Set<A> alphabet,
            Map<S, Map<A, S>> delta,
            S startState,
            Set<S> acceptStates,
            S deadState) {
        
        this.states = states;
        this.alphabet = alphabet;
        this.delta = delta;
        this.startState = startState;
        this.acceptStates = acceptStates;
        this.deadState = deadState;

        completeTransitions();
    }

    private void completeTransitions() {

        // Add dead state with self loops
        Map<A, S> deadMap = new HashMap<>();
        for (A a : this.alphabet) {
            deadMap.put(a, this.deadState);
        }
        delta.put(deadState, deadMap);

        // Add transitions to dead state
        for (S state : states) {
            Map<A, S> currentMap = delta.getOrDefault(state, new HashMap<>());
            for (A a : alphabet) {
                S transitionState = currentMap.getOrDefault(a, deadState);
                currentMap.put(a, transitionState);
            }
            delta.put(state, currentMap);
        }
    }

    /*
     * Yields the state resulting from consuming the given symbol at the given
     * state.
     */
    public S transition(S state, A symbol) {
        return delta.get(state).get(symbol);
    }

    /** Is the given state an accepting state? */
    public boolean isAccepting(S state) {
        return acceptStates.contains(state);
    }

    public boolean accepts(Iterable<A> symbols) {
        S currentState = startState;

        for (A s : symbols) {
            currentState = transition(currentState, s);
        }

        return isAccepting(currentState);
    }

    public Set<S> acceptStates() {
        return new HashSet<>(this.acceptStates);
    }

    public Set<S> states() {
        return new HashSet<>(this.states);
    }

    /**
     * Returns the minimal DFA with the same language using Hopcroft's Algorithm.
     */
    public DFA<Set<S>, A> minimize() {
        Set<Set<S>> P = new HashSet<>();  // P = Set of partitions
        Queue<Set<S>> W = new LinkedList<>();  // W = Worklist
        
        // Initialize P = {Accepting states, Non-accepting}
        Set<S> accepting = this.acceptStates();
        Set<S> nonAccepting = this.states();
        nonAccepting.removeAll(accepting);
        P.add(accepting);
        P.add(nonAccepting);

        // Initialize Worklist = [{accepting}]
        W.offer(accepting);

        while (!W.isEmpty()) {
            Set<S> testSet = W.poll();

            for (A a : this.alphabet) {
                Set<S> X = getSourceStates(testSet, a);

                Queue<Set<S>> qP = new LinkedList<>(P);     // Freeze iteration set
                for (Set<S> Y : qP) {
                    Set<S> intersection = new HashSet<>(Y);
                    intersection.retainAll(X);

                    Set<S> difference = new HashSet<>(Y);
                    difference.removeAll(X);

                    // If the test set splits Y...
                    if (!intersection.isEmpty() && !difference.isEmpty()) {
                        
                        // Replace Y with the split set
                        P.remove(Y);
                        P.add(intersection);
                        P.add(difference);

                        if (W.remove(Y)) {
                            W.add(intersection);
                            W.add(difference);
                        } else {
                            if (intersection.size() <= difference.size()) {
                                W.add(intersection);
                            } else {
                                W.add(difference);
                            }
                        }
                    }
                }
            }
        }

        // Construct new delta function on minimized states.
        Map<Set<S>, Map<A, Set<S>>> newDelta = new HashMap<>();
        for (Set<S> p : P) {
            
            Map<A, Set<S>> pMap = new HashMap<>();
            S element = p.iterator().next(); // Arbitrary element of p
            
            for (A a : alphabet) {
                S destination = transition(element, a);
                for (Set<S> q : P) {
                    if (q.contains(destination)) {
                        pMap.put(a, q);
                        break;
                    }
                }
            }
            newDelta.put(p, pMap);
        }

        // Construct new Accept States
        Set<Set<S>> newAcceptStates = new HashSet<>();
        for (Set<S> p : P) {
            S element = p.iterator().next();
            if (this.acceptStates().contains(element)) {
                newAcceptStates.add(p);
            }
        }

        // New Dead State
        Set<S> newDeadState = null;
        for (Set<S> p : P) {
            if (p.contains(this.deadState)) {
                newDeadState = p;
            }
        }

        // Identify new Start State
        Set<S> newStartState = null;
        for (Set<S> p : P) {
            if (p.contains(this.startState)) {
                newStartState = p;
                break;
            }
        }

        assert newAcceptStates != null && newStartState != null && newDeadState != null;

        return new DFA<>(P, this.alphabet, newDelta, newStartState, newAcceptStates, newDeadState);
    }

    /** Get the set of states in the DFA that send to some state in
     *  a given destination set via a given transition symbol.
     */
    private Set<S> getSourceStates(Set<S> destinationSet, A transition) {
        Set<S> result = new HashSet<>();

        for (S state : this.states) {
            Map<A, S> currentMap = this.delta.getOrDefault(state, new HashMap<>());

            if (destinationSet.contains(currentMap.get(transition))) {
                result.add(state);
            }
        }

        return result;
    }

    /** Returns an equivalent DFA where the states are integers */
    public DFA<Integer, A> denumerate() {
        Map<S, Integer> dictionary = new HashMap<>();

        dictionary.put(startState, 0);
        dictionary.put(deadState, -1);

        int counter = 1;
        for (S state : states) {
            if ((state == startState) || (state == deadState)) {
                continue;
            }
            dictionary.put(state, counter++);
        }

        Map<Integer, Map<A, Integer>> newDelta = new HashMap<>();
        for (S state : states) {
            Map<A, Integer> currentMap = new HashMap<>();
            Map<A, S> oldMap = delta.get(state);

            for (A a : alphabet) {
                currentMap.put(a, dictionary.get(oldMap.get(a)));
            }

            newDelta.put(dictionary.get(state), currentMap);
        }

        Set<Integer> newAcceptStates = new HashSet<>();
        for (S state : acceptStates) {
            newAcceptStates.add(dictionary.get(state));
        }

        return new DFA<>(new HashSet<>(dictionary.values()), alphabet, newDelta, dictionary.get(startState), newAcceptStates, dictionary.get(deadState));

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (S state : states) {
            sb.append(state.toString()).append(":  ");
            Map<A, S> m = delta.get(state);
            for (A a : alphabet) {
                sb.append(a.toString()).append( " -> ").append(m.get(a)).append( ",  ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Set<Character> alphabet = Set.of('f', 'i', 'e');
        DFABuilder<Integer, Character> builder = new DFABuilder<>(alphabet);

        builder.addStates(Set.of(0, 1, 2, 3, 4, 5));
        builder.addDeadState(-1);
        builder.setStartState(0);
        builder.setStatesAsAccept(Set.of(3, 5));

        builder.addTransition(0, 'f', 1);
        builder.addTransition(1, 'e', 2);
        builder.addTransition(2, 'e', 3);
        builder.addTransition(1, 'i', 4);
        builder.addTransition(4, 'e', 5);

        DFA<Integer, Character> dfa = builder.toDFA();

        DFA<Set<Integer>, Character> mini = dfa.minimize();
        DFA<Integer, Character> miniNumbered = mini.denumerate();

        System.out.println(mini);
        System.out.println(miniNumbered);
        // System.out.println(miniNumbered.deadState);
    }
}