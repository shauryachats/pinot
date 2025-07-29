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
package org.apache.pinot.integration.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.commons.io.FileUtils;
import org.apache.helix.zookeeper.datamodel.ZNRecord;
import org.apache.helix.zookeeper.datamodel.serializer.ZNRecordSerializer;
import org.apache.helix.zookeeper.impl.client.ZkClient;
import org.apache.http.HttpStatus;
import org.apache.pinot.broker.broker.helix.BaseBrokerStarter;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.JsonAsyncHttpPinotClientTransportFactory;
import org.apache.pinot.client.PinotClientTransport;
import org.apache.pinot.common.utils.FileUploadDownloadClient;
import org.apache.pinot.common.utils.ZkStarter;
import org.apache.pinot.controller.BaseControllerStarter;
import org.apache.pinot.controller.ControllerConf;
import org.apache.pinot.controller.helix.ControllerTest;
import org.apache.pinot.server.starter.helix.BaseServerStarter;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.config.table.TableType;
import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.env.PinotConfiguration;
import org.apache.pinot.spi.utils.CommonConstants;
import org.apache.pinot.spi.utils.CommonConstants.Broker;
import org.apache.pinot.spi.utils.CommonConstants.Helix;
import org.apache.pinot.spi.utils.CommonConstants.Server;
import org.apache.pinot.spi.utils.JsonUtils;
import org.apache.pinot.spi.utils.NetUtils;
import org.apache.pinot.spi.utils.builder.TableConfigBuilder;
import org.apache.pinot.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test that starts two completely isolated Pinot clusters.
 * Each cluster has its own Zookeeper, Controller, Broker, and Server.
 * The clusters are completely isolated and can operate independently.
 */
