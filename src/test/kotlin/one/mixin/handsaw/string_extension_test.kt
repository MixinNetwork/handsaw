package one.mixin.handsaw

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionTest {
  @Test
  fun testJoinWithCharacter() {
    val s1 = "hello520你好"
    val s2 = "我们将发送4位验证码到手机 %1\$s，请在下一个页面输入"
    val s3: String? = null
    val s4 = "点击“同意并继续” 接受Mixin%1\$s和%2\$s"
    val s5 = "Mixin使用本地網路同步或恢復您的聊天記錄"
    val s6 = "傳輸聊天記錄 （%1\$s%） "
    val s7 = "Mixin錢包現已不支持 %1\$s 充值，注意已轉入的 %2\$s 資產可以繼續轉賬或提現，閲讀文檔**瞭解更多**。"
    val s8 = "通過%@授權桌面登錄"

    assertEquals("hello 520 你好", s1.joinWithCharacter())
    assertEquals("我们将发送 4 位验证码到手机 %1\$s，请在下一个页面输入", s2.joinWithCharacter())
    assertEquals("", s3.joinWithCharacter())
    assertEquals("点击“同意并继续”接受 Mixin %1\$s和%2\$s", s4.joinWithCharacter())
    assertEquals("Mixin 使用本地網路同步或恢復您的聊天記錄", s5.joinWithCharacter())
    assertEquals("傳輸聊天記錄（%1\$s%）", s6.joinWithCharacter())
    assertEquals("Mixin 錢包現已不支持 %1\$s 充值，注意已轉入的 %2\$s 資產可以繼續轉賬或提現，閲讀文檔**瞭解更多**。", s7.joinWithCharacter())
    assertEquals("通過%@授權桌面登錄", s8.joinWithCharacter())
  }
}