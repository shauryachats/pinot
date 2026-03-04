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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.segment.spi.index.reader.Dictionary;
import org.apache.pinot.spi.data.FieldSpec;
import org.roaringbitmap.PeekableIntIterator;
import org.roaringbitmap.RoaringBitmap;


/**
 * Extracts intermediate results from {@link HoldConstantDictIdsWrapper} into a
 * {@code Map<Object, List<RoaringBitmap>>} keyed by the actual hold-constant column value.
 *
 * <p>Each map entry contains per-step bitmaps of correlation column value hashes,
 * ready for cross-segment merging.
 */
class HoldConstantBitmapResultExtractionStrategy
    implements ResultExtractionStrategy<HoldConstantDictIdsWrapper, Map<Object, List<RoaringBitmap>>> {

  protected final int _numSteps;

  HoldConstantBitmapResultExtractionStrategy(int numSteps) {
    _numSteps = numSteps;
  }

  @Override
  public Map<Object, List<RoaringBitmap>> extractIntermediateResult(HoldConstantDictIdsWrapper wrapper) {
    if (wrapper == null) {
      return new HashMap<>();
    }

    Dictionary corrDict = wrapper._correlationDictionary;
    Dictionary holdDict = wrapper._holdConstantDictionary;
    Map<Object, List<RoaringBitmap>> result = new HashMap<>();

    for (Map.Entry<Integer, RoaringBitmap[]> entry : wrapper._holdGroupBitmaps.entrySet()) {
      Object holdValue = getActualValue(holdDict, entry.getKey());
      List<RoaringBitmap> stepBitmaps = new ArrayList<>(_numSteps);
      for (RoaringBitmap dictIdBitmap : entry.getValue()) {
        stepBitmaps.add(convertToValueBitmap(corrDict, dictIdBitmap));
      }
      result.put(holdValue, stepBitmaps);
    }
    return result;
  }

  private Object getActualValue(Dictionary dictionary, int dictId) {
    FieldSpec.DataType storedType = dictionary.getValueType();
    switch (storedType) {
      case INT:
        return dictionary.getIntValue(dictId);
      case LONG:
        return dictionary.getLongValue(dictId);
      case FLOAT:
        return dictionary.getFloatValue(dictId);
      case DOUBLE:
        return dictionary.getDoubleValue(dictId);
      case STRING:
        return dictionary.getStringValue(dictId);
      default:
        throw new IllegalArgumentException(
            "Unsupported HOLD_CONSTANT column data type: " + storedType);
    }
  }

  private RoaringBitmap convertToValueBitmap(Dictionary dictionary, RoaringBitmap dictIdBitmap) {
    RoaringBitmap valueBitmap = new RoaringBitmap();
    PeekableIntIterator iterator = dictIdBitmap.getIntIterator();
    FieldSpec.DataType storedType = dictionary.getValueType();
    switch (storedType) {
      case INT:
        while (iterator.hasNext()) {
          valueBitmap.add(dictionary.getIntValue(iterator.next()));
        }
        break;
      case LONG:
        while (iterator.hasNext()) {
          valueBitmap.add(Long.hashCode(dictionary.getLongValue(iterator.next())));
        }
        break;
      case FLOAT:
        while (iterator.hasNext()) {
          valueBitmap.add(Float.hashCode(dictionary.getFloatValue(iterator.next())));
        }
        break;
      case DOUBLE:
        while (iterator.hasNext()) {
          valueBitmap.add(Double.hashCode(dictionary.getDoubleValue(iterator.next())));
        }
        break;
      case STRING:
        while (iterator.hasNext()) {
          valueBitmap.add(dictionary.getStringValue(iterator.next()).hashCode());
        }
        break;
      default:
        throw new IllegalArgumentException(
            "Unsupported CORRELATE_BY column data type for FUNNEL_COUNT: " + storedType);
    }
    return valueBitmap;
  }
}
