package org.commonprovenance.framework.nro.support.fixture;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public final class Pki {

  private static final AtomicLong SERIAL = new AtomicLong(1);
  private static final Instant NOT_BEFORE = Instant.now().minus(Duration.ofDays(1));
  private static final Instant NOT_AFTER = Instant.now().plus(Duration.ofDays(365));

  private static final KeyPair ROOT_KEYS = keyPair();
  private static final X509Certificate ROOT = selfSigned(ROOT_KEYS, "CN=CPF Test Root CA");
  private static final KeyPair INTERMEDIATE_1_KEYS = keyPair();
  private static final X509Certificate INTERMEDIATE_1 = issued(ROOT, ROOT_KEYS, INTERMEDIATE_1_KEYS, "CN=cpf-int1", new BasicConstraints(1), KeyUsage.keyCertSign | KeyUsage.cRLSign);
  private static final KeyPair INTERMEDIATE_2_KEYS = keyPair();
  private static final X509Certificate INTERMEDIATE_2 = issued(INTERMEDIATE_1, INTERMEDIATE_1_KEYS, INTERMEDIATE_2_KEYS, "CN=cpf-int2", new BasicConstraints(0), KeyUsage.keyCertSign | KeyUsage.cRLSign);
  private static final KeyPair TRUSTED_PARTY_KEYS = keyPair();
  private static final X509Certificate TRUSTED_PARTY = issued(ROOT, ROOT_KEYS, TRUSTED_PARTY_KEYS, "CN=trusted-party", new BasicConstraints(false), KeyUsage.digitalSignature);

  private Pki() {
  }

  public record ClientCertificate(String pem, PrivateKey privateKey) {
  }

  public static ClientCertificate newClientCertificate(String organizationId) {
    KeyPair keys = keyPair();
    X509Certificate certificate = issued(INTERMEDIATE_2, INTERMEDIATE_2_KEYS, keys, "CN=" + organizationId, new BasicConstraints(false), KeyUsage.digitalSignature);
    return new ClientCertificate(pem(certificate), keys.getPrivate());
  }

  public static List<String> intermediateCertificates() {
    return List.of(pem(INTERMEDIATE_2), pem(INTERMEDIATE_1));
  }

  public static String rootCertificate() {
    return pem(ROOT);
  }

  public static String trustedPartyCertificate() {
    return pem(TRUSTED_PARTY);
  }

  public static String trustedPartyCertificateDer() {
    return Base64.getEncoder().encodeToString(encoded(TRUSTED_PARTY));
  }

  public static PublicKey trustedPartyPublicKey() {
    return TRUSTED_PARTY.getPublicKey();
  }

  public static String trustedPartyPrivateKey() {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(TRUSTED_PARTY_KEYS.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }

  public static String digestOf(String certificatePem) {
    try {
      X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
          .generateCertificate(new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.US_ASCII)));
      return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String pem(X509Certificate certificate) {
    return "-----BEGIN CERTIFICATE-----\n"
        + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(encoded(certificate))
        + "\n-----END CERTIFICATE-----\n";
  }

  private static byte[] encoded(X509Certificate certificate) {
    try {
      return certificate.getEncoded();
    } catch (CertificateEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static KeyPair keyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
      generator.initialize(new ECGenParameterSpec("secp256r1"));
      return generator.generateKeyPair();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static X509Certificate selfSigned(KeyPair keys, String subject) {
    X500Principal name = new X500Principal(subject);
    return signed(
        new JcaX509v3CertificateBuilder(name, serial(), Date.from(NOT_BEFORE), Date.from(NOT_AFTER), name, keys.getPublic()),
        keys,
        new BasicConstraints(true),
        KeyUsage.keyCertSign | KeyUsage.cRLSign);
  }

  private static X509Certificate issued(X509Certificate issuer, KeyPair issuerKeys, KeyPair keys, String subject, BasicConstraints constraints, int keyUsage) {
    return signed(
        new JcaX509v3CertificateBuilder(issuer, serial(), Date.from(NOT_BEFORE), Date.from(NOT_AFTER), new X500Principal(subject), keys.getPublic()),
        issuerKeys,
        constraints,
        keyUsage);
  }

  private static X509Certificate signed(X509v3CertificateBuilder builder, KeyPair signerKeys, BasicConstraints constraints, int keyUsage) {
    try {
      builder.addExtension(Extension.basicConstraints, true, constraints);
      builder.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsage));
      return new JcaX509CertificateConverter().getCertificate(
          builder.build(new JcaContentSignerBuilder("SHA256withECDSA").build(signerKeys.getPrivate())));
    } catch (CertIOException | OperatorCreationException | java.security.cert.CertificateException e) {
      throw new IllegalStateException(e);
    }
  }

  private static BigInteger serial() {
    return BigInteger.valueOf(SERIAL.getAndIncrement());
  }
}
