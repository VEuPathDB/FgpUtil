package org.gusdb.fgputil.solr;

public class SolrRuntimeException extends RuntimeException {

  public SolrRuntimeException(String message) {
    super(message);
  }

  public SolrRuntimeException(String message, Exception cause) {
    super(message, cause);
  }

}
