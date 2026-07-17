package org.commonprovenance.framework.nro.data.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GraphType {
  GRAPH,
  DOMAIN_SPECIFIC,
  BACKBONE,
  META;

  @JsonCreator
  public static GraphType fromValue(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim()
        .replace('-', '_')
        .toUpperCase();

    return GraphType.valueOf(normalized);
  }
}
