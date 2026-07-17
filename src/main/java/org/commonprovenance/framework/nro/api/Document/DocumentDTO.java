package org.commonprovenance.framework.nro.api.Document;

import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.data.enums.GraphType;

public class DocumentDTO {
  private String id;
  private String identifier;
  private String graphFormat;
  private String certDigest;
  private String organizationId;
  private GraphType graphType;
  private String graph;
  private LocalDateTime createdOn;
  private String signature;

  public DocumentDTO() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getGraphFormat() {
    return graphFormat;
  }

  public void setGraphFormat(String graphFormat) {
    this.graphFormat = graphFormat;
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

  public GraphType getGraphType() {
    return graphType;
  }

  public void setGraphType(GraphType graphType) {
    this.graphType = graphType;
  }

  public String getGraph() {
    return graph;
  }

  public void setGraph(String graph) {
    this.graph = graph;
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
