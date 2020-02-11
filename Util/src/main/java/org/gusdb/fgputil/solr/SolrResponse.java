package org.gusdb.fgputil.solr;

import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.solr.Solr.FacetCounts;
import org.gusdb.fgputil.solr.Solr.Highlighting;
import org.json.JSONObject;

public class SolrResponse {

  private final int _totalCount;
  private final List<JSONObject> _documents;
  private final FacetCounts _facetCounts;
  private final Highlighting _highlighting;
  private final Optional<String> _nextCursorMark;

  public SolrResponse(
      int totalCount,
      List<JSONObject> documents,
      FacetCounts facetCounts,
      Highlighting highlighting,
      Optional<String> nextCursorMark) {
    _totalCount = totalCount;
    _documents = documents;
    _facetCounts = facetCounts;
    _highlighting = highlighting;
    _nextCursorMark = nextCursorMark;
  }

  public int getTotalCount() {
    return _totalCount;
  }

  public List<JSONObject> getDocuments() {
    return _documents;
  }

  public FacetCounts getFacetCounts() {
    return _facetCounts;
  }

  public Highlighting getHighlighting() {
    return _highlighting;
  }

  public Optional<String> getNextCursorMark() {
    return _nextCursorMark;
  }
}
