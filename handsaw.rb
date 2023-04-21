class Handsaw < Formula
    desc "A tool for generating i18n strings for multiple platforms"
    homepage "https://github.com/MixinNetwork/handsaw"
    url "https://github.com/MixinNetwork/handsaw/releases/download/0.5.11/handsaw.zip"
    version "0.5.11"
    sha256 "10376dcecf35ff1dee0a22f356f2b42ce3226203dac3cc1e802c8da5368c9e51"

    depends_on "openjdk"

    def install
      rm_f Dir["bin/*.bat"]
      libexec.install %w[bin lib]
      (bin/"handsaw").write_env_script libexec/"bin/handsaw",
        :JAVA_HOME => "${JAVA_HOME:-#{Formula["openjdk"].opt_prefix}}"
    end

    test do
      output = shell_output("#{bin}/handsaw --help")
      assert_includes output, "Usage: handsaw"
    end
  end
