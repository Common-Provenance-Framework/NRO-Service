package org.commonprovenance.framework.nro.support;

public final class TrustedPartyApi {
  public static final String INFO = "/api/v1/info";
  public static final String ORGANIZATIONS = "/api/v1/organizations";
  public static final String ORGANIZATION = ORGANIZATIONS + "/{organizationId}";
  public static final String CERTIFICATES = ORGANIZATION + "/certs";
  public static final String TOKENS = ORGANIZATION + "/tokens";
  public static final String DOCUMENT_TOKENS = TOKENS + "/{documentId}/{graphFormat}";
  public static final String DOCUMENT = ORGANIZATION + "/documents/{documentId}/{graphFormat}";
  public static final String ISSUE_TOKEN = "/api/v1/issueToken";
  public static final String VERIFY_SIGNATURE = "/api/v1/verifySignature";

  private TrustedPartyApi() {
  }
}
