package one.mixin.i18n

import org.junit.Test

class ReaderTest {
  @Test
  fun `test read Android strings`() {
    val reader = AndroidReader()
    reader.read("src/test/resources", null)
  }
}