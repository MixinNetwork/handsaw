class Handsaw < Formula
    desc "A tool for generating i18n strings for multiple platforms"
    homepage "https://github.com/MixinNetwork/handsaw"
    url "https://github.com/MixinNetwork/handsaw/releases/download/0.4.1/handsaw.zip"
    version "0.4.1"
    sha256 "d0a8fe7c5b2e6534002b6d5c8eb6370cc2f1a56b95ffcb13a2f0fa7e43421853"

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