class Handsaw < Formula
    desc "A tool for generating i18n strings for multiple platforms"
    homepage "https://github.com/MixinNetwork/handsaw"
    url "https://github.com/MixinNetwork/handsaw/releases/download/0.5.8/handsaw.zip"
    version "0.5.8"
    sha256 "69acd893378ffc71d3003f72eada4c8740e38364bb5b12a73dded15db94efbc9"

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