package automaton

import graph.Graph
import graph.Path

data class FiniteStateMachine<STATE, SYMBOL>(
        val nfaStart: Set<STATE>,
        val nfa: Graph<Set<STATE>, SYMBOL?>,
        val groups: Map<String, Group<STATE>>,
        val epsilonPath: (Path<Set<STATE>, SYMBOL?>) -> Boolean = { path -> defaultEpsilonPath(path) },
        val merge: (MutableMap<SYMBOL, Set<STATE>>, Path<Set<STATE>, SYMBOL?>) -> Unit = { results, path ->
          defaultMerge(results, path)
        }
) : Automaton<STATE, SYMBOL> {

  // Deterministic Finite Automaton
  val dfaStart: Set<STATE>
  val dfa: Graph<Set<STATE>, SYMBOL>

  init {
    val epsilonClosure: (Set<STATE>) -> Set<STATE> = { starts ->
      nfa.closure(
              starts.map { setOf(it) }.toSet(),
              { path -> if (epsilonPath(path)) path.end.map { setOf(it) }.toSet() else setOf() }
      ).flatten().toSet()
    }
    val nonEpsilonPaths: (Set<STATE>) -> Sequence<Path<Set<STATE>, SYMBOL?>> = { start ->
      nfa.paths(start.map { setOf(it) }.toSet(), { path -> !epsilonPath(path) })
    }

    val newDfa = Graph.New<Set<STATE>, SYMBOL>()
    dfaStart = epsilonClosure(nfaStart)
    val queue = mutableListOf(dfaStart)
    while (queue.isNotEmpty()) {
      val starts = queue.removeAt(0)
      if (starts in newDfa.vertices)
        continue

      val reachable: MutableMap<SYMBOL, Set<STATE>> = mutableMapOf()
      for (path in nonEpsilonPaths(epsilonClosure(starts)))
        merge(reachable, Path(path.start, path.edge, epsilonClosure(path.end)))

      for (ends in reachable.values)
        queue.add(ends)

      newDfa[starts] = reachable
    }
    dfa = newDfa.build()
  }

  override fun transitionFunction(state: Set<STATE>, inputs: InputTape<SYMBOL>): Set<STATE>? {
    val input = inputs.next() ?: return null
    return dfa.vertices[state]?.get(input)
  }

  fun evaluate(inputs: Sequence<SYMBOL>): Map<String, Match>? {
    return evaluate(dfaStart, InputTape(inputs), groups)
  }

  fun evaluateAll(inputs: Sequence<SYMBOL>): Sequence<Map<String, Match>> {
    return evaluateAll(dfaStart, InputTape(inputs), groups)
  }

  data class New<STATE, SYMBOL>(val initialState: STATE) {
    val nfa = Graph.New<Set<STATE>, SYMBOL?>()
    val groups: MutableMap<String, Group<STATE>> = mutableMapOf()
    var epsilonPath: (Path<Set<STATE>, SYMBOL?>) -> Boolean = { path -> defaultEpsilonPath(path) }
    var merge: (MutableMap<SYMBOL, Set<STATE>>, Path<Set<STATE>, SYMBOL?>) -> Unit = { results, path ->
      defaultMerge(results, path)
    }

    init {
      nfa.vertices[setOf(initialState)] = mapOf()
    }

    fun path(start: STATE, input: SYMBOL?, end: STATE) = apply {
      val starts = setOf(start)
      val ends = nfa.vertices[starts]?.get(input).orEmpty() union setOf(end)
      nfa.path(starts, input, ends)
    }

    fun group(name: String, start: STATE, end: STATE) = apply {
      groups[name] = Group(start, end)
    }

    fun withEpsilonTransition(function: (Path<Set<STATE>, SYMBOL?>) -> Boolean) = apply {
      epsilonPath = function
    }

    fun withMerge(function: (MutableMap<SYMBOL, Set<STATE>>, Path<Set<STATE>, SYMBOL?>) -> Unit) = apply {
      merge = function
    }

    fun build() = FiniteStateMachine(
            setOf(initialState),
            nfa.build(),
            groups,
            epsilonPath,
            merge
    )
  }
}
