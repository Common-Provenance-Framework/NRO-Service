package org.commonprovenance.framework.nro.support.fixture;

import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

public record OrganizationData(String id, String clientCertificate, List<String> intermediateCertificates, PrivateKey privateKey) {

  public static OrganizationData newOrganization() {
    return withNewClientCertificate(UUID.randomUUID().toString());
  }

  public OrganizationData withRenewedCertificates() {
    return withNewClientCertificate(id);
  }

  private static OrganizationData withNewClientCertificate(String id) {
    Pki.ClientCertificate certificate = Pki.newClientCertificate(id);
    return new OrganizationData(id, certificate.pem(), Pki.intermediateCertificates(), certificate.privateKey());
  }
}