public class DualIsolatedClusterIntegrationTest extends ClusterTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DualIsolatedClusterIntegrationTest.class);

  // Constants
  private static final String SCHEMA_FILE = "On_Time_On_Time_Performance_2014_100k_subset_nonulls.schema";
  private static final String TIME_COLUMN = "DaysSinceEpoch";
  private static final String FEDERATION_TABLE = "federation_test_table";
  private static final String FEDERATION_TABLE_2 = "federation_test_table_2";
  private static final String STRING_COLUMN = "OriginCityName";
  private static final String JOIN_COLUMN = "OriginCityName";
  private static final int CLUSTER_1_SIZE = 1000;
  private static final int CLUSTER_2_SIZE = 1000;
  private static final int SEGMENTS_PER_CLUSTER = 3;
  private static final String CLUSTER_1_PREFIX = "cluster1_OriginCityName_";
  private static final String CLUSTER_2_PREFIX = "cluster2_OriginCityName_";

  // Cluster configurations
  private static final ClusterConfig CLUSTER_1_CONFIG = new ClusterConfig("DualIsolatedCluster1", 30000);
  private static final ClusterConfig CLUSTER_2_CONFIG = new ClusterConfig("DualIsolatedCluster2", 40000);

  // Cluster components
  private ClusterComponents _cluster1;
  private ClusterComponents _cluster2;

  // Test data
  private List<File> _cluster1AvroFiles;
  private List<File> _cluster2AvroFiles;
  private List<File> _cluster1AvroFiles2;
  private List<File> _cluster2AvroFiles2;

  /**
   * Cluster configuration helper class
   */
  private static class ClusterConfig {
    final String name;
    final int basePort;

    ClusterConfig(String name, int basePort) {
      this.name = name;
      this.basePort = basePort;
    }
  }

  /**
   * Cluster components container
   */
  private static class ClusterComponents {
    ZkStarter.ZookeeperInstance zkInstance;
    BaseControllerStarter controllerStarter;
    BaseBrokerStarter brokerStarter;
    BaseServerStarter serverStarter;
    int controllerPort;
    int brokerPort;
    int serverPort;
    String zkUrl;
    String controllerBaseApiUrl;
    Connection pinotConnection;
    File tempDir;
    File segmentDir;
    File tarDir;
  }

  @BeforeClass
  public void setUp() throws Exception {
    LOGGER.info("Setting up dual isolated Pinot clusters");

    // Initialize cluster components
    _cluster1 = new ClusterComponents();
    _cluster2 = new ClusterComponents();

    // Create test directories
    setupDirectories();

    startZookeeper(_cluster1);
    startZookeeper(_cluster2);

    startControllerInit(_cluster1, CLUSTER_1_CONFIG);
    startControllerInit(_cluster2, CLUSTER_2_CONFIG);

    // Start clusters
    startCluster(_cluster1, _cluster2, CLUSTER_1_CONFIG);
    startCluster(_cluster2, _cluster1, CLUSTER_2_CONFIG);

    // Setup connections
    setupPinotConnections();

    LOGGER.info("Dual isolated Pinot clusters setup completed");
  }

  private void setupDirectories() throws Exception {
    _cluster1.tempDir = new File(FileUtils.getTempDirectory(), "cluster1_" + getClass().getSimpleName());
    _cluster1.segmentDir = new File(_cluster1.tempDir, "segmentDir");
    _cluster1.tarDir = new File(_cluster1.tempDir, "tarDir");

    _cluster2.tempDir = new File(FileUtils.getTempDirectory(), "cluster2_" + getClass().getSimpleName());
    _cluster2.segmentDir = new File(_cluster2.tempDir, "segmentDir");
    _cluster2.tarDir = new File(_cluster2.tempDir, "tarDir");

    TestUtils.ensureDirectoriesExistAndEmpty(_cluster1.tempDir, _cluster1.segmentDir, _cluster1.tarDir);
    TestUtils.ensureDirectoriesExistAndEmpty(_cluster2.tempDir, _cluster2.segmentDir, _cluster2.tarDir);
  }

  private void startZookeeper(ClusterComponents cluster) throws Exception {
    LOGGER.info("Starting Zookeeper for cluster: {}", cluster.tempDir.getName());
    cluster.zkInstance = ZkStarter.startLocalZkServer();
    cluster.zkUrl = cluster.zkInstance.getZkUrl();
  }

  private void startControllerInit(ClusterComponents cluster, ClusterConfig config) throws Exception {
    cluster.controllerPort = findAvailablePort(config.basePort);
    startController(cluster, config);
  }

  private void startCluster(ClusterComponents cluster, ClusterComponents secondaryCluster, ClusterConfig config) throws Exception {
    LOGGER.info("Starting cluster: {}", config.name);

    // Start Zookeeper
//    cluster.zkInstance = ZkStarter.startLocalZkServer();
//    cluster.zkUrl = cluster.zkInstance.getZkUrl();

    // Start Controller
//    cluster.controllerPort = findAvailablePort(config.basePort);
//    startController(cluster, config);

    // Start Broker
    cluster.brokerPort = findAvailablePort(cluster.controllerPort + 1000);
    startBroker(cluster, secondaryCluster, config);

    // Start Server with MSE enabled
    cluster.serverPort = findAvailablePort(cluster.brokerPort + 1000);
    startServerWithMSE(cluster, config);

    LOGGER.info("Cluster {} started successfully", config.name);
  }

  private void startController(ClusterComponents cluster, ClusterConfig config) throws Exception {
    Map<String, Object> controllerConfig = new HashMap<>();
    controllerConfig.put(ControllerConf.ZK_STR, cluster.zkUrl);
    controllerConfig.put(ControllerConf.HELIX_CLUSTER_NAME, config.name);
    controllerConfig.put(ControllerConf.CONTROLLER_HOST, ControllerTest.LOCAL_HOST);
    controllerConfig.put(ControllerConf.CONTROLLER_PORT, cluster.controllerPort);
    controllerConfig.put(ControllerConf.DATA_DIR, cluster.tempDir.getAbsolutePath());
    controllerConfig.put(ControllerConf.LOCAL_TEMP_DIR, cluster.tempDir.getAbsolutePath());
    controllerConfig.put(ControllerConf.DISABLE_GROOVY, false);
    controllerConfig.put(ControllerConf.CONSOLE_SWAGGER_ENABLE, false);
    controllerConfig.put(CommonConstants.CONFIG_OF_TIMEZONE, "UTC");

    cluster.controllerStarter = createControllerStarter();
    cluster.controllerStarter.init(new PinotConfiguration(controllerConfig));
    cluster.controllerStarter.start();
    cluster.controllerBaseApiUrl = "http://localhost:" + cluster.controllerPort;
  }

  private void startBroker(ClusterComponents cluster, ClusterComponents secondaryCluster, ClusterConfig config) throws Exception {
    PinotConfiguration brokerConfig = new PinotConfiguration();
    brokerConfig.setProperty(Helix.CONFIG_OF_ZOOKEEPR_SERVER, cluster.zkUrl);
    if (config.name.equalsIgnoreCase("DualIsolatedCluster2")) {
      brokerConfig.setProperty(Helix.CONFIG_OF_SECONDARY_ZOOKEEPR_SERVER, secondaryCluster.zkUrl);
      brokerConfig.setProperty(Helix.CONFIG_OF_SECONDARY_CLUSTER_NAME, "DualIsolatedCluster1");
    } else {
      brokerConfig.setProperty(Helix.CONFIG_OF_SECONDARY_ZOOKEEPR_SERVER, secondaryCluster.zkUrl);
      brokerConfig.setProperty(Helix.CONFIG_OF_SECONDARY_CLUSTER_NAME, "DualIsolatedCluster2");
    }
    brokerConfig.setProperty(Helix.CONFIG_OF_CLUSTER_NAME, config.name);
    brokerConfig.setProperty(Broker.CONFIG_OF_BROKER_HOSTNAME, ControllerTest.LOCAL_HOST);
    brokerConfig.setProperty(Helix.KEY_OF_BROKER_QUERY_PORT, cluster.brokerPort);
    brokerConfig.setProperty(Broker.CONFIG_OF_BROKER_TIMEOUT_MS, 60 * 1000L);
    brokerConfig.setProperty(Broker.CONFIG_OF_DELAY_SHUTDOWN_TIME_MS, 0);
    brokerConfig.setProperty(CommonConstants.CONFIG_OF_TIMEZONE, "UTC");

    cluster.brokerStarter = createBrokerStarter();
    cluster.brokerStarter.init(brokerConfig);
    cluster.brokerStarter.start();
  }

  private void startServer(ClusterComponents cluster, ClusterConfig config) throws Exception {
    PinotConfiguration serverConfig = new PinotConfiguration();
    serverConfig.setProperty(Helix.CONFIG_OF_ZOOKEEPR_SERVER, cluster.zkUrl);
    serverConfig.setProperty(Helix.CONFIG_OF_CLUSTER_NAME, config.name);
    serverConfig.setProperty(Helix.KEY_OF_SERVER_NETTY_HOST, ControllerTest.LOCAL_HOST);
    serverConfig.setProperty(Server.CONFIG_OF_INSTANCE_DATA_DIR, cluster.tempDir + "/dataDir");
    serverConfig.setProperty(Server.CONFIG_OF_INSTANCE_SEGMENT_TAR_DIR, cluster.tempDir + "/segmentTar");
    serverConfig.setProperty(Server.CONFIG_OF_SEGMENT_FORMAT_VERSION, "v3");
    serverConfig.setProperty(Server.CONFIG_OF_SHUTDOWN_ENABLE_QUERY_CHECK, false);
    serverConfig.setProperty(Server.CONFIG_OF_ADMIN_API_PORT, findAvailablePort(cluster.serverPort));
    serverConfig.setProperty(Helix.KEY_OF_SERVER_NETTY_PORT, findAvailablePort(cluster.serverPort + 1));
    serverConfig.setProperty(Server.CONFIG_OF_GRPC_PORT, findAvailablePort(cluster.serverPort + 2));
    serverConfig.setProperty(Server.CONFIG_OF_ENABLE_THREAD_CPU_TIME_MEASUREMENT, true);
    serverConfig.setProperty(CommonConstants.CONFIG_OF_TIMEZONE, "UTC");
    serverConfig.setProperty(Helix.CONFIG_OF_MULTI_STAGE_ENGINE_ENABLED, false);

    cluster.serverStarter = createServerStarter();
    cluster.serverStarter.init(serverConfig);
    cluster.serverStarter.start();
  }

  private void startServerWithMSE(ClusterComponents cluster, ClusterConfig config) throws Exception {
    PinotConfiguration serverConfig = new PinotConfiguration();
    serverConfig.setProperty(Helix.CONFIG_OF_ZOOKEEPR_SERVER, cluster.zkUrl);
    serverConfig.setProperty(Helix.CONFIG_OF_CLUSTER_NAME, config.name);
    serverConfig.setProperty(Helix.KEY_OF_SERVER_NETTY_HOST, ControllerTest.LOCAL_HOST);
    serverConfig.setProperty(Server.CONFIG_OF_INSTANCE_DATA_DIR, cluster.tempDir + "/dataDir");
    serverConfig.setProperty(Server.CONFIG_OF_INSTANCE_SEGMENT_TAR_DIR, cluster.tempDir + "/segmentTar");
    serverConfig.setProperty(Server.CONFIG_OF_SEGMENT_FORMAT_VERSION, "v3");
    serverConfig.setProperty(Server.CONFIG_OF_SHUTDOWN_ENABLE_QUERY_CHECK, false);
    serverConfig.setProperty(Server.CONFIG_OF_ADMIN_API_PORT, findAvailablePort(cluster.serverPort));
    serverConfig.setProperty(Helix.KEY_OF_SERVER_NETTY_PORT, findAvailablePort(cluster.serverPort + 1));
    serverConfig.setProperty(Server.CONFIG_OF_GRPC_PORT, findAvailablePort(cluster.serverPort + 2));
    serverConfig.setProperty(Server.CONFIG_OF_ENABLE_THREAD_CPU_TIME_MEASUREMENT, true);
    serverConfig.setProperty(CommonConstants.CONFIG_OF_TIMEZONE, "UTC");
    serverConfig.setProperty(Helix.CONFIG_OF_MULTI_STAGE_ENGINE_ENABLED, true);

    cluster.serverStarter = createServerStarter();
    cluster.serverStarter.init(serverConfig);
    cluster.serverStarter.start();
  }

  private int findAvailablePort(int basePort) {
    for (int attempt = 0; attempt < 10; attempt++) {
      try {
        int port = NetUtils.findOpenPort(basePort + attempt);
        try (ServerSocket testSocket = new ServerSocket(port)) {
          return port;
        }
      } catch (IOException e) {
        LOGGER.warn("Port allocation attempt {} failed, retrying...", attempt + 1);
      }
    }
    throw new RuntimeException("Failed to find available port after 10 attempts");
  }

  private void setupPinotConnections() throws Exception {
    PinotClientTransport transport1 = new JsonAsyncHttpPinotClientTransportFactory().buildTransport();
    _cluster1.pinotConnection = ConnectionFactory.fromZookeeper(
        _cluster1.zkUrl + "/" + CLUSTER_1_CONFIG.name, transport1);

    PinotClientTransport transport2 = new JsonAsyncHttpPinotClientTransportFactory().buildTransport();
    _cluster2.pinotConnection = ConnectionFactory.fromZookeeper(
        _cluster2.zkUrl + "/" + CLUSTER_2_CONFIG.name, transport2);
  }

  @Test
  public void testClusterIsolation() throws Exception {
    setupFederationTable();

    String cluster1Tables = ControllerTest.sendGetRequest(_cluster1.controllerBaseApiUrl + "/tables");
    String cluster2Tables = ControllerTest.sendGetRequest(_cluster2.controllerBaseApiUrl + "/tables");

    assertTrue(cluster1Tables.contains(FEDERATION_TABLE));
    assertTrue(cluster2Tables.contains(FEDERATION_TABLE));

    String cluster1Schemas = ControllerTest.sendGetRequest(_cluster1.controllerBaseApiUrl + "/schemas");
    String cluster2Schemas = ControllerTest.sendGetRequest(_cluster2.controllerBaseApiUrl + "/schemas");

    assertTrue(cluster1Schemas.contains(FEDERATION_TABLE));
    assertTrue(cluster2Schemas.contains(FEDERATION_TABLE));
  }

  @Test
  public void testIndependentOperations() throws Exception {
    setupFederationTable();

    String cluster1TableInfo = ControllerTest.sendGetRequest(_cluster1.controllerBaseApiUrl + "/tables/" + FEDERATION_TABLE);
    String cluster2TableInfo = ControllerTest.sendGetRequest(_cluster2.controllerBaseApiUrl + "/tables/" + FEDERATION_TABLE);

    assertNotNull(cluster1TableInfo);
    assertNotNull(cluster2TableInfo);
    assertTrue(cluster1TableInfo.contains(FEDERATION_TABLE));
    assertTrue(cluster2TableInfo.contains(FEDERATION_TABLE));
  }

  @Test
  public void testDataIsolation() throws Exception {
    setupFederationTable();
    cleanSegmentDirs();

    // Generate and load data
    _cluster1AvroFiles = createAvroData(CLUSTER_1_SIZE, 1);
    _cluster2AvroFiles = createAvroData(CLUSTER_2_SIZE, 2);

    loadDataIntoCluster(_cluster1AvroFiles, FEDERATION_TABLE, _cluster1);
    loadDataIntoCluster(_cluster2AvroFiles, FEDERATION_TABLE, _cluster2);

    // Verify counts
    long cluster1Count = getCount(FEDERATION_TABLE, _cluster1);
    long cluster2Count = getCount(FEDERATION_TABLE, _cluster2);

    assertEquals(cluster1Count, CLUSTER_1_SIZE);
    assertEquals(cluster2Count, CLUSTER_2_SIZE);

    // Verify data prefixes
    assertTrue(containsPrefix(FEDERATION_TABLE, _cluster1, CLUSTER_1_PREFIX));
    assertTrue(containsPrefix(FEDERATION_TABLE, _cluster2, CLUSTER_2_PREFIX));
  }

  public class ZkPathCollector {
    public List<String> getAllPaths(ZkClient zkClient, String startPath) {
      List<String> allPaths = new ArrayList<>();
      collectPathsRecursive(zkClient, startPath, allPaths);
      return allPaths;
    }

    private void collectPathsRecursive(ZkClient zkClient, String path, List<String> paths) {
      if (!zkClient.exists(path)) return;

      paths.add(path);
      List<String> children = zkClient.getChildren(path);
      for (String child : children) {
        String childPath = path.equals("/") ? "/" + child : path + "/" + child;
        collectPathsRecursive(zkClient, childPath, paths);
      }
    }
  }

  @Test
  public void testDataLoading() throws Exception {
//    ZkClient zkClient = new ZkClient("localhost:2191", 30000, 30000, new ZNRecordSerializer());
//    ZkPathCollector collector = new ZkPathCollector();
//    ZNRecord ideal = zkClient.readData("/DualIsolatedCluster1/IDEALSTATES/brokerResource");

    setupFederationTable();
    cleanSegmentDirs();

//
//    List<String> zkPaths = collector.getAllPaths(zkClient, "/");
//
//    ZNRecord idealAfter = zkClient.readData("/DualIsolatedCluster1/IDEALSTATES/brokerResource");
//    Map<String, String> partitionMap = ideal.getMapField("brokerResource");

    // Generate and load data
    _cluster1AvroFiles = createAvroData(CLUSTER_1_SIZE, 1);
    _cluster2AvroFiles = createAvroData(CLUSTER_2_SIZE, 2);

    loadDataIntoCluster(_cluster1AvroFiles, FEDERATION_TABLE, _cluster1);
    loadDataIntoCluster(_cluster2AvroFiles, FEDERATION_TABLE, _cluster2);

    // Verify counts
    long cluster1Count = getCount(FEDERATION_TABLE, _cluster1);
    long cluster2Count = getCount(FEDERATION_TABLE, _cluster2);

    assertEquals(cluster1Count, CLUSTER_1_SIZE + CLUSTER_2_SIZE);
    assertEquals(cluster2Count, CLUSTER_2_SIZE + CLUSTER_1_SIZE);

    // Verify data prefixes
    assertTrue(containsPrefix(FEDERATION_TABLE, _cluster1, CLUSTER_1_PREFIX));
    assertTrue(containsPrefix(FEDERATION_TABLE, _cluster2, CLUSTER_2_PREFIX));
  }

  @Test
  public void testDataGeneration() throws Exception {
    List<File> cluster1Files = createAvroData(CLUSTER_1_SIZE, 1);
    List<File> cluster2Files = createAvroData(CLUSTER_2_SIZE, 2);

    for (File file : cluster1Files) {
      assertTrue(file.exists());
      assertTrue(file.length() > 0);
    }

    for (File file : cluster2Files) {
      assertTrue(file.exists());
      assertTrue(file.length() > 0);
    }
  }

  @Test
  public void testDataGenerationSimple() throws Exception {
    String testFieldName = "test_field";

    Object cluster1Value = generateFieldValue(testFieldName, 0, 1, FieldSpec.DataType.STRING);
    Object cluster2Value = generateFieldValue(testFieldName, 0, 2, FieldSpec.DataType.STRING);

    String cluster1String = cluster1Value.toString();
    String cluster2String = cluster2Value.toString();

    assertTrue(cluster1String.contains("cluster1_"));
    assertTrue(cluster2String.contains("cluster2_"));
    assertFalse(cluster1String.contains("cluster2_"));
    assertFalse(cluster2String.contains("cluster1_"));
  }

  @Test
  public void testMSEFederationJoin() throws Exception {
    // Setup both tables
    setupFederationTable();
    setupFederationTable2();
    cleanSegmentDirs();

    // Generate data with multiple segments for second table
    _cluster1AvroFiles = createAvroData(CLUSTER_1_SIZE, 1);
    _cluster2AvroFiles = createAvroData(CLUSTER_2_SIZE, 2);
    _cluster1AvroFiles2 = createAvroDataMultipleSegments(CLUSTER_1_SIZE, 1, SEGMENTS_PER_CLUSTER);
    _cluster2AvroFiles2 = createAvroDataMultipleSegments(CLUSTER_2_SIZE, 2, SEGMENTS_PER_CLUSTER);

    // Load data into both tables
    loadDataIntoCluster(_cluster1AvroFiles, FEDERATION_TABLE, _cluster1);
    loadDataIntoCluster(_cluster2AvroFiles, FEDERATION_TABLE, _cluster2);
    loadDataIntoCluster(_cluster1AvroFiles2, FEDERATION_TABLE_2, _cluster1);
    loadDataIntoCluster(_cluster2AvroFiles2, FEDERATION_TABLE_2, _cluster2);

    // Verify data is loaded
    long table1Count = getCount(FEDERATION_TABLE, _cluster1);
    long table2Count = getCount(FEDERATION_TABLE_2, _cluster1);
    assertTrue(table1Count > 0);
    assertTrue(table2Count > 0);

    // Test join query with MSE
    String joinQuery = "SET useMultistageEngine=true; SET usePhysicalOptimizer=true; SET useLiteMode=true; SET runInBroker=true; SELECT t1." + JOIN_COLUMN + ", COUNT(*) as count " +
        "FROM " + FEDERATION_TABLE + " t1 " +
        "JOIN " + FEDERATION_TABLE_2 + " t2 ON t1." + JOIN_COLUMN + " = t2." + JOIN_COLUMN + " " +
        "GROUP BY t1." + JOIN_COLUMN + " LIMIT 10";

    String result = executeQuery(joinQuery, _cluster1);
    System.out.println("SQL OUTPUT = " + result);
    assertNotNull(result);
    assertTrue(result.contains("resultTable"));

    LOGGER.info("MSE Federation Join Test completed successfully");
  }

  private List<File> createAvroData(int dataSize, int clusterId) throws Exception {
    Schema schema = createSchema(SCHEMA_FILE);
    org.apache.avro.Schema avroSchema = createAvroSchema(schema);

    File tempDir = (clusterId == 1) ? _cluster1.tempDir : _cluster2.tempDir;
    File avroFile = new File(tempDir, "cluster" + clusterId + "_data.avro");

    try (DataFileWriter<GenericData.Record> fileWriter = new DataFileWriter<>(new GenericDatumWriter<>(avroSchema))) {
      fileWriter.create(avroSchema, avroFile);

      for (int i = 0; i < dataSize; i++) {
        GenericData.Record record = new GenericData.Record(avroSchema);
        for (FieldSpec fieldSpec : schema.getAllFieldSpecs()) {
          String fieldName = fieldSpec.getName();
          Object value = generateFieldValue(fieldName, i, clusterId, fieldSpec.getDataType());
          record.put(fieldName, value);
        }
        fileWriter.append(record);
      }
    }

    return List.of(avroFile);
  }

  private List<File> createAvroDataMultipleSegments(int totalDataSize, int clusterId, int numSegments) throws Exception {
    Schema schema = createSchema(SCHEMA_FILE);
    org.apache.avro.Schema avroSchema = createAvroSchema(schema);

    File tempDir = (clusterId == 1) ? _cluster1.tempDir : _cluster2.tempDir;
    List<File> avroFiles = new ArrayList<>();
    int dataPerSegment = totalDataSize / numSegments;

    for (int segment = 0; segment < numSegments; segment++) {
      File avroFile = new File(tempDir, "cluster" + clusterId + "_data_segment" + segment + ".avro");

      try (DataFileWriter<GenericData.Record> fileWriter = new DataFileWriter<>(new GenericDatumWriter<>(avroSchema))) {
        fileWriter.create(avroSchema, avroFile);

        int startIndex = segment * dataPerSegment;
        int endIndex = (segment == numSegments - 1) ? totalDataSize : (segment + 1) * dataPerSegment;

        for (int i = startIndex; i < endIndex; i++) {
          GenericData.Record record = new GenericData.Record(avroSchema);
          for (FieldSpec fieldSpec : schema.getAllFieldSpecs()) {
            String fieldName = fieldSpec.getName();
            Object value = generateFieldValue(fieldName, i, clusterId, fieldSpec.getDataType());
            record.put(fieldName, value);
          }
          fileWriter.append(record);
        }
      }
      avroFiles.add(avroFile);
    }

    return avroFiles;
  }

  private org.apache.avro.Schema createAvroSchema(Schema schema) {
    org.apache.avro.Schema avroSchema = org.apache.avro.Schema.createRecord("myRecord", null, null, false);
    List<org.apache.avro.Schema.Field> fields = new ArrayList<>();

    for (FieldSpec fieldSpec : schema.getAllFieldSpecs()) {
      org.apache.avro.Schema.Type avroType = getAvroType(fieldSpec.getDataType());
      fields.add(new org.apache.avro.Schema.Field(fieldSpec.getName(),
          org.apache.avro.Schema.create(avroType), null, null));
    }
    avroSchema.setFields(fields);
    return avroSchema;
  }

  private org.apache.avro.Schema.Type getAvroType(FieldSpec.DataType pinotType) {
    switch (pinotType) {
      case INT: return org.apache.avro.Schema.Type.INT;
      case LONG: return org.apache.avro.Schema.Type.LONG;
      case FLOAT: return org.apache.avro.Schema.Type.FLOAT;
      case DOUBLE: return org.apache.avro.Schema.Type.DOUBLE;
      case STRING: return org.apache.avro.Schema.Type.STRING;
      case BOOLEAN: return org.apache.avro.Schema.Type.BOOLEAN;
      default: return org.apache.avro.Schema.Type.STRING;
    }
  }

  private Object generateFieldValue(String fieldName, int index, int clusterId, FieldSpec.DataType dataType) {
    int baseValue = index + (clusterId * 10000);

    switch (dataType) {
      case INT: return index + 10000;
      case LONG: return (long) baseValue;
      case FLOAT: return (float) (baseValue + 0.1);
      case DOUBLE: return (double) (baseValue + 0.1);
      case STRING: return "cluster" + "_" + fieldName + "_" + index;
      case BOOLEAN: return (baseValue % 2) == 0;
      default: return "cluster" + clusterId + "_" + fieldName + "_" + baseValue;
    }
  }

  private void loadDataIntoCluster(List<File> avroFiles, String tableName, ClusterComponents cluster) throws Exception {
    FileUtils.cleanDirectory(cluster.segmentDir);
    FileUtils.cleanDirectory(cluster.tarDir);

    Schema schema = createSchema(SCHEMA_FILE);
    schema.setSchemaName(tableName);

    TableConfig tableConfig = new TableConfigBuilder(TableType.OFFLINE)
        .setTableName(tableName)
        .setTimeColumnName(TIME_COLUMN)
        .build();

    ClusterIntegrationTestUtils.buildSegmentsFromAvro(avroFiles, tableConfig, schema, 0, cluster.segmentDir, cluster.tarDir);
    uploadSegmentsToCluster(tableName, cluster.tarDir, cluster.controllerBaseApiUrl);
    Thread.sleep(2000);
  }

  private void uploadSegmentsToCluster(String tableName, File tarDir, String controllerBaseApiUrl) throws Exception {
    File[] segmentTarFiles = tarDir.listFiles();
    assertNotNull(segmentTarFiles);
    assertTrue(segmentTarFiles.length > 0);

    URI uploadSegmentHttpURI = URI.create(controllerBaseApiUrl + "/segments");

    try (FileUploadDownloadClient fileUploadDownloadClient = new FileUploadDownloadClient()) {
      for (File segmentTarFile : segmentTarFiles) {
        int status = fileUploadDownloadClient.uploadSegment(uploadSegmentHttpURI, segmentTarFile.getName(), segmentTarFile,
            List.of(), tableName, TableType.OFFLINE).getStatusCode();
        assertEquals(status, HttpStatus.SC_OK);
      }
    }

    Thread.sleep(3000);
  }

  private void setupFederationTable() throws Exception {
    dropTableAndSchemaIfExists(FEDERATION_TABLE, _cluster1.controllerBaseApiUrl);
    dropTableAndSchemaIfExists(FEDERATION_TABLE, _cluster2.controllerBaseApiUrl);

    Schema schema = createSchema(SCHEMA_FILE);
    schema.setSchemaName(FEDERATION_TABLE);
    addSchemaToCluster(schema, _cluster1.controllerBaseApiUrl);
    addSchemaToCluster(schema, _cluster2.controllerBaseApiUrl);

    TableConfig tableConfig = new TableConfigBuilder(TableType.OFFLINE)
        .setTableName(FEDERATION_TABLE)
        .setTimeColumnName(TIME_COLUMN)
        .build();
    addTableConfigToCluster(tableConfig, _cluster1.controllerBaseApiUrl);
    addTableConfigToCluster(tableConfig, _cluster2.controllerBaseApiUrl);
  }

  private void setupFederationTable2() throws Exception {
    dropTableAndSchemaIfExists(FEDERATION_TABLE_2, _cluster1.controllerBaseApiUrl);
    dropTableAndSchemaIfExists(FEDERATION_TABLE_2, _cluster2.controllerBaseApiUrl);

    Schema schema = createSchema(SCHEMA_FILE);
    schema.setSchemaName(FEDERATION_TABLE_2);
    addSchemaToCluster(schema, _cluster1.controllerBaseApiUrl);
    addSchemaToCluster(schema, _cluster2.controllerBaseApiUrl);

    TableConfig tableConfig = new TableConfigBuilder(TableType.OFFLINE)
        .setTableName(FEDERATION_TABLE_2)
        .setTimeColumnName(TIME_COLUMN)
        .build();
    addTableConfigToCluster(tableConfig, _cluster1.controllerBaseApiUrl);
    addTableConfigToCluster(tableConfig, _cluster2.controllerBaseApiUrl);
  }

  private void cleanSegmentDirs() throws Exception {
    FileUtils.cleanDirectory(_cluster1.segmentDir);
    FileUtils.cleanDirectory(_cluster1.tarDir);
    FileUtils.cleanDirectory(_cluster2.segmentDir);
    FileUtils.cleanDirectory(_cluster2.tarDir);
  }

  private void dropTableAndSchemaIfExists(String tableName, String controllerBaseApiUrl) throws Exception {
    try {
      ControllerTest.sendDeleteRequest(controllerBaseApiUrl + "/tables/" + tableName);
    } catch (Exception e) {
      // Ignore if not exists
    }
    try {
      ControllerTest.sendDeleteRequest(controllerBaseApiUrl + "/schemas/" + tableName);
    } catch (Exception e) {
      // Ignore if not exists
    }
  }

  private void addSchemaToCluster(Schema schema, String controllerBaseApiUrl) throws IOException {
    String url = controllerBaseApiUrl + "/schemas";
    String schemaJson = schema.toPrettyJsonString();
    String response = ControllerTest.sendPostRequest(url, schemaJson);
    assertNotNull(response);
  }

  private void addTableConfigToCluster(TableConfig tableConfig, String controllerBaseApiUrl) throws IOException {
    String url = controllerBaseApiUrl + "/tables";
    String tableConfigJson = JsonUtils.objectToPrettyString(tableConfig);
    String response = ControllerTest.sendPostRequest(url, tableConfigJson);
    assertNotNull(response);
  }

  private long getCount(String tableName, ClusterComponents cluster) throws Exception {
    String query = "SELECT COUNT(*) as count FROM " + tableName;
    String result = executeQuery(query, cluster);
    return parseCountResult(result);
  }

  private boolean containsPrefix(String tableName, ClusterComponents cluster, String prefix) throws Exception {
    String query = "SELECT " + STRING_COLUMN + ", count(*) FROM " + tableName + " group by 1 LIMIT 5";
    String result = executeQuery(query, cluster);

    try {
      com.fasterxml.jackson.databind.JsonNode root = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(result);
      if (root.has("resultTable")) {
        com.fasterxml.jackson.databind.JsonNode rows = root.get("resultTable").get("rows");
        if (rows != null && rows.isArray()) {
          for (com.fasterxml.jackson.databind.JsonNode row : rows) {
            System.out.println(row);
            if (row.isArray() && row.size() > 0 && row.get(0).asText().contains(prefix)) {
              return true;
            }
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing query result for prefix check", e);
    }
    return false;
  }

  private String executeQuery(String query, ClusterComponents cluster) throws Exception {
    String queryUrl = "http://localhost:" + cluster.brokerPort + "/query/sql";
    Map<String, Object> requestPayload = new HashMap<>();
    requestPayload.put("sql", query);
    String jsonPayload = JsonUtils.objectToPrettyString(requestPayload);
    return ControllerTest.sendPostRequest(queryUrl, jsonPayload);
  }

  private long parseCountResult(String result) {
    try {
      com.fasterxml.jackson.databind.JsonNode root = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(result);
      if (root.has("resultTable")) {
        com.fasterxml.jackson.databind.JsonNode resultTable = root.get("resultTable");
        if (resultTable.has("rows")) {
          com.fasterxml.jackson.databind.JsonNode rows = resultTable.get("rows");
          if (rows.isArray() && rows.size() > 0) {
            com.fasterxml.jackson.databind.JsonNode firstRow = rows.get(0);
            if (firstRow.isArray() && firstRow.size() > 0) {
              return Long.parseLong(firstRow.get(0).asText());
            }
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing count result: {}", result, e);
    }
    return 0;
  }

  private Schema createSchema(String schemaFileName) throws IOException {
    InputStream schemaInputStream = getClass().getClassLoader().getResourceAsStream(schemaFileName);
    assertNotNull(schemaInputStream, "Schema file not found: " + schemaFileName);
    return Schema.fromInputStream(schemaInputStream);
  }

  @AfterClass
  public void tearDown() throws Exception {
    LOGGER.info("Tearing down dual isolated Pinot clusters");

    // Stop Cluster 1 components
    if (_cluster1.serverStarter != null) _cluster1.serverStarter.stop();
    if (_cluster1.brokerStarter != null) _cluster1.brokerStarter.stop();
    if (_cluster1.controllerStarter != null) _cluster1.controllerStarter.stop();
    if (_cluster1.zkInstance != null) ZkStarter.stopLocalZkServer(_cluster1.zkInstance);

    // Stop Cluster 2 components
    if (_cluster2.serverStarter != null) _cluster2.serverStarter.stop();
    if (_cluster2.brokerStarter != null) _cluster2.brokerStarter.stop();
    if (_cluster2.controllerStarter != null) _cluster2.controllerStarter.stop();
    if (_cluster2.zkInstance != null) ZkStarter.stopLocalZkServer(_cluster2.zkInstance);

    // Clean up test directories
    FileUtils.deleteQuietly(_cluster1.tempDir);
    FileUtils.deleteQuietly(_cluster2.tempDir);

    LOGGER.info("Dual isolated Pinot clusters torn down successfully");
  }
}
