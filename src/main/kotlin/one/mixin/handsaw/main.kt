package one.mixin.handsaw

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

fun main(vararg args: String) {
  NoOpCliktCommand(name = "mi18n")
    .subcommands(GenerateCommand(), ReadCommand(), ViewCommand())
    .main(args)
}

private class GenerateCommand : CliktCommand(
  name = "gen",
  help = "Perform generate i18n strings"
) {
  private val inputFile by option("-i", "--input")
    .required()
    .help("Xlsx file only now, sqlite database file will be supported in future")

  private val output by option("-o", "--output")
    .help("Specify a directory to save the generated result")

  private val platform by option ("-p", "--platform")
    .help("Generate specific platform i18n strings")

  override fun run() {
    val parser = XLSXParser()
    val parseResult = parser.parse(inputFile)

    if (platform.isNullOrBlank()) {
      allGenerators.forEach { generator ->
        generator.generate(parseResult, output)
      }
    } else {
      val generator = if (platform.equals("Android", true)) {
        AndroidGenerator()
      } else {
        IOSGenerator()
      }
      generator.generate(parseResult, output)
    }
  }
}

private class ReadCommand : CliktCommand(
  name = "read",
  help = "Read i18n strings from specify platform"
) {
  private val inputFile by option("-i", "--input")
    .required()
    .help("Xlsx file only now, sqlite database file will be supported in future")

  private val output by option("-o", "--output")
    .help("Specify a directory to save the generated result")

  override fun run() {
    val reader = AndroidReader()
    reader.read(inputFile, output)
  }
}

private class ViewCommand : CliktCommand(
  name = "view",
  help = "View i18n strings"
) {
  override fun run() {

  }
}