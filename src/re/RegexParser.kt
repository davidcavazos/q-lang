package re

import automaton.FiniteStateMachine

internal class RegexParser(val pattern: String) {

  var idx = 0
  var groupIndex = 1
  var fsm = FiniteStateMachine.New<Int, Char>(0)

  fun more(): Boolean {
    return idx < pattern.length
  }

  fun peek(offset: Int = 0): Char? {
    val i = idx + offset
    if (i < pattern.length)
      return pattern[i]
    return null
  }

  fun next(): Char? {
    val c = peek()
    idx++
    return c
  }

  fun accept(c: Char): Boolean {
    val result = c == peek()
    if (result)
      next()
    return result
  }

  fun expect(c: Char, error: Exception? = null) {
    val actual = peek()
    if (actual == null || actual != c) {
      throw error ?: SyntaxError(pattern, idx + 1, "expected '$c', got '${actual}' (${actual?.toInt()})")
    }
    next()
  }

  // TODO: make this function also create the transitionFunction so the New state is always created
  fun newState(): Int {
    return fsm.nfa.vertices.size
  }

  fun parse(): FiniteStateMachine<Int, Char> {
    if (pattern.isEmpty())
      throw IllegalArgumentException("pattern cannot be empty")
    idx = 0
    fsm = FiniteStateMachine.New(0)
    fsm.group(MAIN_GROUP, 0, expression(0))
    return fsm.build()
  }

  fun expression(start: Int): Int {
    val choices = mutableListOf<Int>()

    do {
      choices.add(sequence(start))
    } while (more() && accept('|'))

    if (choices.size == 1)
      return choices[0]

    val end = newState()
    for (choice in choices)
      fsm.path(choice, null, end)
    return end
  }

  fun sequence(start: Int): Int {
    var start = start
    var end = start
    while (peek() != null && peek()!! !in ")]|") {
      end = quantifier(start)
      start = end
    }
    return end
  }

  fun quantifier(start: Int): Int {
    var end = atom(start)
    if (more()) {
      when (peek()) {
        '?' -> {
          /*  .?
           *  start -index-> end
           *    +--------^
           */
          fsm.path(start, null, end)
          next()
        }

        '+' -> {
          /*  .+
           *  start -index-> end ---> New
           *    ^---------+
           */
          val new = newState()
          fsm.path(end, null, new)
          fsm.path(end, null, start)
          end = new
          next()
        }

        '*' -> {
          /*  .*
           *  start -index-> end ---> New
           *    \ ^-------+      /^
           *     +--------------+
           */
          val new = newState()
          fsm.path(end, null, new)
          fsm.path(end, null, start)
          fsm.path(start, null, new)
          end = new
          next()
        }

        '{' -> throw NotImplementedError("a{n}, a{n,}, a{n,m}")
      }
    }
    return end
  }

  fun atom(start: Int): Int {
    var end = start
    when (peek()) {
      '(' -> {
        val openParenthesisIdx = idx
        expect('(')
        end = capture_group(start)
        expect(')', UnmatchedOpeningParenthesisError(pattern, openParenthesisIdx))
      }

      '[' -> end = character_class(start)

      '^', '$', '.' -> throw NotImplementedError("atom ${peek()}")

      '?', '+', '*', ')', ']', '|' -> return end

      else -> end = character(start)
    }
    return end
  }

  fun character_class(start: Int): Int {
    val end = newState()
    expect('[')
    var c = peek()
    while (c != null && c != ']') {
      character(start, end)
      if (peek(0) == '-' && peek(1) != ']') {
        expect('-')
        val startChar = c.toInt() + 1
        val endChar = peek()!!.toInt()
        if (start > end)
          throw InvalidCharacterClassRange(pattern, idx)
        for (i in startChar..endChar)
          fsm.path(start, i.toChar(), end)
      }
      c = peek()
    }
    expect(']', UnmatchedOpeningSquareBracketError(pattern, idx))
    return end
  }

  fun character(start: Int, finalState: Int? = null): Int {
    val end = finalState ?: newState()
    when (peek()) {
      '\\' -> {
        expect('\\')

        when (peek()) {
          null -> throw DanglingBackslashError(pattern, idx - 1)

        // escape characters
          '0' -> fsm.path(start, 0.toChar(), end)
          'a' -> fsm.path(start, 0x07.toChar(), end)
          'e' -> fsm.path(start, 0x1a.toChar(), end)
          'f' -> fsm.path(start, 0x0c.toChar(), end)
          'n' -> fsm.path(start, '\n', end)
          'r' -> fsm.path(start, '\r', end)
          't' -> fsm.path(start, '\t', end)

        // line break
          'R' -> {
            fsm.path(start, '\r', end)
            fsm.path(start, '\n', end)
            val midState = newState()
            fsm.path(start, '\r', midState)
            fsm.path(midState, '\n', end)
          }

        // shorthands
          'd' -> {
            // [0-9]
            for (c in '0'..'9')
              fsm.path(start, c, end)
          }
          'w' -> {
            // [_a-zA-Z0-9]
            fsm.path(start, '_', end)
            for (c in 'a'..'z') {
              fsm.path(start, c, end)
              fsm.path(start, c.toUpperCase(), end)
            }
          }
          's' -> {
            // [ \t\r\n\f]
            fsm.path(start, ' ', end)
            fsm.path(start, '\t', end)
            fsm.path(start, '\r', end)
            fsm.path(start, '\n', end)
            fsm.path(start, 0x0b.toChar(), end)
            fsm.path(start, 0x0c.toChar(), end)
          }

          else -> fsm.path(start, peek(), end)
        }
      }

      else -> fsm.path(start, peek(), end)
    }
    next()
    return end
  }

  fun capture_group(start: Int): Int {
    val groupName: String
    if (accept('?')) {
      val openCaptureGroupIdx = idx
      var c = next() ?: throw InvalidCaptureGroupSyntax(pattern, idx)
      val closeCaptureGroupChar = when (c) {
        '<' -> '>'
        '\'' -> '\''
        else -> throw InvalidCaptureGroupSyntax(pattern, idx)
      }
      val groupNameStart = idx
      c = next() ?: throw UnmatchedOpeningCaptureGroup(pattern, openCaptureGroupIdx)
      if (c !in 'a'..'z' && c !in 'A'..'Z' && c != '_')
        throw InvalidCaptureGroupName(pattern, idx)
      while (peek() != closeCaptureGroupChar) {
        c = next() ?: throw UnmatchedOpeningCaptureGroup(pattern, openCaptureGroupIdx)
        if (c == ')')
          throw UnmatchedOpeningCaptureGroup(pattern, openCaptureGroupIdx)
        if (c !in 'a'..'z' && c !in 'A'..'Z' && c !in '0'..'9' && c != '_')
          throw InvalidCaptureGroupName(pattern, idx)
      }
      groupName = pattern.slice(groupNameStart until idx)
      expect(closeCaptureGroupChar, InvalidCaptureGroupSyntax(pattern, idx))
    } else {
      groupName = "$groupIndex"
    }
    groupIndex++

    val groupStart = newState()
    fsm.path(start, null, groupStart)
    val groupEnd = expression(groupStart)
    val end = newState()
    fsm.path(groupEnd, null, end)
    fsm.group(groupName, start, groupEnd)
    return end
  }
}
