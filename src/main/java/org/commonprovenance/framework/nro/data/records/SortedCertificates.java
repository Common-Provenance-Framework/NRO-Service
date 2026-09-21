package org.commonprovenance.framework.nro.data.records;

import java.util.List;

import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;

public record SortedCertificates(
    OrganizationCertificate activeCertificate,
    List<OrganizationCertificate> revokedCertificates) {
}
