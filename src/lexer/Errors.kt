package lexer

class DuplicateSymbols(duplicates: Set<String>) : Exception("duplicate symbols: $duplicates")

class EmptySymbolName : Exception("empty symbol value")

class EmptyVocabularyPattern(symbol: String) : Exception("empty vocabulary pattern for symbol $symbol")

class AmbiguousVocabulary(symbols: Set<String>) : Exception("ambiguous vocabulary: $symbols")