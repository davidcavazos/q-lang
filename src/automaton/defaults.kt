package automaton

import graph.Path

fun <STATE, SYMBOL> defaultEpsilonPath(path: Path<STATE, SYMBOL?>): Boolean = path.edge == null

fun <STATE, SYMBOL> defaultMerge(results: MutableMap<SYMBOL, Set<STATE>>, path: Path<Set<STATE>, SYMBOL?>) {
  if (path.edge == null)
    return
  results[path.edge] = results[path.edge].orEmpty() union path.end
}
