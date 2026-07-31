package org.commonprovenance.framework.nro.data.model;

import jakarta.persistence.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import org.commonprovenance.framework.nro.data.enums.GraphType;

@Entity
public class Document {

  @Id
  private String id;

  private String identifier;

  private String graphFormat;

  @ManyToOne
  @JoinColumn(name = "certificate", referencedColumnName = "certDigest")
  private Certificate certificate;

  @ManyToOne
  @JoinColumn(name = "organization", referencedColumnName = "id")
  private Organization organization;

  @Enumerated(EnumType.STRING)
  private GraphType graphType;

  @Column(columnDefinition = "bytea")
  @Basic(fetch = FetchType.LAZY)
  private byte[] graph;

  private LocalDateTime createdOn;

  @Column(columnDefinition = "bytea")
  @Basic(fetch = FetchType.LAZY)
  private byte[] signature;

  public String getSignature() {
    if (signature == null)
      return null;

    return new String(signature, StandardCharsets.UTF_8);
  }

  public void setSignature(String signature) {
    if (signature == null)
      this.signature = null;
    else
      this.signature = signature.getBytes(StandardCharsets.UTF_8);
  }

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public String getGraph() {
    if (graph == null)
      return null;

    return new String(graph, StandardCharsets.UTF_8);
  }

  public void setGraph(String graph) {
    if (graph == null)
      this.graph = null;
    else
      this.graph = graph.getBytes(StandardCharsets.UTF_8);
  }

  public GraphType getGraphType() {
    return graphType;
  }

  public void setGraphType(GraphType graphType) {
    this.graphType = graphType;
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

  public String getGraphFormat() {
    return graphFormat;
  }

  public void setGraphFormat(String graphFormat) {
    this.graphFormat = graphFormat;
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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Document document))
      return false;
    return Objects.equals(id, document.id)
        && Objects.equals(identifier, document.identifier)
        && Objects.equals(graphFormat, document.graphFormat)
        && Objects.equals(certificate, document.certificate)
        && Objects.equals(organization, document.organization)
        && graphType == document.graphType
        && Objects.equals(graph, document.graph)
        && Objects.equals(createdOn, document.createdOn)
        && Objects.equals(signature, document.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, identifier, graphFormat, certificate, organization, graphType, graph, createdOn, signature);
  }

  @Override
  public String toString() {
    return "Document{" +
        "id='" + id + '\'' +
        "identifier='" + identifier + '\'' +
        ", graphFormat='" + graphFormat + '\'' +
        ", certificate=" + certificate +
        ", organization=" + organization +
        ", graphType=" + graphType +
        ", graph='" + graph + '\'' +
        ", createdOn=" + createdOn +
        ", signature='" + signature + '\'' +
        '}';
  }
}
