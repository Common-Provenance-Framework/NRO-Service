package org.commonprovenance.framework.nro.api.Token;

import org.commonprovenance.framework.nro.data.enums.GraphType;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TokenRequestDTO {
  @NotBlank(message = "organizationId is mandatory")
  @JsonAlias({ "organizationId", "originatorId" })
  @JsonProperty("organizationId")
  private String organizationId;

  /** Raw PROV graph Base64 OR JSON depending on format */
  @NotBlank(message = "graph is mandatory")
  @JsonAlias({ "graph" })
  @JsonProperty("graph")
  private String graph;

  @NotBlank(message = "graphFormat is mandatory")
  @JsonAlias({ "graphFormat", "doc_format" })
  private String graphFormat;

  private String signature;

  @NotNull(message = "Empty or incorrect type, must be one of [subgraph|meta|graph]!")
  @JsonAlias({ "type", "graphType" })
  private GraphType graphType;

  @NotBlank(message = "createdOn timestamp is mandatory")
  private String createdOn;

  public TokenRequestDTO() {
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getGraph() {
    return graph;
  }

  public void setGraph(String graph) {
    this.graph = graph;
  }

  public String getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(String createdOn) {
    this.createdOn = createdOn;
  }

  public String getGraphFormat() {
    return graphFormat;
  }

  public void setGraphFormat(String graphFormat) {
    this.graphFormat = graphFormat;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public GraphType getGraphType() {
    return graphType;
  }

  public void setGraphType(GraphType graphType) {
    this.graphType = graphType;
  }
}
