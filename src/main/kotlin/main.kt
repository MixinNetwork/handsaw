import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(vararg args: String) {
  NoOpCliktCommand(name = "mi18n")
    .subcommands(GenerateCommand())
    .main(args)
}

private abstract class I18nCommand(
    name: String,
    help: String
) : CliktCommand(
  name = name,
  help = help
) {
  override fun run() {
  }
}

private class GenerateCommand : I18nCommand(
  name = "gen",
  help = "Perform generate i18n strings"
) {

}