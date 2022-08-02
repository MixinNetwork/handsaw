package one.mixin.handsaw

import org.junit.Assert
import org.junit.Test

class GeneratorTest {

  @Test
  fun `test generate`() {
    // val parser = XLSXParser()
    // val parseResult = parser.parse("src/test/resources/example.xlsx")

    val parser = XMLParser()
    val parseResult = parser.parse("src/test/resources/xmldir")

    val androidGenerator = AndroidGenerator()
    androidGenerator.generate(parseResult, null)

    var iOSGenerator = IOSGenerator(Platform.IOS)
    iOSGenerator.generate(parseResult, null)

    iOSGenerator = IOSGenerator(Platform.IOSAuthentication)
    iOSGenerator.generate(parseResult, null)

    iOSGenerator = IOSGenerator(Platform.AppStore)
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