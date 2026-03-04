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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.roaringbitmap.RoaringBitmap;


/**
 * Merge strategy for HOLD_CONSTANT funnel count using bitmaps.
 *
 * <p>Intermediate results are maps from hold-constant value to per-step bitmaps.
 * Merging unions bitmaps within the same hold-constant group across segments.
 * Final extraction performs progressive intersection within each hold-constant group,
 * then unions results across groups for each step.
 */
class HoldConstantBitmapMergeStrategy implements MergeStrategy<Map<Object, List<RoaringBitmap>>> {
  protected final int _numSteps;

  HoldConstantBitmapMergeStrategy(int numSteps) {
    _numSteps = numSteps;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Object, List<RoaringBitmap>> merge(
      Map<Object, List<RoaringBitmap>> result1,
      Map<Object, List<RoaringBitmap>> result2) {
    for (Map.Entry<Object, List<RoaringBitmap>> entry : result2.entrySet()) {
      Object holdValue = entry.getKey();
      List<RoaringBitmap> stepBitmaps2 = entry.getValue();
      List<RoaringBitmap> stepBitmaps1 = result1.get(holdValue);
      if (stepBitmaps1 == null) {
        result1.put(holdValue, stepBitmaps2);
      } else {
        for (int i = 0; i < _numSteps; i++) {
          stepBitmaps1.get(i).or(stepBitmaps2.get(i));
        }
      }
    }
    return result1;
  }

  @Override
  public LongArrayList extractFinalResult(Map<Object, List<RoaringBitmap>> holdGroupBitmaps) {
    RoaringBitmap[] unionStepBitmaps = new RoaringBitmap[_numSteps];
    for (int i = 0; i < _numSteps; i++) {
      unionStepBitmaps[i] = new RoaringBitmap();
    }

    for (List<RoaringBitmap> stepBitmaps : holdGroupBitmaps.values()) {
      List<RoaringBitmap> cloned = new ArrayList<>(_numSteps);
      for (RoaringBitmap bm : stepBitmaps) {
        cloned.add(bm.clone());
      }

      // Progressive intersection within this hold-constant group:
      // step[i] = entities that completed steps 0..i with this hold-constant value
      for (int i = 1; i < _numSteps; i++) {
        cloned.get(i).and(cloned.get(i - 1));
      }

      // Union with results from other hold-constant groups
      for (int i = 0; i < _numSteps; i++) {
        unionStepBitmaps[i].or(cloned.get(i));
      }
    }

    long[] result = new long[_numSteps];
    for (int i = 0; i < _numSteps; i++) {
      result[i] = unionStepBitmaps[i].getCardinality();
    }
    return LongArrayList.wrap(result);
  }
}
