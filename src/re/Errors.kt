package re

open class SyntaxError(pattern: String, col: Int, message: String = "syntax error") :
        Exception("syntax error:$col: $message\n" +
                "$pattern\n" +
                " ".repeat(col) + "^")

class DanglingBackslashError(pattern: String, col: Int) :
        SyntaxError(pattern, col - 1, "dangling backslash")

class InvalidCharacterClassRange(pattern: String, col: Int) :
        SyntaxError(pattern, col - 1,
                "invalid character class range, " +
                        "the state char code '${pattern[col - 1]}' (${pattern[col - 1].toInt()}) " +
                        "must be less or equal to the " +
                        "end char code '${pattern[col + 1]}' (${pattern[col + 1].toInt()}) ")

class InvalidCaptureGroupName(pattern: String, col: Int) :
        SyntaxError(pattern, col - 1, "invalid capture group value, must matches `[_a-zA-Z0-9]\\w*`")

class InvalidCaptureGroupSyntax(pattern: String, col: Int) :
        SyntaxError(pattern, col - 1, "invalid capture group syntax, must be " +
                "`(?<group>pattern)` or `(?'group'pattern)`")

class UnmatchedOpeningCaptureGroup(pattern: String, col: Int) :
        SyntaxError(pattern, col, "unmatched opening capture group")

class UnmatchedOpeningParenthesisError(pattern: String, col: Int) :
        SyntaxError(pattern, col, "unmatched opening parenthesis")

class UnmatchedOpeningSquareBracketError(pattern: String, col: Int) :
        SyntaxError(pattern, col, "unmatched opening square bracket")
