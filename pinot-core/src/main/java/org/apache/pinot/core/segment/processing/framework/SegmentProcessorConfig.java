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
package org.apache.pinot.core.segment.processing.framework;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.apache.pinot.core.segment.processing.partitioner.PartitionerConfig;
import org.apache.pinot.core.segment.processing.timehandler.TimeHandler;
import org.apache.pinot.core.segment.processing.timehandler.TimeHandlerConfig;
import org.apache.pinot.segment.spi.AggregationFunctionType;
import org.apache.pinot.segment.spi.creator.name.SegmentNameGenerator;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.utils.TimestampIndexUtils;


/**
 * Config for configuring the phases of {@link SegmentProcessorFramework}
 */
public class SegmentProcessorConfig {
  private static final MergeType DEFAULT_MERGE_TYPE = MergeType.CONCAT;

  private final TableConfig _tableConfig;
  private final Schema _schema;
  private final TimeHandlerConfig _timeHandlerConfig;
  private final List<PartitionerConfig> _partitionerConfigs;
  private final MergeType _mergeType;
  private final Map<String, AggregationFunctionType> _aggregationTypes;
  private final Map<String, Map<String, String>> _aggregationFunctionParameters;
  private final SegmentConfig _segmentConfig;
  private final Consumer<Object> _progressObserver;
  private final SegmentNameGenerator _segmentNameGenerator;
  private final Long _customCreationTime;

  private SegmentProcessorConfig(TableConfig tableConfig, Schema schema, TimeHandlerConfig timeHandlerConfig,
      List<PartitionerConfig> partitionerConfigs, MergeType mergeType,
      Map<String, AggregationFunctionType> aggregationTypes,
      Map<String, Map<String, String>> aggregationFunctionParameters, SegmentConfig segmentConfig,
      Consumer<Object> progressObserver, @Nullable SegmentNameGenerator segmentNameGenerator,
      @Nullable Long customCreationTime) {
    TimestampIndexUtils.applyTimestampIndex(tableConfig, schema);
    _tableConfig = tableConfig;
    _schema = schema;
    _timeHandlerConfig = timeHandlerConfig;
    _partitionerConfigs = partitionerConfigs;
    _mergeType = mergeType;
    _aggregationTypes = aggregationTypes;
    _aggregationFunctionParameters = aggregationFunctionParameters;
    _segmentConfig = segmentConfig;
    _progressObserver = (progressObserver != null) ? progressObserver : p -> {
      // Do nothing.
    };
    _segmentNameGenerator = segmentNameGenerator;
    _customCreationTime = customCreationTime;
  }

  /**
   * The Pinot table config
   */
  public TableConfig getTableConfig() {
    return _tableConfig;
  }

  /**
   * The Pinot schema
   */
  public Schema getSchema() {
    return _schema;
  }

  /**
   * The time handler config for the SegmentProcessorFramework
   */
  public TimeHandlerConfig getTimeHandlerConfig() {
    return _timeHandlerConfig;
  }

  /**
   * The PartitioningConfig for the SegmentProcessorFramework's map phase
   */
  public List<PartitionerConfig> getPartitionerConfigs() {
    return _partitionerConfigs;
  }

  /**
   * The merge type for the SegmentProcessorFramework
   */
  public MergeType getMergeType() {
    return _mergeType;
  }

  /**
   * The aggregator types for the SegmentProcessorFramework's reduce phase with ROLLUP merge type
   */
  public Map<String, AggregationFunctionType> getAggregationTypes() {
    return _aggregationTypes;
  }

  /**
   * The aggregation function parameters for the SegmentProcessorFramework's reduce phase with ROLLUP merge type
   */
  public Map<String, Map<String, String>> getAggregationFunctionParameters() {
    return _aggregationFunctionParameters;
  }

  /**
   * The SegmentConfig for the SegmentProcessorFramework's reduce phase
   */
  public SegmentConfig getSegmentConfig() {
    return _segmentConfig;
  }

  public Consumer<Object> getProgressObserver() {
    return _progressObserver;
  }

  public SegmentNameGenerator getSegmentNameGenerator() {
    return _segmentNameGenerator;
  }

