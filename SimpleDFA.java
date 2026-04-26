import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleDFA  {

    private DFA<Integer, Character> dfa;

    private SimpleDFA(DFA<Integer, Character> dfa) {
        this.dfa = dfa;
    }

    public static SimpleDFABuilder builder(int size) {
        return new SimpleDFABuilder(size);
    }

    public static SimpleDFABuilder builder(int size, String alphabet) {
        return new SimpleDFABuilder(size, alphabet);
    }

    public static class SimpleDFABuilder {
        private DFABuilder<Integer, Character> builder;
        private final static String DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        SimpleDFABuilder(int numStates) {
            this(numStates, DEFAULT_ALPHABET);

        }

        SimpleDFABuilder(int numStates, String alphabet) {
            if (numStates <= 0) {
                throw new IllegalArgumentException("Number of states must be positive. Given: " + numStates);
            }
            Set<Character> alphaList = new HashSet<>(alphabet.length());
            for (int i = 0; i < alphabet.length(); i++) {
                alphaList.add(alphabet.charAt(i));
            }

            this.builder =  new DFABuilder<>(alphaList);
            for (int i = 0; i < numStates; i++) {
                builder.addState(i);
            }

            this.builder.addDeadState(-1);
            this.builder.setStartState(0);
        }

        public void addTransition(int from, char symbol, int to) {
            builder.addTransition(from, symbol, to);
        }

        public void setStateAsAccept(int state) {
            builder.setStateAsAccept(state);
        }

        public void setStatesAsAccept(Collection<Integer> states) {
            builder.setStatesAsAccept(states);
        }

        public SimpleDFA toSimpleDFA() {
            DFA<Integer, Character> dfa = builder.toDFA();
            return new SimpleDFA(dfa);
        }
    }

    public boolean accepts(String s) {
        ArrayList<Character> chars = new ArrayList<>(s.length());
        for (int i  = 0; i < s.length(); i++) {
            chars.add(s.charAt(i));
        }

        return dfa.accepts(chars);
    }

    @Override
    public String toString() {
        return dfa.toString();
    }

    public static void main(String[] args) {
        SimpleDFABuilder bldr = builder(3, "ab");
        bldr.addTransition(0, 'a', 1);
        bldr.addTransition(1, 'b', 1);
        bldr.addTransition(1, 'a', 2);

        bldr.setStateAsAccept(2);

        SimpleDFA dfa = bldr.toSimpleDFA();

        System.out.println(dfa.accepts("abbbbbbbbbbbbbbbbbbbbbbbbbbbbba"));
    }
}
