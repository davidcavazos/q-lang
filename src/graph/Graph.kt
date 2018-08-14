package graph

import kotlin.coroutines.experimental.buildSequence

data class Graph<VERTEX, EDGE>(
        val vertices: Map<VERTEX, Map<EDGE, VERTEX>>
) {

  fun paths(
          starts: Set<VERTEX>,
          filter: (Path<VERTEX, EDGE>) -> Boolean = { true },
          orderVertices: (Set<VERTEX>) -> Sequence<VERTEX> = { it.asSequence() },
          orderEdges: (Set<EDGE>) -> Sequence<EDGE> = { it.asSequence() }
  ) = buildSequence {

    for (start in orderVertices(starts)) {
      for (edge in orderEdges(vertices[start]?.keys!!)) {
        val end = vertices[start]?.get(edge)!!
        val path = Path(start, edge, end)
        if (filter(path))
          yield(path)
      }
    }
  }

  fun paths(
          filter: (Path<VERTEX, EDGE>) -> Boolean = { true },
          orderVertices: (Set<VERTEX>) -> Sequence<VERTEX> = { it.asSequence() },
          orderEdges: (Set<EDGE>) -> Sequence<EDGE> = { it.asSequence() }
  ) = paths(vertices.keys, filter, orderVertices, orderEdges)

  fun breadthFirstSearch(
          starts: Set<VERTEX>,
          filter: (Path<VERTEX, EDGE>) -> Boolean = { true },
          orderVertices: (Set<VERTEX>) -> Sequence<VERTEX> = { it.asSequence() },
          orderEdges: (Set<EDGE>) -> Sequence<EDGE> = { it.asSequence() }
  ) = buildSequence {

    val seen = starts.toMutableSet()
    val queue = orderVertices(starts).toMutableList()
    while (queue.isNotEmpty()) {
      val start = queue.removeAt(0)  // queue.remove()
      for (edge in orderEdges(vertices[start]?.keys!!)) {
        val end = vertices[start]?.get(edge)!!
        if (end !in seen) {
          seen.add(end)
          queue.add(end)
          val path = Path(start, edge, end)
          if (filter(path))
            yield(path)
        }
      }
    }
  }

  fun depthFirstSearch(
          starts: Set<VERTEX>,
          filter: (Path<VERTEX, EDGE>) -> Boolean = { true },
          orderVertices: (Set<VERTEX>) -> Sequence<VERTEX> = { it.asSequence() },
          orderEdges: (Set<EDGE>) -> Sequence<EDGE> = { it.asSequence() }
  ) = buildSequence {

    val seen = starts.toMutableSet()
    val stack = orderVertices(starts).toMutableList()
    val results = mutableListOf<Path<VERTEX, EDGE>>()
    while (stack.isNotEmpty()) {
      val start = stack.removeAt(stack.count() - 1)  // stack.pop()
      if (results.isNotEmpty())
        yield(results.removeAt(results.count() - 1))  // results.pop()

      for (edge in orderEdges(vertices[start]?.keys!!)) {
        val end = vertices[start]?.get(edge)!!
        if (end !in seen) {
          seen.add(end)
          stack.add(end)
          val path = Path(start, edge, end)
          if (filter(path))
            yield(path)
        }
      }
    }
  }

  fun closure(
          starts: Set<VERTEX>,
          reachable: (Path<VERTEX, EDGE>) -> Set<VERTEX> = { path -> setOf(path.end) }
  ): Set<VERTEX> {
    val seen = starts.toMutableSet()
    val stack = starts.toMutableList()
    while (stack.isNotEmpty()) {
      val start = stack.removeAt(stack.count() - 1)  // stack.pop()
      for ((edge, rawEnd) in vertices[start]!!) {
        for (end in reachable(Path(start, edge, rawEnd))) {
          if (end !in seen) {
            seen.add(end)
            stack.add(end)
          }
        }
      }
    }
    return seen
  }

  override fun toString(): String {
    var s = "${this::class} {\n"
    for ((start, subpaths) in vertices) {
      if (subpaths.isNotEmpty()) {
        for ((edge, end) in subpaths)
          s += "  $start -$edge-> $end\n"
      } else
        s += "  $start\n"
    }
    s += "}\n"
    return s
  }

  class New<VERTEX, EDGE> {
    val vertices: MutableMap<VERTEX, Map<EDGE, VERTEX>> = mutableMapOf()

    operator fun set(origin: VERTEX, destinations: Map<EDGE, VERTEX>) {
      vertices[origin] = destinations
    }

    operator fun get(vertex: VERTEX): Map<EDGE, VERTEX>? {
      return vertices[vertex]
    }

    fun path(start: VERTEX, edge: EDGE, end: VERTEX) = apply {
      path(Path(start, edge, end))
    }

    fun path(path: Path<VERTEX, EDGE>) = apply {
      // TODO: think of a clearer syntax for this
      // Just like `object.field.subfield = value` is possible, this should also be possible:
      //   `vertices[start][edge] = vertices[start][edge] or {} union {end}
      val edges = vertices[path.start].orEmpty().toMutableMap()
      edges[path.edge] = path.end
      vertices[path.start] = edges
      vertices[path.end] = vertices[path.end].orEmpty()
    }

    fun build() = Graph(vertices)
  }
}
