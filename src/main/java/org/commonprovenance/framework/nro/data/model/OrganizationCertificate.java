package org.commonprovenance.framework.nro.data.model;

import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.data.enums.CertificateType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "organization_id", "cert_digest" }))
public class OrganizationCertificate {

  public OrganizationCertificate() {
  }

  public OrganizationCertificate(
      Organization organization,
      Certificate certificate,
      CertificateType certificateType) {
    this.organization = organization;
    this.certificate = certificate;
    this.certificateType = certificateType;
    this.isRevoked = false;
    receivedOn = LocalDateTime.now();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "organization_id", referencedColumnName = "id", nullable = false)
  private Organization organization;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cert_digest", referencedColumnName = "certDigest", nullable = false)
  private Certificate certificate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CertificateType certificateType;

  @Column(nullable = false)
  private boolean isRevoked;

  @Column(nullable = false)
  private LocalDateTime receivedOn;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public Certificate getCertificate() {
    return certificate;
  }

  public void setCertificate(Certificate certificate) {
    this.certificate = certificate;
  }

  public CertificateType getCertificateType() {
    return certificateType;
  }

  public void setCertificateType(CertificateType certificateType) {
    this.certificateType = certificateType;
  }

  public boolean getIsRevoked() {
    return isRevoked;
  }

  public void setIsRevoked(boolean revoked) {
    isRevoked = revoked;
  }

  public LocalDateTime getReceivedOn() {
    return receivedOn;
  }

  public void setReceivedOn(LocalDateTime receivedOn) {
    this.receivedOn = receivedOn;
  }
}
