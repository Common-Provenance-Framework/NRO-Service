package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, String> {
}
