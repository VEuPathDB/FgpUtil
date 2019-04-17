package org.gusdb.fgputil.db.stream;

import org.gusdb.fgputil.FormatUtil;

public class ResultSetToNdJsonConverter extends ResultSetToJsonConverter {

  private static final byte[] EMPTY_BYTES = new byte[0];
  private static final byte[] NEWLINE_BYTES = FormatUtil.NL.getBytes();

  @Override public byte[] getHeader()       { return EMPTY_BYTES; }
  @Override public byte[] getRowDelimiter() { return NEWLINE_BYTES; }
  @Override public byte[] getFooter()       { return EMPTY_BYTES; }

}
