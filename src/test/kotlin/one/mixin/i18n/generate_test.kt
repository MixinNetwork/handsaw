package one.mixin.i18n

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
  }
}