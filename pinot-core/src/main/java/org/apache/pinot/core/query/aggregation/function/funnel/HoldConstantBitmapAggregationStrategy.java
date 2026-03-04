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
package org.apache.pinot.core.query.aggregation.function.funnel;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import org.apache.pinot.common.request.context.ExpressionContext;
import org.apache.pinot.core.common.BlockValSet;
import org.apache.pinot.core.query.aggregation.AggregationResultHolder;
import org.apache.pinot.core.query.aggregation.groupby.GroupByResultHolder;
import org.apache.pinot.segment.spi.index.reader.Dictionary;


/**
 * Bitmap aggregation strategy for HOLD_CONSTANT funnel count.
 *
 * <p>For each row, the correlation ID is added to the bitmap for the matching step,
 * partitioned by the hold-constant column value. This ensures that funnel progression
 * is only counted when the hold-constant column remains the same value across all steps.
 */
class HoldConstantBitmapAggregationStrategy extends AggregationStrategy<HoldConstantDictIdsWrapper> {
  private final ExpressionContext _holdConstantCol;

  HoldConstantBitmapAggregationStrategy(List<ExpressionContext> stepExpressions,
      List<ExpressionContext> correlateByExpressions, ExpressionContext holdConstantCol) {
    super(stepExpressions, correlateByExpressions);
    _holdConstantCol = holdConstantCol;
  }

  @Override
  HoldConstantDictIdsWrapper createAggregationResult(Dictionary dictionary) {
    return new HoldConstantDictIdsWrapper(_numSteps, dictionary);
  }

  @Override
  void add(Dictionary dictionary, HoldConstantDictIdsWrapper aggResult, int step, int correlationId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void aggregate(int length, AggregationResultHolder aggregationResultHolder,
      Map<ExpressionContext, BlockValSet> blockValSetMap) {
    final Dictionary correlationDict = getCorrelationDictionary(blockValSetMap);
    final int[] correlationIds = getCorrelationIds(blockValSetMap);
    final int[][] steps = getSteps(blockValSetMap);
    final Dictionary holdDict = getHoldConstantDictionary(blockValSetMap);
    final int[] holdIds = blockValSetMap.get(_holdConstantCol).getDictionaryIdsSV();

    final HoldConstantDictIdsWrapper aggResult =
        getAggregationResult(correlationDict, aggregationResultHolder);
    aggResult._holdConstantDictionary = holdDict;

    for (int i = 0; i < length; i++) {
      for (int n = 0; n < _numSteps; n++) {
        if (steps[n][i] > 0) {
          aggResult.add(holdIds[i], n, correlationIds[i]);
        }
      }
    }
  }

  @Override
  public void aggregateGroupBySV(int length, int[] groupKeyArray, GroupByResultHolder groupByResultHolder,
      Map<ExpressionContext, BlockValSet> blockValSetMap) {
    final Dictionary correlationDict = getCorrelationDictionary(blockValSetMap);
    final int[] correlationIds = getCorrelationIds(blockValSetMap);
    final int[][] steps = getSteps(blockValSetMap);
    final Dictionary holdDict = getHoldConstantDictionary(blockValSetMap);
    final int[] holdIds = blockValSetMap.get(_holdConstantCol).getDictionaryIdsSV();

    for (int i = 0; i < length; i++) {
      final int groupKey = groupKeyArray[i];
      final HoldConstantDictIdsWrapper aggResult =
          getAggregationResultGroupBy(correlationDict, groupByResultHolder, groupKey);
      aggResult._holdConstantDictionary = holdDict;
      for (int n = 0; n < _numSteps; n++) {
        if (steps[n][i] > 0) {
          aggResult.add(holdIds[i], n, correlationIds[i]);
        }
      }
    }
  }

  @Override
  public void aggregateGroupByMV(int length, int[][] groupKeysArray, GroupByResultHolder groupByResultHolder,
      Map<ExpressionContext, BlockValSet> blockValSetMap) {
    final Dictionary correlationDict = getCorrelationDictionary(blockValSetMap);
    final int[] correlationIds = getCorrelationIds(blockValSetMap);
    final int[][] steps = getSteps(blockValSetMap);
    final Dictionary holdDict = getHoldConstantDictionary(blockValSetMap);
    final int[] holdIds = blockValSetMap.get(_holdConstantCol).getDictionaryIdsSV();

    for (int i = 0; i < length; i++) {
      for (int groupKey : groupKeysArray[i]) {
        final HoldConstantDictIdsWrapper aggResult =
            getAggregationResultGroupBy(correlationDict, groupByResultHolder, groupKey);
        aggResult._holdConstantDictionary = holdDict;
        for (int n = 0; n < _numSteps; n++) {
          if (steps[n][i] > 0) {
            aggResult.add(holdIds[i], n, correlationIds[i]);
          }
        }
      }
    }
  }

  private Dictionary getHoldConstantDictionary(Map<ExpressionContext, BlockValSet> blockValSetMap) {
    final Dictionary holdDict = blockValSetMap.get(_holdConstantCol).getDictionary();
    Preconditions.checkArgument(holdDict != null,
        "HOLD_CONSTANT column in FUNNELCOUNT requires a dictionary encoded column.");
    return holdDict;
  }
}
