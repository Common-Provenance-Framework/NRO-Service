package org.commonprovenance.framework.nro.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.commonprovenance.framework.nro.support.fixture.Pki;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class PkiInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final Path PKI_DIRECTORY = write();

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
        "app.trustedCertsDirPath=" + PKI_DIRECTORY.resolve("trusted"),
        "app.publicCertPath=" + PKI_DIRECTORY.resolve("trusted-party.pem"),
        "app.privateKeyPath=" + PKI_DIRECTORY.resolve("trusted-party.key"))
        .applyTo(applicationContext.getEnvironment());
  }

  private static Path write() {
    try {
      Path directory = Files.createTempDirectory("nro-it-pki");
      Files.createDirectory(directory.resolve("trusted"));
      Files.writeString(directory.resolve("trusted").resolve("root-ca.pem"), Pki.rootCertificate());
      Files.writeString(directory.resolve("trusted-party.pem"), Pki.trustedPartyCertificate());
      Files.writeString(directory.resolve("trusted-party.key"), Pki.trustedPartyPrivateKey());
      return directory;
    } catch (IOException e) {
      throw new UncheckedIOException("Can not write the test PKI", e);
    }
  }
}
