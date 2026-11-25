package org.commonprovenance.framework.nro.support.trustedParty;

import java.time.LocalDateTime;

public record DocumentView(
    String id,
    String identifier,
    String graphFormat,
    String certDigest,
    String organizationId,
    String graphType,
    String graph,
    LocalDateTime createdOn,
    String signature) {
}
