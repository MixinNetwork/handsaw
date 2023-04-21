package one.mixin.handsaw

/**
 *  Joins a string with a specified character while ignoring certain regex patterns.
 *
 *   @param char The character to join the string with.
 *   @param ignoreRegexes A list of regex patterns to ignore while joining the string.
 *   @return The string joined with the specified character, ignoring any regex patterns in the ignoreRegexes list.
 */
fun String?.joinWithCharacter(
  char: Char = ' ',
  ignoreRegexes: List<Regex> = listOf(androidPlaceHolder, iosPlaceHolder)
): String {
  if (this == null) return ""

  val ignoreRanges = mutableListOf<IntRange>()
  ignoreRegexes.forEach { regex ->
    regex.findAll(this).iterator().forEach { matchResult ->
      ignoreRanges.add(matchResult.range)
    }
  }

  val result = StringBuilder()
  this.trim().forEachIndexed { i, c ->
    if (ignoreRanges.any { range -> range.contains(i) }) {
      result.append(c)
      return@forEachIndexed
    }

    if (c.isWhitespace() && !ignoreRanges.any { range -> range.first -1 == i || range.last + 1 == i }) {
      return@forEachIndexed
    }

    val lookAhead = try {
      this[i + 1]
    } catch (ignored: IndexOutOfBoundsException) {
      char
    }
    val isSameType = if (c.isAlphabet() && lookAhead.isAlphabet()) {
      true
    } else if (c.isDigit() && lookAhead.isDigit()) {
      true
    } else {
      !c.isDigit() && !lookAhead.isDigit() && !c.isAlphabet() && !lookAhead.isAlphabet()
    }

    val needWhiteSpace = !isSameType && !c.isWhitespace()
    result.append(c)
    if (needWhiteSpace) {
      result.append(char)
    }
  }
  return result.toString().trim()
}

private fun Char.isAlphabet() = this in 'a'..'z' || this in 'A'..'Z'
