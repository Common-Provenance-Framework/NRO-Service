package org.commonprovenance.framework.nro.data.records;

import java.util.List;

import org.commonprovenance.framework.nro.data.model.Certificate;

public record SortedCertificates(
    Certificate activeCertificate,
    List<Certificate> revokedCertificates) {
}
