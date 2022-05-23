class Handsaw < Formula
    desc "A tool for generating i18n strings for multiple platforms"
    homepage "https://github.com/MixinNetwork/handsaw"
    url "https://github.com/MixinNetwork/handsaw/releases/download/0.3.0/handsaw.zip"
    version "0.3.0"
    sha256 "79753e4fca007e3c1c5daf9f05138b55f9cff839a05909942aff9a41438614ec"

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