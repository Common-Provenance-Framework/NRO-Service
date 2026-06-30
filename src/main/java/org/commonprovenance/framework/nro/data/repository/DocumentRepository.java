package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.enums.DocumentType;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

  Optional<Document> findByIdentifierAndDocFormatAndOrganization(
      String identifier,
      String docFormat,
      Organization organization);

  Optional<Document> findByIdentifierAndDocFormatAndDocumentTypeAndOrganization(
      String identifier,
      String docFormat,
      DocumentType documentType,
      Organization organization);

  List<Document> findByOrganization(Organization organization);
}
