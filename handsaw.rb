class Handsaw < Formula
    desc "A tool for generating i18n strings for multiple platforms"
    homepage "https://github.com/MixinNetwork/handsaw"
    url "https://github.com/MixinNetwork/handsaw/releases/download/0.5.13/handsaw.zip"
    version "0.5.13"
    sha256 "9be68a6066bf614dada3e674ad522515abf2195d568aefe33481e1d62e9d8ccb"

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