  public long getCustomCreationTime() {
    return _customCreationTime != null ? _customCreationTime : System.currentTimeMillis();
  }

  @Override
  public String toString() {
    return "SegmentProcessorConfig{" + "_tableConfig=" + _tableConfig + ", _schema=" + _schema + ", _timeHandlerConfig="
        + _timeHandlerConfig + ", _partitionerConfigs=" + _partitionerConfigs + ", _mergeType=" + _mergeType
        + ", _aggregationTypes=" + _aggregationTypes + ", _segmentConfig=" + _segmentConfig
        + ", _segmentNameGenerator=" + _segmentNameGenerator + ", _customCreationTime=" + _customCreationTime + '}';
  }

  /**
   * Builder for SegmentProcessorConfig
   */
  public static class Builder {
    private TableConfig _tableConfig;
    private Schema _schema;
    private TimeHandlerConfig _timeHandlerConfig;
    private List<PartitionerConfig> _partitionerConfigs;
    private MergeType _mergeType;
    private Map<String, AggregationFunctionType> _aggregationTypes;
    private Map<String, Map<String, String>> _aggregationFunctionParameters;
    private SegmentConfig _segmentConfig;
    private Consumer<Object> _progressObserver;
    private SegmentNameGenerator _segmentNameGenerator;
    private Long _customCreationTime;

    public Builder setTableConfig(TableConfig tableConfig) {
      _tableConfig = tableConfig;
      return this;
    }

    public Builder setSchema(Schema schema) {
      _schema = schema;
      return this;
    }

    public Builder setTimeHandlerConfig(TimeHandlerConfig timeHandlerConfig) {
      _timeHandlerConfig = timeHandlerConfig;
      return this;
    }

    public Builder setPartitionerConfigs(List<PartitionerConfig> partitionerConfigs) {
      _partitionerConfigs = partitionerConfigs;
      return this;
    }

    public Builder setMergeType(MergeType mergeType) {
      _mergeType = mergeType;
      return this;
    }

    public Builder setAggregationTypes(Map<String, AggregationFunctionType> aggregationTypes) {
      _aggregationTypes = aggregationTypes;
      return this;
    }

    public Builder setAggregationFunctionParameters(Map<String, Map<String, String>> aggregationFunctionParameters) {
      _aggregationFunctionParameters = aggregationFunctionParameters;
      return this;
    }

    public Builder setSegmentConfig(SegmentConfig segmentConfig) {
      _segmentConfig = segmentConfig;
      return this;
    }

    public Builder setProgressObserver(Consumer<Object> progressObserver) {
      _progressObserver = progressObserver;
      return this;
    }

    public Builder setSegmentNameGenerator(SegmentNameGenerator segmentNameGenerator) {
      _segmentNameGenerator = segmentNameGenerator;
      return this;
    }

    public Builder setCustomCreationTime(Long customCreationTime) {
      _customCreationTime = customCreationTime;
      return this;
    }

    public SegmentProcessorConfig build() {
      Preconditions.checkState(_tableConfig != null, "Must provide table config in SegmentProcessorConfig");
      Preconditions.checkState(_schema != null, "Must provide schema in SegmentProcessorConfig");

      if (_timeHandlerConfig == null) {
        _timeHandlerConfig = new TimeHandlerConfig.Builder(TimeHandler.Type.NO_OP).build();
      }
      if (_partitionerConfigs == null) {
        _partitionerConfigs = Collections.emptyList();
      }
      if (_mergeType == null) {
        _mergeType = DEFAULT_MERGE_TYPE;
      }
      if (_aggregationTypes == null) {
        _aggregationTypes = Collections.emptyMap();
      }
      if (_aggregationFunctionParameters == null) {
        _aggregationFunctionParameters = Collections.emptyMap();
      }
      if (_segmentConfig == null) {
        _segmentConfig = new SegmentConfig.Builder().build();
      }
      return new SegmentProcessorConfig(_tableConfig, _schema, _timeHandlerConfig, _partitionerConfigs, _mergeType,
          _aggregationTypes, _aggregationFunctionParameters, _segmentConfig, _progressObserver,
              _segmentNameGenerator, _customCreationTime);
    }
  }
}
