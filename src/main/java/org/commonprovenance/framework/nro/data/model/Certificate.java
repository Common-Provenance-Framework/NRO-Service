package org.commonprovenance.framework.nro.data.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Certificate {

  @Id
  @Column(length = 64)
  private String certDigest;

  @Column(columnDefinition = "TEXT")
  private String cert;

  public String getCertDigest() {
    return certDigest;
  }

  public void setCertDigest(String certDigest) {
    this.certDigest = certDigest;
  }

  public String getCert() {
    return cert;
  }

  public void setCert(String cert) {
    this.cert = cert;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Certificate that))
      return false;
    return Objects.equals(this.certDigest, that.certDigest)
        && Objects.equals(this.cert, that.cert);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.certDigest, this.cert);
  }

  @Override
  public String toString() {
    return "Certificate{" +
        "certDigest='" + this.certDigest + '\'' +
        ", cert='" + this.cert + '\'' +
        '}';
  }
}
