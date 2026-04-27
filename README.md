# Finite State Automata Tools

In this Java package, we provide some tools for working with Finite State Automata, particularly for use in lexical parsers. The following classes are available:

## DFA

Represents a Deterministic Finite-state Automaton. Automata of this class are always _total_, that is, every transition in the alphabet is provided for. Hence, one of the states _must_ be the dead state. Complete self loops back to the dead state are enforced.

This class also provides two key methods for manipulating the DFA:

1. `denumerate()` which converts the DFA from arbitrary node types to integer-labeled nodes (useful for DFAs produced by other algorithms)

2. `minimize()` which uses Hopcroft's Algorithm to construct a minimal DFA with an equivalent language.
