package graph

import org.testng.annotations.Test
import kotlin.test.assertEquals

// WARNING: they way many of these tests work right now is that they depend on the hash map implementation for the
//          ordering.
// FIX: add a `sort (lhs T, rhs T) bool` function parameter to make the order of the traversal deterministic by

internal class GraphTest {
  val g = Graph.New<Char, Char>()
          .path('A', 'b', 'B')
          .path('A', 'c', 'C')
          .path('B', 'd', 'D')
          .path('C', 'd', 'D')
          .build()

  @Test
  fun paths() {
    val actual = g.paths(
            setOf('A'),
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'b', 'B'),
            Path('A', 'c', 'C')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun pathsFilter() {
    val actual = g.paths(
            setOf('A'),
            filter = { path -> path.edge != 'b' },
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'c', 'C')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun pathsAll() {
    val actual = g.paths(
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'b', 'B'),
            Path('A', 'c', 'C'),
            Path('B', 'd', 'D'),
            Path('C', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun pathsAllFilter() {
    val actual = g.paths(
            filter = { path -> path.edge != 'b' },
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'c', 'C'),
            Path('B', 'd', 'D'),
            Path('C', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun breadthFirstSearch() {
    val actual = g.breadthFirstSearch(
            setOf('A'),
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'b', 'B'),
            Path('A', 'c', 'C'),
            Path('B', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun breadthFirstSearchFilter() {
    val actual = g.breadthFirstSearch(
            setOf('A'),
            filter = { path -> path.edge != 'b' },
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'c', 'C'),
            Path('B', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun depthFirstSearch() {
    val actual = g.depthFirstSearch(
            setOf('A'),
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'b', 'B'),
            Path('A', 'c', 'C'),
            Path('C', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun depthFirstSearchFilter() {
    val actual = g.depthFirstSearch(
            setOf('A'),
            filter = { path -> path.edge != 'b' },
            orderVertices = { vertices: Set<Char> -> vertices.toSortedSet().asSequence() },
            orderEdges = { edges: Set<Char> -> edges.toSortedSet().asSequence() }
    ).toList()
    val expected = listOf(
            Path('A', 'c', 'C'),
            Path('C', 'd', 'D')
    )
    assertEquals(expected, actual)
  }

  @Test
  fun closure() {
    val actual = g.closure(setOf('A'))
    val expected = setOf('A', 'B', 'C', 'D')
    assertEquals(expected, actual)
  }

  @Test
  fun closureReachable() {
    val actual = g.closure(
            setOf('A'),
            reachable = { path -> if (path.edge != 'b') setOf(path.end) else setOf() }
    )
    val expected = setOf('A', 'C', 'D')
    assertEquals(expected, actual)
  }
}