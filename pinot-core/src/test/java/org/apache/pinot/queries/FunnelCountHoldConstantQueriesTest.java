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
package org.apache.pinot.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.pinot.common.response.broker.BrokerResponseNative;
import org.apache.pinot.common.response.broker.ResultTable;
import org.apache.pinot.core.common.Operator;
import org.apache.pinot.core.operator.blocks.results.AggregationResultsBlock;
import org.apache.pinot.core.operator.query.AggregationOperator;
import org.apache.pinot.segment.local.indexsegment.mutable.MutableSegmentImplTestUtils;
import org.apache.pinot.segment.spi.IndexSegment;
import org.apache.pinot.segment.spi.MutableSegment;
import org.apache.pinot.spi.data.FieldSpec.DataType;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.data.readers.GenericRow;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * Integration test for FUNNEL_COUNT with HOLD_CONSTANT clause.
 *
 * <p>Scenario: E-commerce purchase funnel tracked across devices.
 * <pre>
 *   Steps: view_product -> add_to_cart -> checkout
 *   Hold constant: device (mobile / desktop)
 *
 *   Test data (deterministic):
 *   user_id=1: view_product on mobile,  add_to_cart on mobile   -> progresses on mobile
 *   user_id=2: view_product on mobile,  add_to_cart on desktop  -> blocked (switched device)
 *   user_id=3: view_product on desktop, add_to_cart on desktop  -> progresses on desktop
 *   user_id=4: view_product on mobile                           -> step 0 only
 *   user_id=5:                          add_to_cart on desktop  -> step 1 only (no step 0)
 *   user_id=6: view_product on mobile,  add_to_cart on mobile,  checkout on mobile  -> full funnel
 *   user_id=7: view_product on desktop, add_to_cart on desktop, checkout on mobile  -> blocked at step 2
 *
 *   Without HOLD_CONSTANT:
 *     step 0 (view_product):                             users {1,2,3,4,6,7} = 6
 *     step 1 (add_to_cart AND did step 0):               users {1,2,3,6,7}   = 5
 *     step 2 (checkout AND did step 0 AND step 1):       users {6,7}         = 2
 *
 *   With HOLD_CONSTANT(device):
 *     Mobile group:
 *       step 0: {1,2,4,6}  step 1: {1,6}  step 2: {6}
 *       intersect: step0=4, step1=intersect(step0,step1)={1,6}=2, step2=intersect(prev,step2)={6}=1
 *     Desktop group:
 *       step 0: {3,7}  step 1: {2,3,5,7}  step 2: {}
 *       intersect: step0=2, step1=intersect(step0,step1)={3,7}=2, step2=intersect(prev,step2)={}=0
 *     Union across groups:
 *       step 0: {1,2,3,4,6,7} = 6
 *       step 1: {1,3,6,7}     = 4  (user 2 excluded - switched devices!)
 *       step 2: {6}            = 1  (user 7 excluded - switched devices for checkout!)
 * </pre>
 */
public class FunnelCountHoldConstantQueriesTest extends BaseQueriesTest {
  private static final File INDEX_DIR =
      new File(FileUtils.getTempDirectory(), "FunnelCountHoldConstantTest");

  private static final String USER_ID = "user_id";
  private static final String STEP = "step";
  private static final String DEVICE = "device";

  private static final Schema SCHEMA = new Schema.SchemaBuilder()
      .addSingleValueDimension(USER_ID, DataType.INT)
      .addSingleValueDimension(STEP, DataType.STRING)
      .addSingleValueDimension(DEVICE, DataType.STRING)
      .build();

  private IndexSegment _indexSegment;
  private List<IndexSegment> _indexSegments;

  @Override
  protected String getFilter() {
    return "";
  }

  @Override
  protected IndexSegment getIndexSegment() {
    return _indexSegment;
  }

  @Override
  protected List<IndexSegment> getIndexSegments() {
    return _indexSegments;
  }

  @BeforeClass
  public void setUp()
      throws Exception {
    FileUtils.deleteDirectory(INDEX_DIR);
    List<GenericRow> records = buildTestData();
    MutableSegment segment = MutableSegmentImplTestUtils.createMutableSegmentImpl(SCHEMA);
    for (GenericRow record : records) {
      segment.index(record, null);
    }
    _indexSegment = segment;
    _indexSegments = Arrays.asList(_indexSegment, _indexSegment);
  }

