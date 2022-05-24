package one.mixin.handsaw

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int

fun main(vararg args: String) {
  NoOpCliktCommand(name = "handsaw")
    .subcommands(GenerateCommand(), ReadCommand())
    .main(args)
}

private class GenerateCommand : CliktCommand(
  name = "gen",
  help = "Generate i18n strings from other sources, xlsx file only for now"
) {
  private val inputFile by option("-i", "--input")
    .required()
    .help("Xlsx file only now, sqlite database file will be supported in future")

  private val output by option("-o", "--output")
    .help("Specify a directory to save the generated result")

  private val platform by option ("-p", "--platform")
    .help("Generate specific platform i18n strings")

  private val keyType by option("-k", "--key-type")
    .help("Specify the key type, default is 0, which means use the key as is, 1 means use the English value as the key, only works for iOS")
    .int()
    .default(0)

  override fun run() {
    val parser = XLSXParser()
    val parseResult = parser.parse(inputFile)

    if (platform.isNullOrBlank()) {
      val allGenerators = listOf(AndroidGenerator(), getIOSGenerator(keyType), FlutterGenerator())
      allGenerators.forEach { generator ->
        generator.generate(parseResult, output)
      }
    } else {
      val generator = if (platform.equals("Android", true)) {
        AndroidGenerator()
      } else if (platform.equals("iOS", true)) {
        getIOSGenerator(keyType)
      } else if (platform.equals("Flutter", true)) {
        FlutterGenerator()
      } else {
        throw IllegalArgumentException("Unsupported platform: $platform")
      }
      generator.generate(parseResult, output)
    }
  }

  private fun getIOSGenerator(keyType: Int): IOSGenerator {
    return if (keyType != 0) {
      IOSGenerator(KeyType.EnValue)
    } else {
      IOSGenerator()
    }
  }
}

private class ReadCommand : CliktCommand(
  name = "read",
  help = "Read i18n strings from specify platform"
) {
  private val inputFile by option("-i", "--input")
    .required()
    .help("Xlsx file only now")

  private val output by option("-o", "--output")
    .help("Specify a directory to save the generated result")

  override fun run() {
    val reader = AndroidReader()
    reader.read(inputFile, output)
  }
}
