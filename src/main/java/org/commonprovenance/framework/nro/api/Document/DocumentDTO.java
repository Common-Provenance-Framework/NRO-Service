package org.commonprovenance.framework.nro.api.Document;

import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.data.enums.DocumentType;

public class DocumentDTO {
  private String identifier;
  private String documentFormat;
  private String certDigest;
  private String organizationId;
  private DocumentType documentType;
  private String documentText;
  private LocalDateTime createdOn;
  private String signature;

  public DocumentDTO() {
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDocumentFormat() {
    return documentFormat;
  }

  public void setDocumentFormat(String documentFormat) {
    this.documentFormat = documentFormat;
  }

  public String getCertDigest() {
    return certDigest;
  }

  public void setCertDigest(String certDigest) {
    this.certDigest = certDigest;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }

  public String getDocumentText() {
    return documentText;
  }

  public void setDocumentText(String documentText) {
    this.documentText = documentText;
  }

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }
}
