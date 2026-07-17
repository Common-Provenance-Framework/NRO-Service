package org.commonprovenance.framework.nro.rest;

import org.commonprovenance.framework.nro.api.Document.DocumentDTO;
import org.commonprovenance.framework.nro.facade.DocumentFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Documents", description = "Actions for documents")
@RestController
@RequestMapping("/api/v1")
public class DocumentRestController {

  private final DocumentFacade documentFacade;

  public DocumentRestController(DocumentFacade documentFacade) {
    this.documentFacade = documentFacade;
  }

  @GetMapping("/organizations/{orgId}/documents/{docId}/{graphFormat}")
  public ResponseEntity<DocumentDTO> getDocument(
      @PathVariable @NonNull String orgId,
      @PathVariable String docId,
      @PathVariable String graphFormat) {
    DocumentDTO response = documentFacade.getDocument(orgId, docId, graphFormat);
    return ResponseEntity.ok(response);
  }
}
