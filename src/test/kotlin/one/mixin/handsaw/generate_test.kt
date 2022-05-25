package one.mixin.handsaw

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

    val iOSGeneratorEn = IOSGenerator(KeyType.EnValue)
    iOSGeneratorEn.generate(parseResult, null)

    val flutterGenerator = FlutterGenerator()
    flutterGenerator.generate(parseResult, null)
  }
}