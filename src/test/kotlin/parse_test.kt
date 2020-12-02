import org.junit.Test

class ParseTest {

    @Test
    fun `test parse xlsx file`() {
        XLSXParser().parse("src/test/example.xlsx")
    }
}