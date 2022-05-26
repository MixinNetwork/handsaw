package one.mixin.handsaw

import org.junit.Assert
import org.junit.Test

class GeneratorTest {

  @Test
  fun `test generate`() {
    val parser = XLSXParser()
    val parseResult = parser.parse("src/test/resources/example.xlsx")

    val androidGenerator = AndroidGenerator()
    androidGenerator.generate(parseResult, null)

    val iOSGenerator = IOSGenerator()
    iOSGenerator.generate(parseResult, null)

    // val iOSGeneratorEn = IOSGenerator(KeyType.EnValue)
    // iOSGeneratorEn.generate(parseResult, null)

    val flutterGenerator = FlutterGenerator()
    flutterGenerator.generate(parseResult, null)
  }

  @Test
  fun `test strings with invalid char`() {
    val parser = XLSXParser()
    val parseResult = parser.parse("src/test/resources/exampleWithInvalidChar.xlsx")

    val androidGenerator = AndroidGenerator()
    try {
      androidGenerator.generate(parseResult, null)
    } catch (e: FormatException) {
      return
    }
    Assert.fail("should throw FormatException!")
  }
}