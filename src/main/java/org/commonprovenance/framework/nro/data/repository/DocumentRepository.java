package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.enums.GraphType;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

  Optional<Document> findByIdAndGraphFormatAndOrganization(
      String id,
      String graphFormat,
      Organization organization);

  Optional<Document> findByIdAndGraphFormatAndGraphTypeAndOrganization(
      String id,
      String graphFormat,
      GraphType graphType,
      Organization organization);

  Optional<Document> findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
      String identifier,
      String graphFormat,
      GraphType graphType,
      Organization organization);

  List<Document> findByOrganization(Organization organization);
}
