package org.commonprovenance.framework.nro.service;

import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.repository.DocumentRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationRepository;
import org.commonprovenance.framework.nro.exceptions.DocumentNotFoundException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final OrganizationRepository organizationRepository;

  public DocumentService(DocumentRepository documentRepository, OrganizationRepository organizationRepository) {
    this.documentRepository = documentRepository;
    this.organizationRepository = organizationRepository;
  }

  public Document getDocument(
      @NonNull String organizationName,
      String documentId,
      String documentFormat) {
    Organization organization = organizationRepository
        .findById(organizationName)
        .orElseThrow(() -> new OrganizationNotFoundException(organizationName));

    return documentRepository
        .findByIdentifierAndDocFormatAndOrganization(documentId, documentFormat, organization)
        .orElseThrow(() -> new DocumentNotFoundException(
            "No document with id " + documentId
                + " in format " + documentFormat
                + "exists for organization " + organizationName));
  }
}
