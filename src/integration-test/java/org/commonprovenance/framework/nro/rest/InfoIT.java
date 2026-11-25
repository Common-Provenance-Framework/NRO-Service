package org.commonprovenance.framework.nro.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.trustedPartyInfo;

import org.commonprovenance.framework.nro.support.IntegrationTest;
import org.commonprovenance.framework.nro.support.trustedParty.InfoView;
import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Info")
class InfoIT {

  @Autowired
  private TrustedPartyClient trustedParty;

  @Test
  @DisplayName("should return the identity and certificate of the trusted party")
  void getInfo_shouldReturnTrustedPartyIdentityAndCertificate() {
    var info = trustedParty.getInfo();

    assertThat(info)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(InfoView.class).isEqualTo(trustedPartyInfo());
  }
}
