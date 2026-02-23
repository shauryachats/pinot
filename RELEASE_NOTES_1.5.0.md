# Apache Pinot 1.5.0

Apache Pinot 1.5.0 is a major release that brings significant enhancements across federation, time series capabilities, query engine improvements, security, and performance optimizations. This release includes **over 1,000 commits** from the community, making it one of the most substantial releases in Pinot's history.

## What's New in 1.5.0

### Major Features

#### 🌐 Federation & Multi-Cluster Support (Beta)
[Design Doc](https://docs.google.com/document/d/federation-design)

Apache Pinot 1.5.0 introduces comprehensive **multi-cluster federation** capabilities, enabling queries to span multiple Pinot clusters seamlessly. This feature is critical for organizations managing geographically distributed data or multiple independent Pinot deployments.

**Key Capabilities:**
- Multi-cluster routing support for both MSE and SSE query engines ([#17444](https://github.com/apache/pinot/pull/17444), [#17439](https://github.com/apache/pinot/pull/17439))
- Physical optimizer integration for federated queries ([#17516](https://github.com/apache/pinot/pull/17516))
- New `MultiClusterHelixBrokerStarter` for federated broker mode ([#17421](https://github.com/apache/pinot/pull/17421))
- Automatic handling of unavailable remote clusters with warnings in broker responses ([#17510](https://github.com/apache/pinot/pull/17510))
- TLS support for logical table routing ([#17663](https://github.com/apache/pinot/pull/17663))
- Multi-cluster QuickStart for easy evaluation ([#17581](https://github.com/apache/pinot/pull/17581))

**Configuration Example:**
```properties
pinot.broker.enable.federation=true
pinot.broker.federation.clusters=cluster1,cluster2
pinot.broker.federation.cluster1.zk.address=zk1:2181
pinot.broker.federation.cluster2.zk.address=zk2:2181
```

#### ⏱️ Time Series Engine Enhancements

Apache Pinot's Time Series Engine has graduated from experimental to **Beta** status with this release, offering production-ready capabilities for time series analytics.

**Query Execution Improvements:**
- End-to-end query execution statistics propagation ([#17170](https://github.com/apache/pinot/pull/17170))
- Query event listener support for custom monitoring ([#17464](https://github.com/apache/pinot/pull/17464))
- Partial results support for time series queries ([#17278](https://github.com/apache/pinot/pull/17278))
- TimeSeriesBlock-based exception propagation for better error handling ([#17440](https://github.com/apache/pinot/pull/17440))

**Language & Parsing:**
- M3QL JavaCC parser replacing custom tokenizer implementation ([#17192](https://github.com/apache/pinot/pull/17192))
- Column validation in logical planning ([#17081](https://github.com/apache/pinot/pull/17081))

**Storage Optimizations:**
- Delta and DeltaDelta compression codecs for time series data ([#15258](https://github.com/apache/pinot/pull/15258))
- Validation for DeltaDelta/Delta compression in table config ([#17252](https://github.com/apache/pinot/pull/17252))

**UI & Visualization:**
- Apache ECharts-based time series visualization in Controller UI ([#16390](https://github.com/apache/pinot/pull/16390))
- Query statistics panel with time metrics ([#17423](https://github.com/apache/pinot/pull/17423))
- Explain plan support in Controller UI ([#17511](https://github.com/apache/pinot/pull/17511))
- BrokerResponse-compatible `/query/timeseries` API ([#16531](https://github.com/apache/pinot/pull/16531))
- Configurable max series limit for chart visualization ([#16580](https://github.com/apache/pinot/pull/16580))

#### 🔍 Query Fingerprinting

New **query fingerprinting** capability enables pattern identification and optimization tracking across your query workload ([#17177](https://github.com/apache/pinot/pull/17177)). This feature helps identify similar query patterns, track query performance trends, and optimize frequently executed queries.

**Use Cases:**
- Identify and optimize common query patterns
- Track query performance regression across releases
- Enable query-level caching and optimization hints
- Workload analysis and capacity planning

#### 🏗️ Column-Major Segment Building

Fundamental change to segment building architecture with **column-major** format ([#16727](https://github.com/apache/pinot/pull/16727)). This new approach offers:
- Better columnar data handling and compression
- Improved query performance for analytical workloads
- Enhanced compatibility with columnar storage formats
- Support for commit time compaction ([#16769](https://github.com/apache/pinot/pull/16769))

#### 🔌 Apache Arrow Format Support

Native Apache Arrow format decoder enables direct ingestion of Arrow-formatted data ([#17031](https://github.com/apache/pinot/pull/17031)), providing:
- Zero-copy data transfer from Arrow producers
- Better integration with modern data processing frameworks
- Reduced serialization overhead
- Improved ingestion performance

#### 🛠️ Comprehensive Admin CLI

New `pinot-cli` module provides a dedicated terminal interface for Pinot administration ([#17029](https://github.com/apache/pinot/pull/17029), [#17040](https://github.com/apache/pinot/pull/17040)):

```bash
# Comprehensive admin operations
pinot-admin table create -t myTable -c tableConfig.json
pinot-admin segment upload -s segment.tar.gz
pinot-admin query -q "SELECT * FROM myTable"
```

#### 📊 Enhanced Backfill Support

Complete backfill job orchestration with production-grade features ([#16890](https://github.com/apache/pinot/pull/16890)):
- Self-signed certificate support for secure clusters ([#16411](https://github.com/apache/pinot/pull/16411))
- Automated backfill job scheduling and monitoring
- TLS specification serialization ([#16381](https://github.com/apache/pinot/pull/16381))

---

### Query Engine Enhancements

#### Multi-Stage Query Engine (MSE)

**Join Improvements:**
- Enriched join operator with performance optimizations ([#16828](https://github.com/apache/pinot/pull/16828))
- **UNNEST / CROSS JOIN UNNEST** support ([#17168](https://github.com/apache/pinot/pull/17168))
- Reduced array copying for non-equi join conditions ([#17542](https://github.com/apache/pinot/pull/17542))
- Colocated join enforcement with `is_colocated_by_join_keys` hint ([#17273](https://github.com/apache/pinot/pull/17273))
- Join compatibility without partition functions ([#16603](https://github.com/apache/pinot/pull/16603))
- Enhanced LookupTable implementations ([#16408](https://github.com/apache/pinot/pull/16408))

**Set Operations:**
- Distinct UNION operator ([#16570](https://github.com/apache/pinot/pull/16570))
- Support for multiple children in UNION / UNION ALL ([#16990](https://github.com/apache/pinot/pull/16990))

**Error Handling:**
- ErrorOperator for comprehensive error management ([#16257](https://github.com/apache/pinot/pull/16257))
- Improved error messages for leaf stage errors ([#16374](https://github.com/apache/pinot/pull/16374))
- Proper exception handling for cancelled/killed queries ([#17009](https://github.com/apache/pinot/pull/17009))
- Self-termination in panic mode ([#16380](https://github.com/apache/pinot/pull/16380))

**Performance Optimizations:**
- Row() value expression support in comparisons ([#17317](https://github.com/apache/pinot/pull/17317))
- Fixed non-deterministic hash-distributed exchange routing ([#17323](https://github.com/apache/pinot/pull/17323))
- Deterministic workerId to server mapping ([#17342](https://github.com/apache/pinot/pull/17342))
- GRPC connection backoff reset when server re-enabled ([#17466](https://github.com/apache/pinot/pull/17466))

**New Metrics:**
- Comprehensive MSE metrics ([#17419](https://github.com/apache/pinot/pull/17419))
- Multi-stage query latency tracking ([#16398](https://github.com/apache/pinot/pull/16398))

**Query Features:**
- Exclude virtual columns from schema ([#17047](https://github.com/apache/pinot/pull/17047))
- REST API for table name extraction ([#16957](https://github.com/apache/pinot/pull/16957))
- Enhanced validateMultiStageQuery API ([#16752](https://github.com/apache/pinot/pull/16752), [#16746](https://github.com/apache/pinot/pull/16746))
- Controller REST API for query compilation/validation ([#16536](https://github.com/apache/pinot/pull/16536))

#### Single-Stage Query Engine (SSE)

**Aggregation Framework:**
- Comprehensive AggregationOptimizer framework supporting multiple functions ([#16399](https://github.com/apache/pinot/pull/16399))
- Consolidation of MV aggregations into SV aggregations ([#17519](https://github.com/apache/pinot/pull/17519), [#17109](https://github.com/apache/pinot/pull/17109))
- Sort-aggregate and pair-wise merge when orderBy equals groupBy ([#16308](https://github.com/apache/pinot/pull/16308))

**Type-Specific Optimizations:**
- Automatic rewrite of MIN/MAX/SUM on LONG columns to MINLONG/MAXLONG/SUMLONG ([#17058](https://github.com/apache/pinot/pull/17058))
- Automatic rewrite of MIN/MAX on STRING columns to MINSTRING/MAXSTRING ([#16980](https://github.com/apache/pinot/pull/16980))
- Dictionary/metadata-based optimizations for string aggregations ([#16983](https://github.com/apache/pinot/pull/16983))
- LONG-specific aggregation functions ([#17001](https://github.com/apache/pinot/pull/17001), [#17007](https://github.com/apache/pinot/pull/17007))
- SumIntAggregationFunction for optimized INT aggregation ([#16704](https://github.com/apache/pinot/pull/16704))

**Distinct Count Optimizations:**
- Smart HLL-based distinct count ([#17011](https://github.com/apache/pinot/pull/17011))
- DistinctCountSmartULL with set→ULL promotion ([#16811](https://github.com/apache/pinot/pull/16811))
- Hybrid cardinality: exact ≤2,048, then HLL++ ([#17186](https://github.com/apache/pinot/pull/17186))
- DistinctCountSmartHLL improvements for dictionary columns ([#17411](https://github.com/apache/pinot/pull/17411))

**New Aggregation Functions:**
- **ANY VALUE** support ([#16678](https://github.com/apache/pinot/pull/16678))
- String type support in MIN/MAX ([#16851](https://github.com/apache/pinot/pull/16851))
- ArrayAgg with multi-value column support ([#17153](https://github.com/apache/pinot/pull/17153))
- ListAggMv with distinct variant ([#17155](https://github.com/apache/pinot/pull/17155))
- Window value aggregator for AVG function ([#17268](https://github.com/apache/pinot/pull/17268))

**Query Planner:**
- Planner rule production tracing ([#16581](https://github.com/apache/pinot/pull/16581))
- Configurable default-disabled planner rules ([#17258](https://github.com/apache/pinot/pull/17258))
- Disabled JOIN_PUSH_TRANSITIVE_PREDICATES by default ([#17181](https://github.com/apache/pinot/pull/17181))
- Disabled AggregateUnionAggregate rule by default ([#16515](https://github.com/apache/pinot/pull/16515))
- ProjectRemove rule in PRUNE_RULES ([#16518](https://github.com/apache/pinot/pull/16518))

**CTE Support:**
- Handle CTE with same name as table ([#17591](https://github.com/apache/pinot/pull/17591))

---

### Upsert & Dedup Enhancements

#### Commit Time Compaction
**Commit time compaction** for upsert tables ([#16344](https://github.com/apache/pinot/pull/16344)) enables inline compaction during segment commits, significantly reducing storage overhead and improving query performance:

```json
{
  "tableIndexConfig": {
    "enableUpsert": true,
    "upsertConfig": {
      "mode": "FULL",
      "enableCommitTimeCompaction": true
    }
  }
}
```

**Related Improvements:**
- Soft-delete record preservation during compaction ([#16743](https://github.com/apache/pinot/pull/16743))
- Consensus check before segment selection ([#17352](https://github.com/apache/pinot/pull/17352))
- Configurable minNumSegmentsPerTask ([#17104](https://github.com/apache/pinot/pull/17104))
- Integration tests for UpsertCompactMerge ([#16592](https://github.com/apache/pinot/pull/16592))

#### Queryable DocIds Persistence
Persist QueryableDocIds on disk for upsert tables ([#16517](https://github.com/apache/pinot/pull/16517)), reducing memory footprint and enabling faster server restarts.

#### Partial Upsert Improvements
- Disabled reload on consuming segments ([#17144](https://github.com/apache/pinot/pull/17144))
- Removed reload/force commit restrictions ([#17458](https://github.com/apache/pinot/pull/17458))
- Metric to detect server ahead of ZK committed offset ([#17139](https://github.com/apache/pinot/pull/17139))

#### Out-of-Order Handling
- Restrictions on reload/force commit for dropOutOfOrderRecord=true ([#17251](https://github.com/apache/pinot/pull/17251))
- Default DISALLOW_ALWAYS for dropOutOfOrderRecord ([#17671](https://github.com/apache/pinot/pull/17671))
- Validation preventing dropOutOfOrderRecord when consistencyMode != NONE ([#17550](https://github.com/apache/pinot/pull/17550))

#### Primary Key Compression
XXHash support for primary key compression ([#17253](https://github.com/apache/pinot/pull/17253)), offering better compression ratios and performance than default hashing.

#### Valid Doc IDs
- SNAPSHOT_WITH_DELETE validDocIdsType support ([#16725](https://github.com/apache/pinot/pull/16725))
- Enhanced column reader support for valid doc IDs ([#17443](https://github.com/apache/pinot/pull/17443))

#### Dedup Enhancements
- StrictRealtimeSegmentAssignment with multi-tier support ([#17154](https://github.com/apache/pinot/pull/17154))
- TIMESTAMP support for dedupTimeColumn ([#17200](https://github.com/apache/pinot/pull/17200))

---

### Security & Authentication

#### Authentication & Authorization
- **@Authenticate** annotation for critical endpoints ([#17552](https://github.com/apache/pinot/pull/17552))
- Authorization action checks for missing APIs ([#17725](https://github.com/apache/pinot/pull/17725))
- Access control for segment download API ([#17508](https://github.com/apache/pinot/pull/17508))
- Authorization for revert replace segment ([#16434](https://github.com/apache/pinot/pull/16434))
- Fixed duplicate auth header issue ([#17134](https://github.com/apache/pinot/pull/17134))

#### TLS/SSL Enhancements
- SSL context configuration in Controller/Broker/Server contexts ([#17358](https://github.com/apache/pinot/pull/17358))
- Runtime TLS diagnostics for gRPC and HTTPS ([#17559](https://github.com/apache/pinot/pull/17559))
- Renewable SSL context support in HttpsSegmentFetcher ([#17315](https://github.com/apache/pinot/pull/17315))
- Fixed non-daemon threads in RenewableTlsUtils ([#17221](https://github.com/apache/pinot/pull/17221))
- TLS integration test improvements ([#17641](https://github.com/apache/pinot/pull/17641), [#17683](https://github.com/apache/pinot/pull/17683))

#### Audit Logging System
Comprehensive audit logging infrastructure with production-ready features:
- SPI-based token resolver for custom identity resolution ([#17658](https://github.com/apache/pinot/pull/17658))
- Broker REST API audit support ([#16823](https://github.com/apache/pinot/pull/16823))
- Response auditing capability ([#16851](https://github.com/apache/pinot/pull/16851))
- Performance monitoring metrics ([#16869](https://github.com/apache/pinot/pull/16869))
- Request body handling with payload limits ([#16715](https://github.com/apache/pinot/pull/16715))
- Custom header and JWT identity resolution ([#16743](https://github.com/apache/pinot/pull/16743))
- PathMatcher glob patterns for filtering ([#16723](https://github.com/apache/pinot/pull/16723))
- Component-specific config prefixes ([#16711](https://github.com/apache/pinot/pull/16711))

**Configuration Example:**
```properties
pinot.broker.audit.enabled=true
pinot.broker.audit.identity.header=X-User-Id
pinot.broker.audit.max.payload.size=10240
pinot.broker.audit.include.patterns=/tables/**,/segments/**
```

#### MSE gRPC Authorization
Proposed MSE gRPC channel authorization ([#16475](https://github.com/apache/pinot/pull/16475)) for securing multi-stage query execution.

#### Minion Task Authentication
- MinionContext AuthProvider support ([#17405](https://github.com/apache/pinot/pull/17405))
- Reusable authProvider utilities ([#17626](https://github.com/apache/pinot/pull/17626))
- AuthProvider in segment replacement ([#16978](https://github.com/apache/pinot/pull/16978))

---

### Scalar Functions

#### String Functions
- **INITCAP** - Capitalize first letter of each word ([#17642](https://github.com/apache/pinot/pull/17642))
- Regex and distance functions ([#17372](https://github.com/apache/pinot/pull/17372))
- Enhanced string replace() ([#16528](https://github.com/apache/pinot/pull/16528))

#### Array Functions
- **arrayPushFront/arrayPushBack** - Add elements to arrays ([#17140](https://github.com/apache/pinot/pull/17140))
- **ARRAYHASANY** - Check for any matching elements ([#17156](https://github.com/apache/pinot/pull/17156))
- Enhanced ArrayAgg with MV column support ([#17153](https://github.com/apache/pinot/pull/17153))
- ListAggMv with distinct variant ([#17155](https://github.com/apache/pinot/pull/17155))

#### IP Address Functions
Comprehensive IP address manipulation functions ([#17127](https://github.com/apache/pinot/pull/17127)):
- IP address validation
- CIDR range checking
- IP to integer conversion
- Subnet calculations

#### JSON Functions
- **jsonExtractKey** with depth control and dot notation ([#16306](https://github.com/apache/pinot/pull/16306))
- Null value handling with default support ([#16683](https://github.com/apache/pinot/pull/16683))

#### Mathematical Functions
- NaN and Infinity handling in ROUND_DECIMAL ([#16993](https://github.com/apache/pinot/pull/16993))
- Fixed truncate() signed-zero behavior ([#17677](https://github.com/apache/pinot/pull/17677))
- Null FLOAT handling in roundDecimal() ([#15377](https://github.com/apache/pinot/pull/15377))

#### Logical Functions
New logical operators for Apache Pinot ([#17189](https://github.com/apache/pinot/pull/17189))

#### Ngram Functions
N-gram MV UDFs and refactoring ([#16671](https://github.com/apache/pinot/pull/16671))

#### Function Framework
- **RAND** function with seeded and runtime-only modes ([#17208](https://github.com/apache/pinot/pull/17208))
- Determinism advertising for scalar functions ([#17208](https://github.com/apache/pinot/pull/17208))
- UDF test framework ([#16258](https://github.com/apache/pinot/pull/16258))
- Function polymorphism support ([#16992](https://github.com/apache/pinot/pull/16992))

---

### Indexing & Storage

#### New Index Types

**Raw Bitmap Inverted Index**
High-performance bitmap index creator and reader ([#17060](https://github.com/apache/pinot/pull/17060)) for improved cardinality handling.

**N-gram Index**
Realtime n-gram filtering index ([#16364](https://github.com/apache/pinot/pull/16364)) with comprehensive benchmark results demonstrating 3-5x performance improvement for text search queries.

**Case-Insensitive FST Index**
IFST index type for case-insensitive regex matching ([#16276](https://github.com/apache/pinot/pull/16276)):
```json
{
  "tableIndexConfig": {
    "fstIndexType": "IFST"
  }
}
```

#### Text & Lucene Index
- Combined Lucene text index files merged into columns.psf ([#16688](https://github.com/apache/pinot/pull/16688))
- Minimum should match support ([#16650](https://github.com/apache/pinot/pull/16650))
- Match prefix phrase query parser ([#16476](https://github.com/apache/pinot/pull/16476))
- Lucene doc ID mapping for offline segments ([#17437](https://github.com/apache/pinot/pull/17437))

#### JSON Index
- MAP data type support ([#16808](https://github.com/apache/pinot/pull/16808))
- Unified JSON index reader ([#16436](https://github.com/apache/pinot/pull/16436))

#### Star-Tree Index
- Multi-value column aggregation support ([#16836](https://github.com/apache/pinot/pull/16836))
- Validation for * column with non-COUNT functions ([#17008](https://github.com/apache/pinot/pull/17008))
- Fixed COUNT(col) with null handling ([#17106](https://github.com/apache/pinot/pull/17106))
- Graceful failure handling with rollback ([#17028](https://github.com/apache/pinot/pull/17028))

#### H3 Index
Automatic rebuild on resolution config update ([#16953](https://github.com/apache/pinot/pull/16953))

#### Timestamp Index
Merged SSE and MSE timestamp index implementations ([#17335](https://github.com/apache/pinot/pull/17335))

#### Dictionary & Encoding
- Variable-length dictionary API cleanup ([#17662](https://github.com/apache/pinot/pull/17662))
- BIG_DECIMAL validation fixes ([#16469](https://github.com/apache/pinot/pull/16469))
- v4 as default raw index version ([#16943](https://github.com/apache/pinot/pull/16943))
- No-dictionary stats collector optimizations ([#16845](https://github.com/apache/pinot/pull/16845), [#16967](https://github.com/apache/pinot/pull/16967))

#### Column Readers & Data Sources
- Enhanced null and valid doc ID support ([#17443](https://github.com/apache/pinot/pull/17443))
- InMemoryColumnReader for testing ([#17415](https://github.com/apache/pinot/pull/17415))
- MV primitive type validity bitset ([#17387](https://github.com/apache/pinot/pull/17387))
- Pluggable ForwardIndexReader ([#16363](https://github.com/apache/pinot/pull/16363))
- EmptyIndexBuffer for remote forward index ([#17059](https://github.com/apache/pinot/pull/17059))

#### Data Integrity
- Deterministic data-only CRC computation ([#17264](https://github.com/apache/pinot/pull/17264), [#17380](https://github.com/apache/pinot/pull/17380))
- Skip CRC check during server startup ([#16432](https://github.com/apache/pinot/pull/16432))

---

### Ingestion

#### Kafka Support
- **Kafka 4.x client** support ([#17633](https://github.com/apache/pinot/pull/17633))
- Removed Kafka 2.0 plugin, migrated to Kafka 3.0 ([#17602](https://github.com/apache/pinot/pull/17602))
- Kafka 3 as default consumer ([#16858](https://github.com/apache/pinot/pull/16858))
- Confluent library 7.9.5 matching Kafka 3.9.1 ([#17614](https://github.com/apache/pinot/pull/17614))
- Multi-level Kafka admin client reuse ([#16620](https://github.com/apache/pinot/pull/16620))
- Enhanced metrics support in JMX exporter ([#16745](https://github.com/apache/pinot/pull/16745))

#### Stream Consumption
- Fixed race conditions in stream consumer ([#17089](https://github.com/apache/pinot/pull/17089))
- Segment partition tracking for multi-topic ingestion ([#17217](https://github.com/apache/pinot/pull/17217))
- Optimized multi-topic ingestion ([#16833](https://github.com/apache/pinot/pull/16833))
- Freshness check improvements ([#17560](https://github.com/apache/pinot/pull/17560), [#17563](https://github.com/apache/pinot/pull/17563))
- Minimum ingestion lag for freshness checkers ([#17598](https://github.com/apache/pinot/pull/17598))

#### Pulsar Support
Optimized partition query elimination ([#17379](https://github.com/apache/pinot/pull/17379))

#### Data Format Support

**Avro:**
- Support for header-prefixed payloads (Confluent Schema Registry) ([#17077](https://github.com/apache/pinot/pull/17077))
- Upgraded to Avro 1.12.1 ([#16729](https://github.com/apache/pinot/pull/16729))

**Protobuf:**
- Field descriptor caching optimization ([#17593](https://github.com/apache/pinot/pull/17593))

**JSON:**
- Direct Map parsing optimization ([#17485](https://github.com/apache/pinot/pull/17485))

**Parquet:**
- Input format resolution fixes ([#17601](https://github.com/apache/pinot/pull/17601))
- Record reader config support ([#17280](https://github.com/apache/pinot/pull/17280))

#### Consumption Control
- Batch pause consumption ([#17194](https://github.com/apache/pinot/pull/17194))
- Pauseless consumption validation for multi-topic ([#16507](https://github.com/apache/pinot/pull/16507))

#### Offset Management
- Auto reset during ingestion lag ([#16492](https://github.com/apache/pinot/pull/16492))
- Topic inactive status ([#16692](https://github.com/apache/pinot/pull/16692))
- Auto reset enabled by default ([#15736](https://github.com/apache/pinot/pull/15736))
- Offset and age as long type ([#16614](https://github.com/apache/pinot/pull/16614))

#### Virtual Columns
**$partitionId** virtual column ([#16721](https://github.com/apache/pinot/pull/16721)) for partition-aware queries.

#### Error Handling & Logging
- Suppressed data logging during segment creation ([#17656](https://github.com/apache/pinot/pull/17656))
- Graceful handling of bad segment time data ([#16462](https://github.com/apache/pinot/pull/16462))
- Rate-limited transformer exceptions ([#17239](https://github.com/apache/pinot/pull/17239))
- Record inclusion when filter transform throws ([#17218](https://github.com/apache/pinot/pull/17218))

#### Metrics
- End-to-end ingestion delay metric ([#17163](https://github.com/apache/pinot/pull/17163))
- Fixed realtime ingestion metrics ([#16783](https://github.com/apache/pinot/pull/16783))

---

### Minion Tasks

#### Task Management APIs
- **GET /minions/status** endpoint ([#17475](https://github.com/apache/pinot/pull/17475))
- All minion tasks summary API ([#17330](https://github.com/apache/pinot/pull/17330))
- State and table filtering for task counts ([#16433](https://github.com/apache/pinot/pull/16433))
- Table-based subtask filtering ([#16977](https://github.com/apache/pinot/pull/16977))

#### Performance & Metrics
- Subtask timing metrics ([#17190](https://github.com/apache/pinot/pull/17190))
- Fixed metrics collection gaps ([#17128](https://github.com/apache/pinot/pull/17128))

#### Task Execution
- Isolated long-running task API resources ([#17494](https://github.com/apache/pinot/pull/17494))
- Configurable max tasks per instance ([#16981](https://github.com/apache/pinot/pull/16981))
- Per-tenant numConcurrentTasksPerInstance ([#16777](https://github.com/apache/pinot/pull/16777))

#### Task Scheduling
- Table-level distributed locking ([#16857](https://github.com/apache/pinot/pull/16857))
- Refactored PinotTaskManager ([#17459](https://github.com/apache/pinot/pull/17459))
- Overridable createTaskManager ([#17495](https://github.com/apache/pinot/pull/17495))

#### Minion Instance Management
- Drained minion tag support ([#17375](https://github.com/apache/pinot/pull/17375))
- PodDisruptionBudget for minion/minion-stateless ([#17538](https://github.com/apache/pinot/pull/17538))

#### Deep Store Operations
- **/uploadFromServerToDeepstore** endpoint ([#17197](https://github.com/apache/pinot/pull/17197))
- Deep store upload retry enabled by default for Pauseless ([#17241](https://github.com/apache/pinot/pull/17241))

#### Task Cleanup
Automatic task data cleanup on table deletion ([#16307](https://github.com/apache/pinot/pull/16307))

---

### API & UI Improvements

#### Controller UI Enhancements

**Table Management:**
- Delete table and schema from UI ([#17294](https://github.com/apache/pinot/pull/17294))
- Fixed segment search bar width ([#17520](https://github.com/apache/pinot/pull/17520))
- Primary key display in schema UI ([#17566](https://github.com/apache/pinot/pull/17566))

**Minion Task Dashboard:**
- Comprehensive task stats and filtering ([#16521](https://github.com/apache/pinot/pull/16521))
- Subtask status filters ([#16591](https://github.com/apache/pinot/pull/16591))
- Debug tables surfaced ([#16968](https://github.com/apache/pinot/pull/16968))
- TASK_ERROR status filter ([#16609](https://github.com/apache/pinot/pull/16609))

**Instance Health:**
- Distinguished liveness vs health status ([#17450](https://github.com/apache/pinot/pull/17450), [#17248](https://github.com/apache/pinot/pull/17248))

**Zookeeper Browser:**
- Fixed duplicate nav entry ([#17500](https://github.com/apache/pinot/pull/17500))
- QuickStart support ([#17490](https://github.com/apache/pinot/pull/17490))

**Segment Reload Tracking:**
- Segment-level failure details ([#17234](https://github.com/apache/pinot/pull/17234))
- In-memory status cache ([#17099](https://github.com/apache/pinot/pull/17099))
- Type-safe API refactoring ([#17051](https://github.com/apache/pinot/pull/17051))

**Navigation:**
- Header links next to cluster name ([#17499](https://github.com/apache/pinot/pull/17499))

**Time Zone:**
- Enhanced time zone support with segment time display ([#16489](https://github.com/apache/pinot/pull/16489))

#### REST API Improvements
- Storage quota check for batch upload ([#17512](https://github.com/apache/pinot/pull/17512))
- HTTP response codes for errors ([#17341](https://github.com/apache/pinot/pull/17341))
- Tenant rebalance jobs API ([#16376](https://github.com/apache/pinot/pull/16376))
- ZooKeeper PUT via request body ([#16809](https://github.com/apache/pinot/pull/16809))

#### Client Libraries
- **Cursor-based pagination** in Java client ([#16782](https://github.com/apache/pinot/pull/16782))
- Fixed JDBC driver metadata retrieval ([#17275](https://github.com/apache/pinot/pull/17275))

#### Spark Connector
- gRPC support with Pinot Proxy ([#16666](https://github.com/apache/pinot/pull/16666))
- Live brokers API integration ([#14802](https://github.com/apache/pinot/pull/14802))
- Fixed gRPC shading ([#17000](https://github.com/apache/pinot/pull/17000), [#16923](https://github.com/apache/pinot/pull/16923))

---

### Performance Optimizations

#### Query Performance

**Aggregation Optimizations:**
- LONG-specific MIN/MAX/SUM functions ([#17001](https://github.com/apache/pinot/pull/17001), [#17007](https://github.com/apache/pinot/pull/17007))
- Optimized SumInt for INT columns ([#16704](https://github.com/apache/pinot/pull/16704))
- Enhanced ValueAggregator ([#16552](https://github.com/apache/pinot/pull/16552))

**Distinct Count:**
- Smart HLL-based distinct count ([#17011](https://github.com/apache/pinot/pull/17011))
- ULL smart promotion ([#16811](https://github.com/apache/pinot/pull/16811))
- Hybrid exact/HLL cardinality ([#17186](https://github.com/apache/pinot/pull/17186))

**Predicate Optimizations:**
- 2-bitset REGEXP_LIKE evaluator ([#16922](https://github.com/apache/pinot/pull/16922))
- Dictionary scan for small dictionaries in REGEX ([#16478](https://github.com/apache/pinot/pull/16478))
- Configurable DICTIONARY vs RAW scan ([#16879](https://github.com/apache/pinot/pull/16879))

**Memory & Resource Management:**
- Fixed off-heap memory spike ([#17489](https://github.com/apache/pinot/pull/17489))
- Reduced mailbox memory footprint ([#16872](https://github.com/apache/pinot/pull/16872))
- Stats preservation during pipeline breaker ([#17576](https://github.com/apache/pinot/pull/17576))
- Eliminated bytes copy in MessageDecoder ([#16349](https://github.com/apache/pinot/pull/16349))

**Thread & CPU Accounting:**
- Removed System.gc() calls ([#16374](https://github.com/apache/pinot/pull/16374))
- Correlation ID for CPU accounting ([#16040](https://github.com/apache/pinot/pull/16040))
- Enhanced thread accounting setup ([#17541](https://github.com/apache/pinot/pull/17541))

**Workload Isolation:**
- Workload Scheduler ([#16018](https://github.com/apache/pinot/pull/16018))
- Workload stats collection interface ([#16340](https://github.com/apache/pinot/pull/16340))
- Additional sampling for broker and server ([#16164](https://github.com/apache/pinot/pull/16164))
- Cost-split support ([#16672](https://github.com/apache/pinot/pull/16672))

**Group By Optimizations:**
- Reduced allocation in group ID generation ([#16798](https://github.com/apache/pinot/pull/16798))
- ThreadLocal cleanup ([#16454](https://github.com/apache/pinot/pull/16454))

**Routing:**
- Pluggable query routing strategy ([#17364](https://github.com/apache/pinot/pull/17364))
- Parallel routing table builds ([#16791](https://github.com/apache/pinot/pull/16791))
- Per-table routing build locks ([#16585](https://github.com/apache/pinot/pull/16585))
- Pluggable table samplers ([#17532](https://github.com/apache/pinot/pull/17532))

**Early Termination:**
- AND/OR short circuit optimizations ([#16583](https://github.com/apache/pinot/pull/16583))
- Improved DESC order by for sorted columns ([#16789](https://github.com/apache/pinot/pull/16789))

---

### Notable Bug Fixes

#### Query Bugs
- Fixed empty table handling with useLeafServerForIntermediateStage ([#17634](https://github.com/apache/pinot/pull/17634))
- Column alias respect in empty responses ([#17143](https://github.com/apache/pinot/pull/17143))
- SUM rewrite under null handling ([#17338](https://github.com/apache/pinot/pull/17338))
- NPE guard for MSE filtered aggregations ([#17386](https://github.com/apache/pinot/pull/17386))

#### Storage & Segment Bugs
- Mixed-type map key promotion to string ([#17722](https://github.com/apache/pinot/pull/17722))
- TableRebalancer NPE fix ([#17723](https://github.com/apache/pinot/pull/17723))
- DirectOOMHandler string matching ([#17684](https://github.com/apache/pinot/pull/17684))
- S3 URL encoding for S3-compatible storage ([#17691](https://github.com/apache/pinot/pull/17691))
- GcsPinotFS deleteBatch resiliency ([#17713](https://github.com/apache/pinot/pull/17713))

#### Rebalance & Assignment
- Pool-based assignment enforcement ([#17276](https://github.com/apache/pinot/pull/17276))
- COMMITTING segment handling ([#16706](https://github.com/apache/pinot/pull/16706), [#16348](https://github.com/apache/pinot/pull/16348))

#### Metrics & Monitoring
- Fixed broker metrics for gRPC ([#16870](https://github.com/apache/pinot/pull/16870))
- Server metric initialization ([#16850](https://github.com/apache/pinot/pull/16850))
- GRPC_BYTES_SENT metric fix ([#17422](https://github.com/apache/pinot/pull/17422))

---

### Dependency Updates

**Major Version Upgrades:**
- Jackson: 2.19.2 → 2.21.0
- Testcontainers: 1.x → 2.x
- Parquet: 1.15.2 → 1.17.0
- gRPC: 1.73.0 → 1.79.0
- Kafka: Default consumer moved to Kafka 3.x
- Apache Pulsar: 4.0.5 → 4.0.8
- Hadoop: 3.4.1 → 3.4.2
- Bouncy Castle: 1.78.1 → 1.83
- Avro: 1.12.0 → 1.12.1

**AWS SDK:**
- Continuous updates from 2.31.78 → 2.41.32

**Google Cloud:**
- libraries-bom: 26.63.0 → 26.74.0

**Azure SDK:**
- 1.2.36 → 1.3.4

**Build Tools:**
- Maven: 3.9.10 → 3.9.12
- Gradle Develocity: 2.0.1 → 2.3.4

**Testing:**
- TestNG: 7.11.0 → 7.12.0
- AssertJ: 3.27.3 → 3.27.7

**See full dependency update list in [Dependencies](#dependency-updates) section.**

---

### Infrastructure & Testing

#### CI/CD
- Retry wrapper for fast-failing Maven steps ([#17316](https://github.com/apache/pinot/pull/17316))
- Surefire report generation and artifact upload ([#17076](https://github.com/apache/pinot/pull/17076))

#### Compatibility
- Backward compatibility verification for 1.4.0 ([#17073](https://github.com/apache/pinot/pull/17073))
- Removed 1.3.0 compatibility verifiers ([#17649](https://github.com/apache/pinot/pull/17649), [#17693](https://github.com/apache/pinot/pull/17693))

#### Code Quality
- Automatic package-info.java generation ([#17327](https://github.com/apache/pinot/pull/17327))
- ImmutableMap → Map.of() migration with Checkstyle enforcement ([#17091](https://github.com/apache/pinot/pull/17091))
- ImmutableList → java.util.List migration ([#17101](https://github.com/apache/pinot/pull/17101))
- Copilot instructions and AI tooling support ([#16652](https://github.com/apache/pinot/pull/16652), [#16693](https://github.com/apache/pinot/pull/16693))

#### Helm Charts
- Version 0.3.5 with Bitnami ZooKeeper fixes ([#16985](https://github.com/apache/pinot/pull/16985))

---

## Migration Notes

### Breaking Changes

1. **Kafka Default Consumer:**
   - Default Kafka consumer changed from Kafka 2.x to Kafka 3.x
   - Kafka 2.0 plugin removed
   - **Action Required:** Update Kafka client configurations if using custom settings

2. **Dictionary API Changes:**
   - `DictionaryIndexConfig.getUseVarLengthDictionary()` renamed to `isUseVarLengthDictionary()`
   - **Action Required:** Update custom code using this API

3. **Raw Index Version:**
   - Default raw index version changed to v4
   - **Action Required:** Existing segments remain compatible; new segments use v4

4. **Disabled Planner Rules:**
   - `JOIN_PUSH_TRANSITIVE_PREDICATES` and `AggregateUnionAggregate` disabled by default
   - **Action Required:** Re-enable via broker config if needed:
     ```properties
     pinot.broker.enable.query.limit.override=false
     ```

5. **Upsert Restrictions:**
   - Stricter validation for `dropOutOfOrderRecord` with `consistencyMode`
   - **Action Required:** Ensure consistency mode is set appropriately

### Deprecated Features

1. **Preferred Replicas Query Option:**
   - Deprecated in favor of new routing strategies
   - Will be removed in 2.0.0

2. **Stale Instances Cleanup Task:**
   - Disabled by default
   - Enable explicitly if needed

3. **Old Minion Cleanup Configs:**
   - Deprecated configs for `MinionInstancesCleanupTask`
   - Use new cluster-level configurations

### Recommended Upgrades

1. **Enable Federation:** If managing multiple clusters, enable federation for unified query experience
2. **Audit Logging:** Enable audit logging for compliance and security monitoring
3. **Query Fingerprinting:** Enable for query optimization insights
4. **Time Series:** Migrate to Time Series Engine for time series workloads
5. **Commit Time Compaction:** Enable for upsert tables to reduce storage

---

## Getting Started

### QuickStart

```bash
# Download Apache Pinot 1.5.0
wget https://downloads.apache.org/pinot/apache-pinot-1.5.0/apache-pinot-1.5.0-bin.tar.gz
tar -xzf apache-pinot-1.5.0-bin.tar.gz
cd apache-pinot-1.5.0-bin

# Start Pinot cluster
bin/pinot-admin.sh QuickStart -type batch

# Try Federation QuickStart
bin/pinot-admin.sh QuickStart -type MULTI_CLUSTER

# Try Logical Tables QuickStart
bin/pinot-admin.sh QuickStart -type LOGICAL_TABLE
```

### Documentation

- [Apache Pinot Documentation](https://docs.pinot.apache.org/)
- [Federation Guide](https://docs.pinot.apache.org/operators/operating-pinot/federation)
- [Time Series Engine](https://docs.pinot.apache.org/users/user-guide-query/timeseries)
- [Upsert Documentation](https://docs.pinot.apache.org/basics/data-import/upsert)
- [Security Guide](https://docs.pinot.apache.org/operators/operating-pinot/security)

---

## Contributors

This release includes **1,006 commits** from our amazing community of contributors. Special thanks to all who made this release possible!

**Release Manager:** [TBD]

For the complete list of contributors, see the [commit history](https://github.com/apache/pinot/compare/release-1.4.0...release-1.5.0).

---

## Known Issues

1. **Testcontainers 2.x Migration:** Some integration tests may require Docker configuration updates
2. **Federation (Beta):** Multi-cluster federation is in Beta; extensive testing recommended before production use
3. **MSE gRPC Authorization:** Proposed but not yet fully implemented in this release

---

## Feedback & Support

- **Mailing List:** dev@pinot.apache.org
- **Slack:** [Apache Pinot Slack](https://communityinviter.com/apps/apache-pinot/apache-pinot)
- **GitHub Issues:** https://github.com/apache/pinot/issues
- **Stack Overflow:** Tag questions with `apache-pinot`

---

## What's Next

Looking ahead to Apache Pinot 1.6.0:
- Enhanced federation capabilities moving to GA
- Additional time series optimizations
- Extended security features
- Performance improvements for large-scale deployments

Stay tuned to our [mailing list](mailto:dev@pinot.apache.org) for updates!

---

**Apache Pinot 1.5.0** - Making real-time analytics faster, more secure, and more powerful.

Released: [Release Date]

[Download](https://downloads.apache.org/pinot/apache-pinot-1.5.0/) | [Release Notes](https://github.com/apache/pinot/releases/tag/release-1.5.0) | [Documentation](https://docs.pinot.apache.org/)
