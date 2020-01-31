package org.gusdb.fgputil.solr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

public class SolrResponse {

  private final int _totalCount;
  private final List<JSONObject> _documents;
  private final Map<String,Map<String,Integer>> _facetCounts;
  private final Map<String,List<String>> _highlighting;
  private final Optional<String> _nextCursorMark;

  public SolrResponse(
      int totalCount,
      List<JSONObject> documents,
      Map<String,Map<String, Integer>> facetCounts,
      Map<String, List<String>> highlighting,
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

  public Map<String,Map<String,Integer>> getFacetCounts() {
    return _facetCounts;
  }

  public Map<String, List<String>> getHighlighting() {
    return _highlighting;
  }

  public Optional<String> getNextCursorMark() {
    return _nextCursorMark;
  }
}
