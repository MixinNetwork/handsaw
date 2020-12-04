package one.mixin.handsaw

import org.junit.Test

class ParseTest {

  @Test
  fun `test parse xlsx file`() {
    XLSXParser().parse("src/test/resources/example.xlsx")
  }
}