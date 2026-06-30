package org.commonprovenance.framework.nro.data.records;

import java.util.List;

import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;

public record OrganizationAndCertificates(
    Organization organization,
    Certificate activeCertificate,
    List<Certificate> revokedCertificates) {
}
