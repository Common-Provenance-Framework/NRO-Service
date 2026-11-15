package org.commonprovenance.framework.nro.support.trustedParty;

import java.util.List;

import org.commonprovenance.framework.nro.support.fixture.OrganizationData;
import org.commonprovenance.framework.nro.support.fixture.Pki;
import org.commonprovenance.framework.nro.support.fixture.ProvDocumentData;
import org.commonprovenance.framework.nro.support.fixture.TrustedPartyFixture;

public final class TrustedPartyExpectations {

  private TrustedPartyExpectations() {
  }

  public static InfoView trustedPartyInfo() {
    return new InfoView(TrustedPartyFixture.ID, Pki.trustedPartyCertificate());
  }

  public static OrganizationView viewOf(OrganizationData organization) {
    return new OrganizationView(organization.id(), organization.clientCertificate(), null);
  }

  public static OrganizationView certificatesOf(OrganizationData organization) {
    return new OrganizationView(organization.id(), organization.clientCertificate(), List.of());
  }

  public static OrganizationView certificatesAfterRenewal(OrganizationData renewed, OrganizationData previous) {
    return new OrganizationView(renewed.id(), renewed.clientCertificate(), List.of(previous.clientCertificate()));
  }

  public static TokenView tokenFor(ProvDocumentData document) {
    return new TokenView(
        new TokenView.Header("JWT", "ES256", List.of(Pki.trustedPartyCertificateDer()), TrustedPartyFixture.FQDN),
        new TokenView.Claims(
            TrustedPartyFixture.ID,
            document.bundleIdentifier(),
            document.digest(),
            "SHA256",
            document.createdOnEpochSecond(),
            document.organization().id()),
        true);
  }

  public static DocumentView documentKeptFor(ProvDocumentData document) {
    return new DocumentView(
        document.bundleId(),
        document.bundleIdentifier(),
        ProvDocumentData.GRAPH_FORMAT,
        Pki.digestOf(document.organization().clientCertificate()),
        document.organization().id(),
        "GRAPH",
        document.base64(),
        ProvDocumentData.CREATED_ON,
        document.signature());
  }
}
