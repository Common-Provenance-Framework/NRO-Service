package org.commonprovenance.framework.nro.data.records;

import java.util.List;

import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;

public record OrganizationAndCertificates(
    Organization organization,
    OrganizationCertificate activeCertificate,
    List<OrganizationCertificate> revokedCertificates) {
}
