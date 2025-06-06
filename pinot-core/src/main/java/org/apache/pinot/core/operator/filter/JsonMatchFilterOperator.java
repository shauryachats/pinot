/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.operator.filter;

import com.google.common.base.CaseFormat;
import java.util.Collections;
import java.util.List;
import org.apache.pinot.common.request.context.FilterContext;
import org.apache.pinot.common.request.context.predicate.JsonMatchPredicate;
import org.apache.pinot.core.common.BlockDocIdSet;
import org.apache.pinot.core.common.Operator;
import org.apache.pinot.core.operator.ExplainAttributeBuilder;
import org.apache.pinot.core.operator.docidsets.BitmapDocIdSet;
import org.apache.pinot.segment.spi.index.reader.JsonIndexReader;
import org.apache.pinot.spi.trace.FilterType;
import org.apache.pinot.spi.trace.InvocationRecording;
import org.apache.pinot.spi.trace.Tracing;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;


/**
 * Filter operator for JSON_MATCH. E.g. SELECT ... WHERE JSON_MATCH(column_name, filter_string)
 */
public class JsonMatchFilterOperator extends BaseFilterOperator {
  private static final String EXPLAIN_NAME = "FILTER_JSON_INDEX";

  private final JsonIndexReader _jsonIndex;
  private final JsonMatchPredicate _predicate;
  private final FilterContext _filterContext;

  /**
   * Constructor that takes a Json Predicate
   */
  public JsonMatchFilterOperator(JsonIndexReader jsonIndex, JsonMatchPredicate predicate, int numDocs) {
    super(numDocs, false);
    _jsonIndex = jsonIndex;
    _predicate = predicate;
    _filterContext = null;
  }

  /**
   * Constructor that takes a FilterContext
   */
  public JsonMatchFilterOperator(JsonIndexReader jsonIndex, FilterContext filterContext, int numDocs) {
    super(numDocs, false);
    _jsonIndex = jsonIndex;
    _filterContext = filterContext;
    _predicate = null;
  }

  @Override
  protected BlockDocIdSet getTrues() {
    ImmutableRoaringBitmap bitmap = getMatchingDocIdBitmap();
    record(bitmap);
    return new BitmapDocIdSet(bitmap, _numDocs);
  }

  @Override
  public boolean canOptimizeCount() {
    return true;
  }

  @Override
  public int getNumMatchingDocs() {
    return getMatchingDocIdBitmap().getCardinality();
  }

  @Override
  public boolean canProduceBitmaps() {
    return true;
  }

  @Override
  public BitmapCollection getBitmaps() {
    ImmutableRoaringBitmap bitmap = getMatchingDocIdBitmap();
    return new BitmapCollection(_numDocs, false, bitmap);
  }

  @Override
  public List<Operator> getChildOperators() {
    return Collections.emptyList();
  }

  @Override
  public String toExplainString() {
    StringBuilder stringBuilder = new StringBuilder(EXPLAIN_NAME).append("(indexLookUp:json_index");
    stringBuilder.append(",operator:").append(_predicate.getType());
    stringBuilder.append(",predicate:").append(_predicate);
    return stringBuilder.append(')').toString();
  }

  @Override
  protected String getExplainName() {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, EXPLAIN_NAME);
  }

  @Override
  protected void explainAttributes(ExplainAttributeBuilder attributeBuilder) {
    super.explainAttributes(attributeBuilder);
    attributeBuilder.putString("indexLookUp", "json_index");
    attributeBuilder.putString("operator", _predicate.getType().name());
    attributeBuilder.putString("predicate", _predicate.toString());
  }

  private void record(ImmutableRoaringBitmap bitmap) {
    InvocationRecording recording = Tracing.activeRecording();
    if (recording.isEnabled()) {
      recording.setColumnName(_predicate.getLhs().getIdentifier());
      recording.setFilter(FilterType.INDEX, _predicate.getType().name());
      recording.setNumDocsMatchingAfterFilter(bitmap.getCardinality());
    }
  }

  private ImmutableRoaringBitmap getMatchingDocIdBitmap() {
    if (_predicate != null) {
      return _jsonIndex.getMatchingDocIds(_predicate.getValue(), _predicate.getCountPredicate());
    } else {
      return _jsonIndex.getMatchingDocIds(_filterContext);
    }
  }
}
