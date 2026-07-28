package org.commonprovenance.framework.nro.api;

public class InfoResponseDTO {
  private String id;
  private String clientCertificate;

  public InfoResponseDTO(String id, String clientCertificate) {
    this.id = id;
    this.clientCertificate = clientCertificate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public void setClientCertificate(String certificate) {
    this.clientCertificate = certificate;
  }
}
