package one.mixin.i18n

import org.junit.Test

class ConverterTest {

  @Test
  fun `test generate`() {
    val parser = XLSXParser()
    val parseResult = parser.parse("src/test/example.xlsx")

    val androidGenerator = AndroidGenerator()
    androidGenerator.generate(parseResult)

    val iOSGenerator = IOSGenerator()
    iOSGenerator.generate(parseResult)
  }
}