package re

import automaton.FiniteStateMachine.New
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RegexParserTest {
  @Test
  fun atom() {
    val actual = RegexParser("""a""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .group(MAIN_GROUP, 0, 1)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun sequence() {
    val actual = RegexParser("""abc""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(2, 'c', 3)
            .group(MAIN_GROUP, 0, 3)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun quantifier_zero_or_one() {
    val actual = RegexParser("""ab?c""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(1, null, 2)
            .path(2, 'c', 3)
            .group(MAIN_GROUP, 0, 3)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun quantifier_one_or_more() {
    val actual = RegexParser("""ab+c""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(2, null, 1)
            .path(2, null, 3)
            .path(3, 'c', 4)
            .group(MAIN_GROUP, 0, 4)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun quantifier_zero_or_more() {
    val actual = RegexParser("""ab*c""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(2, null, 3)
            .path(2, null, 1)
            .path(1, null, 3)
            .path(3, 'c', 4)
            .group(MAIN_GROUP, 0, 4)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun expression() {
    val actual = RegexParser("""ab|cd|ef""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(0, 'c', 3)
            .path(3, 'd', 4)
            .path(0, 'e', 5)
            .path(5, 'f', 6)
            .path(2, null, 7)
            .path(4, null, 7)
            .path(6, null, 7)
            .group(MAIN_GROUP, 0, 7)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun parenthesis() {
    val actual = RegexParser("""(a|b)(c|d)(e|f)""").parse()
    val expected = New<Int, Char>(0)
            .path(0, null, 1)
            .path(1, 'a', 2)
            .path(1, 'b', 3)
            .path(2, null, 4)
            .path(3, null, 4)
            .path(4, null, 5)
            .path(5, null, 6)
            .path(6, 'c', 7)
            .path(6, 'd', 8)
            .path(7, null, 9)
            .path(8, null, 9)
            .path(9, null, 10)
            .path(10, null, 11)
            .path(11, 'e', 12)
            .path(11, 'f', 13)
            .path(12, null, 14)
            .path(13, null, 14)
            .path(14, null, 15)
            .group("1", 0, 4)
            .group("2", 5, 9)
            .group("3", 10, 14)
            .group(MAIN_GROUP, 0, 15)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun character_class() {
    val actual = RegexParser("""[abc]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(0, 'b', 1)
            .path(0, 'c', 1)
            .group(MAIN_GROUP, 0, 1)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun character_class_range() {
    val actual = RegexParser("""[a-c]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(0, 'b', 1)
            .path(0, 'c', 1)
            .group(MAIN_GROUP, 0, 1)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun character_class_first_dash() {
    val actual = RegexParser("""[-c]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '-', 1)
            .path(0, 'c', 1)
            .group(MAIN_GROUP, 0, 1)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun escape_characters() {
    val actual = RegexParser("""\0\a\e\f\n\r\t[\0\a\e\f\n\r\t]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 0.toChar(), 1)
            .path(1, 0x07.toChar(), 2)
            .path(2, 0x1a.toChar(), 3)
            .path(3, 0x0c.toChar(), 4)
            .path(4, '\n', 5)
            .path(5, '\r', 6)
            .path(6, '\t', 7)
            .path(7, 0.toChar(), 8)
            .path(7, 0x07.toChar(), 8)
            .path(7, 0x1a.toChar(), 8)
            .path(7, 0x0c.toChar(), 8)
            .path(7, '\n', 8)
            .path(7, '\r', 8)
            .path(7, '\t', 8)
            .group(MAIN_GROUP, 0, 8)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun escape_metacharacter() {
    val actual = RegexParser("""\[\\\^\$\.\|\?\*\+\(\)\{\}""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '[', 1)
            .path(1, '\\', 2)
            .path(2, '^', 3)
            .path(3, '$', 4)
            .path(4, '.', 5)
            .path(5, '|', 6)
            .path(6, '?', 7)
            .path(7, '*', 8)
            .path(8, '+', 9)
            .path(9, '(', 10)
            .path(10, ')', 11)
            .path(11, '{', 12)
            .path(12, '}', 13)
            .group(MAIN_GROUP, 0, 13)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun escape_line_break() {
    val actual = RegexParser("""\R[\R]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '\r', 1)
            .path(0, '\n', 1)
            .path(0, '\r', 2)
            .path(2, '\n', 1)
            .path(1, '\r', 3)
            .path(1, '\n', 3)
            .path(1, '\r', 4)
            .path(4, '\n', 3)
            .group(MAIN_GROUP, 0, 3)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun escape_octal() {
    val actual = RegexParser("""\052\100\o{52}\o{100}\o{00000052}""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '*', 1)
            .path(1, '@', 2)
            .path(2, '*', 3)
            .path(3, '@', 4)
            .path(4, '*', 5)
            .group(MAIN_GROUP, 0, 5)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun escape_hex() {
    val actual = RegexParser("""\x2a\x2A\x{2a}\x{0000002A}""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '*', 1)
            .path(1, '*', 2)
            .path(2, '*', 3)
            .path(3, '*', 4)
            .group(MAIN_GROUP, 0, 4)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun escape_unicode() {
    val actual = RegexParser("""\u20ac\u20AC""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 0x20.toChar(), 1)
            .path(1, 0xAC.toChar(), 2)
            .path(2, 0x20.toChar(), 3)
            .path(3, 0xAC.toChar(), 4)
            .group(MAIN_GROUP, 0, 4)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun escape_sequence() {
    val actual = RegexParser("""\Q[\^$.|?*+(){}\E""").parse()
    val expected = New<Int, Char>(0)
            .path(0, '[', 1)
            .path(1, '\\', 2)
            .path(2, '^', 3)
            .path(3, '$', 4)
            .path(4, '.', 5)
            .path(5, '|', 6)
            .path(6, '?', 7)
            .path(7, '*', 8)
            .path(8, '+', 9)
            .path(9, '(', 10)
            .path(10, ')', 11)
            .path(11, '{', 12)
            .path(12, '}', 13)
            .group(MAIN_GROUP, 0, 13)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun control_characters() {
    val pattern = ('A'..'Z').joinToString("") { "\\c$it" } +
            ('a'..'z').joinToString("") { "\\c$it" } +
            "[" + ('A'..'Z').joinToString("") { "\\c$it" } +
            ('a'..'z').joinToString("") { "\\c$it" } + "]"
    val actual = RegexParser(pattern).parse()
    val expected = New<Int, Char>(0)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun shorthand_digit() {
    val actual = RegexParser("""\d[\d]""").parse()
    val builder = New<Int, Char>(0)
    for (c in '0'..'9') {
      builder.path(0, c, 1)
      builder.path(1, c, 2)
    }
    val expected = builder
            .group(MAIN_GROUP, 0, 2)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun shorthand_word() {
    val actual = RegexParser("""\w[\w]""").parse()
    val builder = New<Int, Char>(0)
    builder.path(0, '_', 1)
    builder.path(1, '_', 2)
    for (c in 'a'..'z') {
      builder.path(0, c, 1)
      builder.path(0, c.toUpperCase(), 1)
      builder.path(1, c, 2)
      builder.path(1, c.toUpperCase(), 2)
    }
    val expected = builder
            .group(MAIN_GROUP, 0, 2)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun shorthand_space() {
    val actual = RegexParser("""\s[\s]""").parse()
    val expected = New<Int, Char>(0)
            .path(0, ' ', 1)
            .path(0, '\t', 1)
            .path(0, '\r', 1)
            .path(0, '\n', 1)
            .path(0, 0x0b.toChar(), 1)
            .path(0, 0x0c.toChar(), 1)
            .path(1, ' ', 2)
            .path(1, '\t', 2)
            .path(1, '\r', 2)
            .path(1, '\n', 2)
            .path(1, 0x0b.toChar(), 2)
            .path(1, 0x0c.toChar(), 2)
            .group(MAIN_GROUP, 0, 2)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun curly_braces() {
    val actual = RegexParser("""a{}b{0,2""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, '{', 2)
            .path(2, '}', 3)
            .path(3, 'b', 4)
            .path(4, '{', 5)
            .path(5, '0', 6)
            .path(6, ',', 7)
            .path(7, '2', 8)
            .group(MAIN_GROUP, 0, 8)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun quantifier_fixed() {
    val actual = RegexParser("""ab{3}c""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(2, 'b', 3)
            .path(3, 'b', 4)
            .path(4, 'c', 5)
            .group(MAIN_GROUP, 0, 5)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun quantifier_range() {
    val actual = RegexParser("""ab{1,3}c""").parse()
    val expected = New<Int, Char>(0)
            .path(0, 'a', 1)
            .path(1, 'b', 2)
            .path(2, null, 4)
            .path(2, 'b', 3)
            .path(3, null, 4)
            .path(3, 'b', 4)
            .path(4, 'c', 5)
            .group(MAIN_GROUP, 0, 5)
            .build()
    println(expected)
    println(actual)
    assertEquals(expected, actual)
  }

  @Test
  fun groups() {
    val actual = RegexParser("""(?<G1>a)(?'G2'b)""").parse()
    val expected = New<Int, Char>(0)
            .path(0, null, 1)
            .path(1, 'a', 2)
            .path(2, null, 3)
            .path(3, null, 4)
            .path(4, 'b', 5)
            .path(5, null, 6)
            .group("G1", 0, 2)
            .group("G2", 3, 5)
            .group(MAIN_GROUP, 0, 6)
            .build()
    assertEquals(expected, actual)
  }

  @Test
  fun `empty pattern`() {
    assertFailsWith(IllegalArgumentException::class) {
      RegexParser("").parse()
    }
  }

  @Test
  fun `dangling backslash`() {
    assertFailsWith(DanglingBackslashError::class) {
      RegexParser("""\""").parse()
    }
  }

  @Test
  fun `unmatched opening parenthesis`() {
    assertFailsWith(UnmatchedOpeningParenthesisError::class) {
      RegexParser("""(""").parse()
    }
  }

  @Test
  fun `unmatched opening square bracket`() {
    assertFailsWith(UnmatchedOpeningSquareBracketError::class) {
      RegexParser("""[""").parse()
    }
  }

  @Test
  fun `unmatched opening capture group`() {
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?<""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?'""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?<a""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?'a""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?<a)""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?'a)""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?<a0""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?'a0""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?<a0)""").parse()
    }
    assertFailsWith(UnmatchedOpeningCaptureGroup::class) {
      RegexParser("""(?'a0)""").parse()
    }
  }

  @Test
  fun `invalid character class range`() {
    assertFailsWith(InvalidCharacterClassRange::class) {
      RegexParser("""[c-a]""").parse()
    }
  }

  @Test
  fun `invalid capture group syntax`() {
    assertFailsWith(InvalidCaptureGroupSyntax::class) {
      RegexParser("""(?""").parse()
    }
    assertFailsWith(InvalidCaptureGroupSyntax::class) {
      RegexParser("""(?a""").parse()
    }
  }

  @Test
  fun `invalid capture group name`() {
    assertFailsWith(InvalidCaptureGroupName::class) {
      RegexParser("""(?<*""").parse()
    }
    assertFailsWith(InvalidCaptureGroupName::class) {
      RegexParser("""(?<a*""").parse()
    }
  }
}