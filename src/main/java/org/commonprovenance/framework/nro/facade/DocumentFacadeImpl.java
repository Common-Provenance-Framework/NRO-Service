package org.commonprovenance.framework.nro.facade;

import org.commonprovenance.framework.nro.api.Document.DocumentDTO;
import org.commonprovenance.framework.nro.mappers.DocumentMapper;
import org.commonprovenance.framework.nro.service.DocumentService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class DocumentFacadeImpl implements DocumentFacade {

  private final DocumentService documentService;
  private final DocumentMapper documentMapper;

  public DocumentFacadeImpl(DocumentService documentService, DocumentMapper documentMapper) {
    this.documentService = documentService;
    this.documentMapper = documentMapper;
  }

  @Override
  public DocumentDTO getDocument(
      @NonNull String organizationId,
      String documentId,
      String documentFormat) {
    return documentMapper.mapToDTO(documentService.getDocument(organizationId, documentId, documentFormat));
  }
}
