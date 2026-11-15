package org.commonprovenance.framework.nro.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.commonprovenance.framework.nro.support.fixture.ProvDocumentData.newDocumentOf;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.tokenFor;

import org.commonprovenance.framework.nro.support.IntegrationTest;
import org.commonprovenance.framework.nro.support.IntegrationTestData;
import org.commonprovenance.framework.nro.support.fixture.OrganizationData;
import org.commonprovenance.framework.nro.support.fixture.ProvDocumentData;
import org.commonprovenance.framework.nro.support.trustedParty.TokenView;
import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Tokens")
class TokenIT {

  @Autowired
  private TrustedPartyClient trustedParty;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should issue a token for a signed document and keep it as evidence")
  void issueToken_shouldIssueTokenForSignedDocumentAndKeepIt() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentData document = newDocumentOf(organization);

    var issued = trustedParty.issueGraphToken(document);
    var tokens = trustedParty.getTokensOf(document);

    assertThat(issued)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(tokenFor(document));
    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).containsExactly(tokenFor(document));
  }

  @Test
  @DisplayName("should issue a token for the domain-specific part of a document without keeping the document")
  void issueToken_shouldIssueTokenForDomainSpecificPart() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentData document = newDocumentOf(organization);

    var issued = trustedParty.issueDomainSpecificToken(document);
    var tokens = trustedParty.getTokens(organization);

    assertThat(issued)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(tokenFor(document));
    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).isEmpty();
  }

  @Test
  @DisplayName("should issue a token for the backbone of a document without keeping the document")
  void issueToken_shouldIssueTokenForBackbone() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentData document = newDocumentOf(organization);

    var issued = trustedParty.issueBackboneToken(document);
    var tokens = trustedParty.getTokens(organization);

    assertThat(issued)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(tokenFor(document));
    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).isEmpty();
  }

  @Test
  @DisplayName("should issue a token for a meta document without keeping the document")
  void issueToken_shouldIssueTokenForMetaDocument() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentData document = newDocumentOf(organization);

    var issued = trustedParty.issueMetaToken(document);
    var tokens = trustedParty.getTokens(organization);

    assertThat(issued)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(tokenFor(document));
    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).isEmpty();
  }

  @Test
  @DisplayName("should confirm a signature made with the key of the organization certificate")
  void verifySignature_shouldConfirmValidSignature() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentData document = newDocumentOf(organization);

    var verified = trustedParty.verifySignature(document);

    assertThat(verified)
        .hasStatusOk()
        .body().isEmpty();
  }

  @Test
  @DisplayName("should return all tokens kept for the organization")
  void getAllTokens_shouldReturnTokensKeptForOrganization() {
    ProvDocumentData document = testData.storedDocument();

    var tokens = trustedParty.getTokens(document.organization());

    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).containsExactly(tokenFor(document));
  }

  @Test
  @DisplayName("should return the tokens kept for the document")
  void getToken_shouldReturnTokensKeptForDocument() {
    ProvDocumentData document = testData.storedDocument();

    var tokens = trustedParty.getTokensOf(document);

    assertThat(tokens)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(TokenView.class)).containsExactly(tokenFor(document));
  }
}
