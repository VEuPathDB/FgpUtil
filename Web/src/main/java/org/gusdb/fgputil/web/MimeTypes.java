package org.gusdb.fgputil.web;

/**
 * Contains lesser-known MIME types that may not be available as constants elsewhere
 * 
 * @author rdoherty
 */
public class MimeTypes {

  // newline-delimited JSON for easy streaming of records
  public static final String ND_JSON = "application/x-ndjson";

  // tabular text
  public static final String TEXT_TABULAR = "text/tab-separated-values";
}
