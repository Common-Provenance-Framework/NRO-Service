package org.commonprovenance.framework.nro.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.commonprovenance.framework.nro.support.fixture.OrganizationData.newOrganization;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.certificatesAfterRenewal;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.certificatesOf;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.viewOf;

import org.commonprovenance.framework.nro.support.IntegrationTest;
import org.commonprovenance.framework.nro.support.IntegrationTestData;
import org.commonprovenance.framework.nro.support.fixture.OrganizationData;
import org.commonprovenance.framework.nro.support.trustedParty.OrganizationView;
import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Organizations")
class OrganizationIT {

  @Autowired
  private TrustedPartyClient trustedParty;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should register the organization with a certificate chain issued by a trusted authority")
  void registerOrganization_shouldStoreOrganizationWithItsCertificates() {
    OrganizationData organization = newOrganization();

    var registered = trustedParty.registerOrganization(organization);
    var fetched = trustedParty.getOrganization(organization);

    assertThat(registered)
        .hasStatus(HttpStatus.CREATED)
        .body().isEmpty();
    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(viewOf(organization));
  }

  @Test
  @DisplayName("should list all registered organizations")
  void getAllOrganizations_shouldListRegisteredOrganizations() {
    OrganizationData first = testData.registeredOrganization();
    OrganizationData second = testData.registeredOrganization();

    var listed = trustedParty.getOrganizations();

    assertThat(listed)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(list(OrganizationView.class)).contains(viewOf(first), viewOf(second));
  }

  @Test
  @DisplayName("should return the certificates of the organization")
  void retrieveCertificates_shouldReturnCertificatesOfOrganization() {
    OrganizationData organization = testData.registeredOrganization();

    var certificates = trustedParty.getCertificates(organization);

    assertThat(certificates)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(certificatesOf(organization));
  }

  @Test
  @DisplayName("should replace the certificates of the organization and keep the previous ones as revoked")
  void updateCertificates_shouldReplaceCertificatesAndKeepPreviousAsRevoked() {
    OrganizationData organization = testData.registeredOrganization();
    OrganizationData renewed = organization.withRenewedCertificates();

    var updated = trustedParty.updateCertificates(renewed);
    var certificates = trustedParty.getCertificates(renewed);

    assertThat(updated)
        .hasStatus(HttpStatus.CREATED)
        .body().isEmpty();
    assertThat(certificates)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(certificatesAfterRenewal(renewed, organization));
  }
}
