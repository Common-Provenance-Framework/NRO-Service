package org.commonprovenance.framework.nro.api;

public class TokenResponseDTO {
  private String jwt;

  public TokenResponseDTO() {
  }

  public String getJwt() {
    return jwt;
  }

  public void setJwt(String tokenValue) {
    this.jwt = tokenValue;
  }
}
