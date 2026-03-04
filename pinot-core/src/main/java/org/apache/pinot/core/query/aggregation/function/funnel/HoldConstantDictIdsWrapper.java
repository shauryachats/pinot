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

import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.segment.spi.index.reader.Dictionary;
import org.roaringbitmap.RoaringBitmap;


/**
 * Aggregation result wrapper for HOLD_CONSTANT funnel count.
 * Groups correlation ID bitmaps by hold-constant column dictionary ID, so that
 * funnel progression is tracked independently within each hold-constant value group.
 */
final class HoldConstantDictIdsWrapper {
  final Dictionary _correlationDictionary;
  Dictionary _holdConstantDictionary;
  final int _numSteps;
  final Map<Integer, RoaringBitmap[]> _holdGroupBitmaps;

  HoldConstantDictIdsWrapper(int numSteps, Dictionary correlationDictionary) {
    _numSteps = numSteps;
    _correlationDictionary = correlationDictionary;
    _holdGroupBitmaps = new HashMap<>();
  }

  void add(int holdConstantDictId, int step, int correlationDictId) {
    RoaringBitmap[] stepBitmaps = _holdGroupBitmaps.computeIfAbsent(holdConstantDictId, k -> {
      RoaringBitmap[] bitmaps = new RoaringBitmap[_numSteps];
      for (int i = 0; i < _numSteps; i++) {
        bitmaps[i] = new RoaringBitmap();
      }
      return bitmaps;
    });
    stepBitmaps[step].add(correlationDictId);
  }
}
