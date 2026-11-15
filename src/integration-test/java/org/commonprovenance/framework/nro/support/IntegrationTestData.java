package org.commonprovenance.framework.nro.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonprovenance.framework.nro.support.fixture.OrganizationData;
import org.commonprovenance.framework.nro.support.fixture.ProvDocumentData;
import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.springframework.http.HttpStatus;

public class IntegrationTestData {
  private final TrustedPartyClient trustedParty;

  public IntegrationTestData(TrustedPartyClient trustedParty) {
    this.trustedParty = trustedParty;
  }

  public OrganizationData registeredOrganization() {
    OrganizationData organization = OrganizationData.newOrganization();
    assertThat(trustedParty.registerOrganization(organization)).hasStatus(HttpStatus.CREATED);
    return organization;
  }

  public ProvDocumentData storedDocument() {
    ProvDocumentData document = ProvDocumentData.newDocumentOf(registeredOrganization());
    assertThat(trustedParty.issueGraphToken(document)).hasStatus(HttpStatus.CREATED);
    return document;
  }
}
