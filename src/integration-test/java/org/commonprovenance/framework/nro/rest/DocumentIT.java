package org.commonprovenance.framework.nro.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyExpectations.documentKeptFor;

import org.commonprovenance.framework.nro.support.IntegrationTest;
import org.commonprovenance.framework.nro.support.IntegrationTestData;
import org.commonprovenance.framework.nro.support.fixture.ProvDocumentData;
import org.commonprovenance.framework.nro.support.trustedParty.DocumentView;
import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Documents")
class DocumentIT {

  @Autowired
  private TrustedPartyClient trustedParty;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should return the copy of the document kept as evidence")
  void getDocument_shouldReturnDocumentKeptAsEvidence() {
    ProvDocumentData document = testData.storedDocument();

    var fetched = trustedParty.getDocument(document);

    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(documentKeptFor(document));
  }
}
