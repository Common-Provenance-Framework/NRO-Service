package org.commonprovenance.framework.nro.support.trustedParty;

import java.util.List;

import org.commonprovenance.framework.nro.support.TrustedPartyApi;
import org.commonprovenance.framework.nro.support.fixture.OrganizationData;
import org.commonprovenance.framework.nro.support.fixture.ProvDocumentData;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class TrustedPartyClient {

  private static final JsonMapper MAPPER = JsonMapper.builder()
      .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  private final MockMvcTester mvc;

  public TrustedPartyClient(MockMvcTester mvc) {
    this.mvc = mvc.withHttpMessageConverters(List.of(new JacksonJsonHttpMessageConverter(MAPPER)));
  }

  public MvcTestResult getInfo() {
    return send(HttpMethod.GET, null, TrustedPartyApi.INFO);
  }

  public MvcTestResult registerOrganization(OrganizationData organization) {
    return send(HttpMethod.POST, certificatesOf(organization), TrustedPartyApi.ORGANIZATION, organization.id());
  }

  public MvcTestResult getOrganization(OrganizationData organization) {
    return send(HttpMethod.GET, null, TrustedPartyApi.ORGANIZATION, organization.id());
  }

  public MvcTestResult getOrganizations() {
    return send(HttpMethod.GET, null, TrustedPartyApi.ORGANIZATIONS);
  }

  public MvcTestResult getCertificates(OrganizationData organization) {
    return send(HttpMethod.GET, null, TrustedPartyApi.CERTIFICATES, organization.id());
  }

  public MvcTestResult updateCertificates(OrganizationData organization) {
    return send(HttpMethod.PUT, certificatesOf(organization), TrustedPartyApi.CERTIFICATES, organization.id());
  }

  public MvcTestResult issueGraphToken(ProvDocumentData document) {
    return send(HttpMethod.POST, tokenRequest(document, document.signature(), "GRAPH"), TrustedPartyApi.ISSUE_TOKEN);
  }

  public MvcTestResult issueDomainSpecificToken(ProvDocumentData document) {
    return send(HttpMethod.POST, tokenRequest(document, null, "DOMAIN_SPECIFIC"), TrustedPartyApi.ISSUE_TOKEN);
  }

  public MvcTestResult issueBackboneToken(ProvDocumentData document) {
    return send(HttpMethod.POST, tokenRequest(document, null, "BACKBONE"), TrustedPartyApi.ISSUE_TOKEN);
  }

  public MvcTestResult issueMetaToken(ProvDocumentData document) {
    return send(HttpMethod.POST, tokenRequest(document, null, "META"), TrustedPartyApi.ISSUE_TOKEN);
  }

  public MvcTestResult verifySignature(ProvDocumentData document) {
    return send(HttpMethod.POST, tokenRequest(document, document.signature(), "GRAPH"), TrustedPartyApi.VERIFY_SIGNATURE);
  }

  public MvcTestResult getTokens(OrganizationData organization) {
    return send(HttpMethod.GET, null, TrustedPartyApi.TOKENS, organization.id());
  }

  public MvcTestResult getTokensOf(ProvDocumentData document) {
    return send(HttpMethod.GET, null, TrustedPartyApi.DOCUMENT_TOKENS, document.organization().id(), document.bundleId(), ProvDocumentData.GRAPH_FORMAT);
  }

  public MvcTestResult getDocument(ProvDocumentData document) {
    return send(HttpMethod.GET, null, TrustedPartyApi.DOCUMENT, document.organization().id(), document.bundleId(), ProvDocumentData.GRAPH_FORMAT);
  }

  private MvcTestResult send(HttpMethod method, Object body, String path, Object... uriVariables) {
    MockMvcTester.MockMvcRequestBuilder request = mvc.method(method).uri(path, uriVariables);
    return body == null
        ? request.exchange()
        : request.contentType(MediaType.APPLICATION_JSON).content(MAPPER.writeValueAsString(body)).exchange();
  }

  private static CertificatesRequest certificatesOf(OrganizationData organization) {
    return new CertificatesRequest(organization.id(), organization.clientCertificate(), organization.intermediateCertificates());
  }

  private static TokenRequest tokenRequest(ProvDocumentData document, String signature, String graphType) {
    return new TokenRequest(document.organization().id(), document.base64(), ProvDocumentData.GRAPH_FORMAT, signature, graphType, document.createdOn());
  }

  private record CertificatesRequest(String id, String clientCertificate, List<String> intermediateCertificates) {
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private record TokenRequest(String organizationId, String graph, String graphFormat, String signature, String graphType, String createdOn) {
  }
}
