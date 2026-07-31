package org.commonprovenance.framework.nro.api.Organization;

import java.util.List;

public class OrganizationDTO {
  private String id;
  private String clientCertificate;
  private List<String> revokedCertificates;

  public OrganizationDTO() {
  }

  public String getId() {
    return id;
  }

  public void setId(String organizationId) {
    this.id = organizationId;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public void setClientCertificate(String clientCertificate) {
    this.clientCertificate = clientCertificate;
  }

  public List<String> getRevokedCertificates() {
    return revokedCertificates;
  }

  public void setRevokedCertificates(List<String> revokedCertificates) {
    this.revokedCertificates = revokedCertificates;
  }
}
