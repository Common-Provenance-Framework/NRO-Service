package org.commonprovenance.framework.nro.support.trustedParty;

import java.util.List;

public record OrganizationView(String id, String clientCertificate, List<String> revokedCertificates) {
}
