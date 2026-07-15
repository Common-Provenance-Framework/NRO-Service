package org.commonprovenance.framework.nro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.enums.DocumentType;
import org.commonprovenance.framework.nro.data.enums.HashFunction;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.Token;
import org.commonprovenance.framework.nro.data.repository.CertificateRepository;
import org.commonprovenance.framework.nro.data.repository.DocumentRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationRepository;
import org.commonprovenance.framework.nro.data.repository.TokenRepository;
import org.commonprovenance.framework.nro.exceptions.CertificateNotFoundException;
import org.commonprovenance.framework.nro.exceptions.DocumentNotFoundException;
import org.commonprovenance.framework.nro.exceptions.InvalidRequestException;
import org.commonprovenance.framework.nro.exceptions.InvalidTimestampException;
import org.commonprovenance.framework.nro.exceptions.MissingSignatureException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.commonprovenance.framework.nro.exceptions.SignatureVerificationException;
import org.commonprovenance.framework.nro.exceptions.TokenAlreadyExistsException;
import org.commonprovenance.framework.nro.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private DocumentRepository documentRepository;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private AppProperties appProperties;

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService(
        tokenRepository,
        organizationRepository,
        documentRepository,
        certificateRepository,
        appProperties);
  }

  @Test
  void getToken_existingDocument_returnsTokens() {
    Organization organization = new Organization();
    organization.setId("org-1");
    Document document = new Document();
    document.setIdentifier("doc-1");

    List<Token> tokens = List.of(new Token());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(documentRepository.findByIdentifierAndDocFormatAndDocumentTypeAndOrganization(
        "doc-1",
        "json",
        DocumentType.GRAPH,
        organization)).thenReturn(Optional.of(document));
    when(tokenRepository.findByDocument(document)).thenReturn(tokens);

    List<Token> result = tokenService.getToken("org-1", "doc-1", "json");

    assertThat(result).isSameAs(tokens);
  }

  @Test
  void getToken_missingOrganization_throwsOrganizationNotFoundException() {
    when(organizationRepository.findById("missing-org")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.getToken("missing-org", "doc-1", "json"))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("missing-org");
  }

  @Test
  void getToken_missingDocument_throwsDocumentNotFoundException() {
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(documentRepository.findByIdentifierAndDocFormatAndDocumentTypeAndOrganization(
        "doc-404",
        "json",
        DocumentType.GRAPH,
        organization)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.getToken("org-1", "doc-404", "json"))
        .isInstanceOf(DocumentNotFoundException.class)
        .hasMessageContaining("doc-404");
  }

  @Test
  void getAllTokens_existingOrganization_returnsMap() {
    Organization organization = new Organization();
    organization.setId("org-1");
    Document documentA = new Document();
    documentA.setIdentifier("doc-a");
    Document documentB = new Document();
    documentB.setIdentifier("doc-b");
    List<Document> documents = List.of(documentA, documentB);
    List<Token> tokensA = List.of(new Token());
    List<Token> tokensB = List.of(new Token(), new Token());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(documentRepository.findByOrganization(organization)).thenReturn(documents);
    when(tokenRepository.findByDocument(documentA)).thenReturn(tokensA);
    when(tokenRepository.findByDocument(documentB)).thenReturn(tokensB);

    Map<Document, List<Token>> result = tokenService.getAllTokens("org-1");

    assertThat(result)
        .hasSize(2)
        .containsEntry(documentA, tokensA)
        .containsEntry(documentB, tokensB);
  }

  @Test
  void getAllTokens_missingOrganization_throwsOrganizationNotFoundException() {
    when(organizationRepository.findById("missing-org")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.getAllTokens("missing-org"))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("missing-org");
  }

  @Test
  void issueToken_missingOrganizationId_throwsInvalidRequestException() {
    TokenRequestDTO body = buildRequest(DocumentType.META);
    body.setOrganizationId(" ");
    body.setDocument(null);

    assertThatThrownBy(() -> tokenService.issueToken(body))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("Missing organizationId");
  }

  @Test
  void issueToken_graphMissingSignature_throwsMissingSignatureException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    body.setSignature(" ");

    assertThatThrownBy(() -> tokenService.issueToken(body))
        .isInstanceOf(MissingSignatureException.class);
  }

  @Test
  void issueToken_futureTimestamp_throwsInvalidTimestampException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    body.setCreatedOn(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    assertThatThrownBy(() -> tokenService.issueToken(body))
        .isInstanceOf(InvalidTimestampException.class);
  }

  @Test
  void issueToken_missingOrganization_throwsOrganizationNotFoundException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);

    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.issueToken(body))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void issueToken_invalidSignature_throwsSignatureVerificationException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

    TokenService serviceSpy = spy(tokenService);
    doReturn(false).when(serviceSpy).verifySignature(body);

    assertThatThrownBy(() -> serviceSpy.issueToken(body))
        .isInstanceOf(SignatureVerificationException.class);
  }

  @Test
  void issueToken_validRequest_callsIssueTokenAndStoreDoc() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    Organization organization = new Organization();
    organization.setId("org-1");
    Token expected = new Token();

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

    TokenService serviceSpy = spy(tokenService);
    doReturn(true).when(serviceSpy).verifySignature(body);
    doReturn(expected).when(serviceSpy).issueTokenAndStoreDoc(body);

    Token result = serviceSpy.issueToken(body);

    assertThat(result).isSameAs(expected);
  }

  @Test
  void issueToken_missingOrganizationId_infersFromDocument() {
    TokenRequestDTO body = buildRequest(DocumentType.META);
    body.setOrganizationId(" ");
    body.setDocument(base64Of("/organizations/org-inferred/documents/doc-x"));
    Token expected = new Token();

    TokenService serviceSpy = spy(tokenService);
    doReturn(expected).when(serviceSpy).issueTokenAndStoreDoc(body);

    Token result = serviceSpy.issueToken(body);

    assertThat(result).isSameAs(expected);
    assertThat(body.getOrganizationId()).isEqualTo("org-inferred");
  }

  @Test
  void issueTokenAndStoreDoc_existingDocumentWithTokens_throwsTokenAlreadyExistsExceptionException() {
    TokenRequestDTO body = buildRequestWithBundleId(DocumentType.GRAPH, "b1");
    Organization organization = new Organization();
    organization.setId("org-1");
    Document document = new Document();
    document.setIdentifier("ex:b1");
    Token expected = new Token();

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(documentRepository.findByIdentifierAndDocFormatAndDocumentTypeAndOrganization(
        anyString(),
        eq("provn"),
        eq(DocumentType.GRAPH),
        eq(organization))).thenReturn(Optional.of(document));
    when(tokenRepository.findByDocument(document)).thenReturn(List.of(expected));

    assertThatThrownBy(() -> tokenService.issueTokenAndStoreDoc(body))
        .isInstanceOf(TokenAlreadyExistsException.class)
        .hasMessage("Token for Document with identifier [http://example.org/b1] already exists");
    // .hasMessageContaining("org-1");
  }

  @Test
  void issueTokenAndStoreDoc_missingCertificate_throwsCertificateNotFoundException() {
    TokenRequestDTO body = buildRequestWithBundleId(DocumentType.GRAPH, "b2");
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(documentRepository.findByIdentifierAndDocFormatAndDocumentTypeAndOrganization(
        anyString(),
        eq("provn"),
        eq(DocumentType.GRAPH),
        eq(organization))).thenReturn(Optional.empty());
    when(certificateRepository.findFirstByOrganizationIdAndIsRevoked("org-1", false))
        .thenReturn(null);

    assertThatThrownBy(() -> tokenService.issueTokenAndStoreDoc(body))
        .isInstanceOf(CertificateNotFoundException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void verifySignature_missingOrganization_throwsOrganizationNotFoundException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.verifySignature(body))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void verifySignature_missingCertificate_throwsCertificateNotFoundException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(certificateRepository.findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        org.commonprovenance.framework.nro.data.enums.CertificateType.CLIENT,
        false)).thenReturn(null);

    assertThatThrownBy(() -> tokenService.verifySignature(body))
        .isInstanceOf(CertificateNotFoundException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void verifySignature_invalidCertificate_throwsSignatureVerificationException() {
    TokenRequestDTO body = buildRequest(DocumentType.GRAPH);
    Organization organization = new Organization();
    organization.setId("org-1");
    org.commonprovenance.framework.nro.data.model.Certificate certificate = new org.commonprovenance.framework.nro.data.model.Certificate();
    certificate.setCert("not-a-certificate");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(certificateRepository.findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        org.commonprovenance.framework.nro.data.enums.CertificateType.CLIENT,
        false)).thenReturn(certificate);

    assertThatThrownBy(() -> tokenService.verifySignature(body))
        .isInstanceOf(SignatureVerificationException.class)
        .hasMessageContaining("Invalid signature");
  }

  @Test
  void verifySignature_validSignature_returnsTrue() throws Exception {
    KeyPair keyPair = generateEcKeyPair();
    X509Certificate x509 = createSelfSignedCertificate(keyPair);
    String certPem = toPem(x509);

    Organization organization = new Organization();
    organization.setId("org-1");
    org.commonprovenance.framework.nro.data.model.Certificate certificate = new org.commonprovenance.framework.nro.data.model.Certificate();
    certificate.setCert(certPem);

    String graph = "graph";
    byte[] graphBytes = graph.getBytes(StandardCharsets.UTF_8);
    java.security.Signature signer = java.security.Signature.getInstance("SHA256withECDSA");
    signer.initSign(keyPair.getPrivate());
    signer.update(graphBytes);
    String signatureB64 = Base64.getEncoder().encodeToString(signer.sign());

    TokenRequestDTO body = new TokenRequestDTO();
    body.setOrganizationId("org-1");
    body.setDocument(Base64.getEncoder().encodeToString(graphBytes));
    body.setSignature(signatureB64);

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(certificateRepository.findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        org.commonprovenance.framework.nro.data.enums.CertificateType.CLIENT,
        false)).thenReturn(certificate);

    boolean verified = tokenService.verifySignature(body);

    assertThat(verified).isTrue();
  }

  @Test
  void buildJwtToken_returnsJWT() throws Exception {
    KeyPair keyPair = generateEcKeyPair();
    X509Certificate x509 = createSelfSignedCertificate(keyPair);
    String certPem = toPem(x509);
    String privateKeyPem = toPkcs8Pem(keyPair.getPrivate());

    Path keyPath = Files.createTempFile("tp-test-key", ".pem");
    Files.writeString(keyPath, privateKeyPem, StandardCharsets.UTF_8);
    keyPath.toFile().deleteOnExit();

    LocalDateTime tokenTimestamp = LocalDateTime.of(2026, 1, 1, 10, 0);
    LocalDateTime documentTimestamp = LocalDateTime.of(2026, 1, 1, 9, 0);
    String documentDigest = "37a0bb04c1bbf7294ce87e5144d75cce4a99f2bb203ee0b4cf6cca0d90e8a728";
    String bundleId = "http://example.org/";
    String issuerId = "tp-id";
    String originatorId = "org-1";

    when(appProperties.getPrivateKeyPath()).thenReturn(keyPath.toString());
    when(appProperties.getCertificate()).thenReturn(certPem);
    when(appProperties.getId()).thenReturn(issuerId);
    when(appProperties.getFqdn()).thenReturn("tp.example");

    Method buildJwtToken = TokenService.class.getDeclaredMethod("buildJwtToken",
        String.class, LocalDateTime.class, LocalDateTime.class, String.class, String.class);
    buildJwtToken.setAccessible(true);
    String jwt = (String) buildJwtToken.invoke(tokenService,
        originatorId,
        tokenTimestamp,
        documentTimestamp,
        documentDigest,
        bundleId);

    SignedJWT signedJWT = SignedJWT.parse(jwt);
    // check signature
    assertThat(signedJWT.verify(new ECDSAVerifier((ECPublicKey) keyPair.getPublic()))).isTrue();

    // check payload
    assertThat(signedJWT.getJWTClaimsSet().getIssuer()).isEqualTo(issuerId);
    assertThat(signedJWT.getJWTClaimsSet().getIssueTime()).isEqualTo(Date.from(tokenTimestamp.toInstant(ZoneOffset.UTC)));
    assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo(bundleId);
    assertThat(signedJWT.getJWTClaimsSet().getClaimAsString("doc_digest")).isEqualTo(documentDigest);
    assertThat(signedJWT.getJWTClaimsSet().getClaimAsString("hash_alg")).isEqualTo("SHA256");
    assertThat(signedJWT.getJWTClaimsSet().getLongClaim("doc_iat")).isEqualTo(documentTimestamp.toEpochSecond(ZoneOffset.UTC));
    assertThat(signedJWT.getJWTClaimsSet().getClaimAsString("org_id")).isEqualTo(originatorId);

    // check header
    assertThat(signedJWT.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.ES256);
    assertThat(signedJWT.getHeader().getType()).isEqualTo(JOSEObjectType.JWT);
    assertThat(signedJWT.getHeader().getCustomParam("trustedPartyUri")).isEqualTo("tp.example");
    assertThat(signedJWT.getHeader().getX509CertChain().size()).isEqualTo(1);
  }

  @Test
  void issueTokenAndStoreDoc_metaSignsTokenData() throws Exception {
    KeyPair keyPair = generateEcKeyPair();
    X509Certificate x509 = createSelfSignedCertificate(keyPair);
    String certPem = toPem(x509);
    String privateKeyPem = toPkcs8Pem(keyPair.getPrivate());

    Path keyPath = Files.createTempFile("tp-test-key", ".pem");
    Files.writeString(keyPath, privateKeyPem, StandardCharsets.UTF_8);
    keyPath.toFile().deleteOnExit();

    when(appProperties.getPrivateKeyPath()).thenReturn(keyPath.toString());
    when(appProperties.getCertificate()).thenReturn(certPem);
    when(appProperties.getId()).thenReturn("tp-id");
    when(appProperties.getFqdn()).thenReturn("tp.example");

    TokenRequestDTO body = new TokenRequestDTO();
    body.setOrganizationId("org-1");
    body.setDocumentFormat("provn");
    body.setDocumentType(DocumentType.META);
    body.setDocument(base64Of(
        "document\n"
            + "prefix ex <http://example.org/>\n"
            + "bundle ex:b1\n"
            + "entity(ex:e1)\n"
            + "endBundle\n"
            + "endDocument"));
    LocalDateTime createdOn = LocalDateTime.of(2025, 1, 1, 10, 0);
    body.setCreatedOn(createdOn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    Token token = tokenService.issueTokenAndStoreDoc(body);

    Document doc = token.getDocument();

    SignedJWT signedJWT = SignedJWT.parse(token.getTokenValue());
    assertThat(signedJWT.verify(new ECDSAVerifier((ECPublicKey) keyPair.getPublic()))).isTrue();

    assertThat(signedJWT.getJWTClaimsSet().getClaimAsString("hash_alg")).isEqualTo(HashFunction.SHA256.getValue());
    assertThat(doc.getSignature()).isNull();
    assertThat(doc.getOrganization().getId()).isEqualTo("org-1");
    assertThat(doc.getCreatedOn()).isEqualTo(createdOn);
    assertThat(token.getTokenValue()).matches("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    String expectedHash = sha256Hex(Base64.getDecoder().decode(body.getDocument()));
    assertThat(signedJWT.getJWTClaimsSet().getClaimAsString("doc_digest")).isEqualTo(expectedHash);

    boolean verified = signedJWT.verify(new ECDSAVerifier((ECPublicKey) keyPair.getPublic()));
    assertThat(verified).isTrue();

    assertThat(signedJWT.getJWTClaimsSet().getIssuer()).isEqualTo("tp-id");
    assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("http://example.org/b1");
  }

  private TokenRequestDTO buildRequest(DocumentType documentType) {
    TokenRequestDTO body = TestDataFactory.tokenRequest();
    body.setDocumentType(documentType);
    body.setCreatedOn(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    return body;
  }

  private TokenRequestDTO buildRequestWithBundleId(DocumentType documentType, String bundleId) {
    TokenRequestDTO body = TestDataFactory.tokenRequest();
    body.setDocument(base64Of("document\nprefix ex <http://example.org/>\n"
        + "bundle ex:" + bundleId + "\nentity(ex:e1)\nendBundle\nendDocument"));
    body.setDocumentFormat("provn");
    body.setDocumentType(documentType);
    body.setCreatedOn(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    return body;
  }

  private String base64Of(String content) {
    return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
  }

  private static KeyPair generateEcKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(256, new SecureRandom());
    return generator.generateKeyPair();
  }

  private static X509Certificate createSelfSignedCertificate(KeyPair keyPair) throws Exception {
    long now = System.currentTimeMillis();
    Date notBefore = new Date(now - 60_000L);
    Date notAfter = new Date(now + 86_400_000L);
    BigInteger serial = new BigInteger(64, new SecureRandom());
    X500Name subject = new X500Name("CN=Test");

    JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
        subject,
        serial,
        notBefore,
        notAfter,
        subject,
        keyPair.getPublic());

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
        .build(keyPair.getPrivate());
    X509CertificateHolder holder = builder.build(signer);
    return new JcaX509CertificateConverter().getCertificate(holder);
  }

  private static String toPem(Object value) throws Exception {
    StringWriter writer = new StringWriter();
    try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
      pemWriter.writeObject(value);
    }
    return writer.toString();
  }

  private static String toPkcs8Pem(PrivateKey privateKey) {
    String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
        .encodeToString(privateKey.getEncoded());
    return "-----BEGIN PRIVATE KEY-----\n"
        + base64
        + "\n-----END PRIVATE KEY-----\n";
  }

  private static String sha256Hex(byte[] data) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashed = digest.digest(data);
    StringBuilder sb = new StringBuilder(hashed.length * 2);
    for (byte b : hashed) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

}