  private List<GenericRow> buildTestData() {
    List<GenericRow> records = new ArrayList<>();
    // user 1: view_product on mobile, add_to_cart on mobile
    records.add(row(1, "view_product", "mobile"));
    records.add(row(1, "add_to_cart", "mobile"));
    // user 2: view_product on mobile, add_to_cart on desktop (device switch!)
    records.add(row(2, "view_product", "mobile"));
    records.add(row(2, "add_to_cart", "desktop"));
    // user 3: view_product on desktop, add_to_cart on desktop
    records.add(row(3, "view_product", "desktop"));
    records.add(row(3, "add_to_cart", "desktop"));
    // user 4: view_product on mobile (only step 0)
    records.add(row(4, "view_product", "mobile"));
    // user 5: add_to_cart on desktop (only step 1, no step 0)
    records.add(row(5, "add_to_cart", "desktop"));
    // user 6: full funnel on mobile
    records.add(row(6, "view_product", "mobile"));
    records.add(row(6, "add_to_cart", "mobile"));
    records.add(row(6, "checkout", "mobile"));
    // user 7: view+cart on desktop, checkout on mobile (device switch at checkout!)
    records.add(row(7, "view_product", "desktop"));
    records.add(row(7, "add_to_cart", "desktop"));
    records.add(row(7, "checkout", "mobile"));
    return records;
  }

  private GenericRow row(int userId, String step, String device) {
    GenericRow record = new GenericRow();
    record.putValue(USER_ID, userId);
    record.putValue(STEP, step);
    record.putValue(DEVICE, device);
    return record;
  }

  @Test
  public void testWithoutHoldConstant() {
    String query = "SELECT FUNNEL_COUNT("
        + "STEPS(step = 'view_product', step = 'add_to_cart', step = 'checkout'), "
        + "CORRELATE_BY(user_id), "
        + "SETTINGS('bitmap')"
        + ") FROM testTable";

    BrokerResponseNative response = getBrokerResponse(query);
    ResultTable resultTable = response.getResultTable();
    assertNotNull(resultTable);
    long[] counts = (long[]) resultTable.getRows().get(0)[0];

    // Without HOLD_CONSTANT: 6 viewed, 5 added to cart, 2 checked out
    // Inter-segment doubles the same segment, but bitmap deduplicates by value hash,
    // so unique counts remain the same.
    assertEquals(counts[0], 6L);
    assertEquals(counts[1], 5L);
    assertEquals(counts[2], 2L);
  }

  @Test
  public void testWithHoldConstant() {
    String query = "SELECT FUNNEL_COUNT("
        + "STEPS(step = 'view_product', step = 'add_to_cart', step = 'checkout'), "
        + "CORRELATE_BY(user_id), "
        + "HOLD_CONSTANT(device), "
        + "SETTINGS('bitmap')"
        + ") FROM testTable";

    BrokerResponseNative response = getBrokerResponse(query);
    ResultTable resultTable = response.getResultTable();
    assertNotNull(resultTable);
    long[] counts = (long[]) resultTable.getRows().get(0)[0];

    // With HOLD_CONSTANT(device):
    //   step 0: 6 (same - all who viewed)
    //   step 1: 4 (user 2 excluded because they switched from mobile to desktop)
    //   step 2: 1 (user 7 excluded because they switched from desktop to mobile for checkout)
    assertEquals(counts[0], 6L);
    assertEquals(counts[1], 4L);
    assertEquals(counts[2], 1L);
  }

  @Test
  public void testHoldConstantInnerSegment() {
    String query = "SELECT FUNNEL_COUNT("
        + "STEPS(step = 'view_product', step = 'add_to_cart', step = 'checkout'), "
        + "CORRELATE_BY(user_id), "
        + "HOLD_CONSTANT(device), "
        + "SETTINGS('bitmap')"
        + ") FROM testTable";

    Operator operator = getOperator(query);
    assertTrue(operator instanceof AggregationOperator);
    AggregationResultsBlock resultsBlock = ((AggregationOperator) operator).nextBlock();
    List<Object> aggregationResult = resultsBlock.getResults();
    assertNotNull(aggregationResult);
    assertEquals(aggregationResult.size(), 1);

    // Inner segment produces intermediate Map<Object, List<RoaringBitmap>>
    Object intermediateResult = aggregationResult.get(0);
    assertNotNull(intermediateResult);
    assertTrue(intermediateResult instanceof Map);
  }

  @Test
  public void testHoldConstantTwoSteps() {
    String query = "SELECT FUNNEL_COUNT("
        + "STEPS(step = 'view_product', step = 'add_to_cart'), "
        + "CORRELATE_BY(user_id), "
        + "HOLD_CONSTANT(device), "
        + "SETTINGS('bitmap')"
        + ") FROM testTable";

    BrokerResponseNative response = getBrokerResponse(query);
    ResultTable resultTable = response.getResultTable();
    assertNotNull(resultTable);
    long[] counts = (long[]) resultTable.getRows().get(0)[0];

    // step 0: 6 users viewed
    // step 1: 4 users added to cart on the same device they viewed on
    assertEquals(counts[0], 6L);
    assertEquals(counts[1], 4L);
  }

  @AfterClass
  public void tearDown()
      throws IOException {
    _indexSegment.destroy();
    FileUtils.deleteDirectory(INDEX_DIR);
  }
}
