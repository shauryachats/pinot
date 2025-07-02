# Timeseries Query Integration

This document describes the integration of timeseries query functionality into the Pinot Controller UI, providing a seamless interface for executing timeseries queries through the Pinot timeseries engine.

## Overview

The timeseries query integration consists of:

1. **Controller API Endpoints** - New REST endpoints in the controller that route timeseries queries to brokers
2. **Frontend Modal** - A React component for building and executing timeseries queries
3. **API Integration** - Frontend API calls that communicate with the controller endpoints

## Architecture

```
Frontend (TimeseriesQueryModal)
    ↓
Controller (/timeseries/api/v1/query_range)
    ↓
Broker (/timeseries/api/v1/query_range)
    ↓
Timeseries Engine (M3QL, etc.)
```

## Controller Integration

### New Endpoints Added

The controller now includes timeseries query endpoints that follow the same pattern as existing SQL query endpoints:

#### GET `/timeseries/api/v1/query_range`
- **Parameters:**
  - `language` - Query language (e.g., "m3ql")
  - `query` - The timeseries query string
  - `start` - Start time in Unix timestamp
  - `end` - End time in Unix timestamp
  - `step` - Time step for aggregation

#### POST `/timeseries/api/v1/query_range`
- **Request Body:** JSON with the same parameters as GET
- **Example:**
```json
{
  "language": "m3ql",
  "query": "SELECT COUNT(*) FROM my_table WHERE timestamp >= 1672531200 AND timestamp <= 1704067199",
  "start": "1672531200",
  "end": "1704067199",
  "step": "1h"
}
```

### Implementation Details

The controller routes timeseries queries to brokers using the same infrastructure as SQL queries:

1. **Broker Selection** - Selects a random online broker instance
2. **Request Forwarding** - Forwards the request to the selected broker
3. **Response Handling** - Returns the broker response to the client

## Frontend Integration

### TimeseriesQueryModal Component

A comprehensive modal component that provides:

- **Query Language Selection** - Dropdown for supported languages (currently M3QL)
- **Table Selection** - Dropdown populated with available tables
- **Time Range Configuration** - Unix timestamp inputs for start/end times
- **Query Input** - Plain text area for entering timeseries queries
- **Query Options** - Tracing, multi-stage engine, timeout settings
- **Sample Query Generation** - Auto-generates sample queries
- **Raw Output Display** - Shows query results in JSON format

### Key Features

1. **Configurable Query Languages**
   ```typescript
   const SUPPORTED_QUERY_LANGUAGES = [
     { value: 'm3ql', label: 'M3QL' },
     // Easy to add more languages:
     // { value: 'promql', label: 'PromQL' },
     // { value: 'influxql', label: 'InfluxQL' },
   ];
   ```

2. **Time Range Helpers**
   - Current timestamp display
   - One hour ago timestamp
   - Unix timestamp validation

3. **Sample Query Generation**
   - Automatically generates M3QL queries based on selected table and time range
   - Includes proper time filtering and aggregation

4. **Error Handling**
   - Comprehensive error display
   - API error response formatting
   - User-friendly error messages

### API Integration

The frontend uses the `getTimeSeriesQueryResult` function to communicate with the controller:

```typescript
export const getTimeSeriesQueryResult = (params: Object): Promise<AxiosResponse<any>> =>
  transformApi.post(`/timeseries/api/v1/query_range`, params, {headers});
```

## Usage Examples

### Basic M3QL Query

```m3ql
SELECT COUNT(*) FROM my_timeseries_table
WHERE timestamp >= 1672531200
  AND timestamp <= 1704067199
GROUP BY time_bucket(1h, timestamp)
```

### Query with Aggregation

```m3ql
SELECT AVG(value), MAX(value), MIN(value)
FROM metrics_table
WHERE timestamp >= 1672531200
  AND timestamp <= 1704067199
  AND metric_name = 'cpu_usage'
GROUP BY time_bucket(5m, timestamp)
```

## Integration Points

### Query Page Integration

The timeseries modal is integrated into the main Query page:

1. **Button Access** - "Timeseries Query" button in the query interface
2. **Modal Integration** - Opens the TimeseriesQueryModal component
3. **Result Handling** - Results displayed within the modal

### Extensibility

The implementation is designed for easy extension:

1. **New Query Languages** - Add to `SUPPORTED_QUERY_LANGUAGES` array
2. **Additional Query Options** - Extend the `TimeseriesQueryConfig` interface
3. **Custom Query Templates** - Add to the sample query generation logic

## Configuration

### Controller Configuration

The controller uses existing configuration for broker communication:

- `controller.broker.protocol` - Protocol for broker communication
- `controller.broker.port.override` - Optional broker port override
- `controller.multi.stage.engine.enabled` - Multi-stage engine configuration

### Frontend Configuration

No additional configuration required - the modal uses existing table and schema data from the Pinot cluster.

## Testing

### Unit Tests

The implementation includes comprehensive unit tests covering:

- Modal rendering and interaction
- Query generation
- Time range handling
- Error scenarios
- API integration

### Integration Testing

To test the complete flow:

1. Start a Pinot cluster with timeseries support enabled
2. Create a timeseries-enabled table
3. Open the Query page in the controller UI
4. Click "Timeseries Query" button
5. Configure and execute a timeseries query
6. Verify results are displayed correctly

## Error Handling

### Common Error Scenarios

1. **No Online Brokers** - "No online broker found for timeseries query"
2. **Invalid Query** - Query parsing errors from the timeseries engine
3. **Network Issues** - Connection timeouts and network errors
4. **Authentication** - Access control and permission errors

### Error Display

Errors are displayed in the modal's raw output section with:

- Error type and message
- Detailed error information
- Timestamp of the error
- Suggestions for resolution

## Future Enhancements

### Planned Features

1. **Additional Query Languages**
   - PromQL support
   - InfluxQL support
   - Flux support

2. **Advanced Query Builder**
   - Visual query builder interface
   - Drag-and-drop query construction
   - Query templates and saved queries

3. **Result Visualization**
   - Time series charts
   - Interactive graphs
   - Export capabilities

4. **Query History**
   - Saved query templates
   - Query execution history
   - Performance metrics

### Performance Optimizations

1. **Query Caching** - Cache frequently used queries
2. **Lazy Loading** - Load table schemas on demand
3. **Connection Pooling** - Optimize broker connections
4. **Response Streaming** - Stream large result sets

## Troubleshooting

### Common Issues

1. **Modal Not Opening**
   - Check browser console for JavaScript errors
   - Verify React component imports

2. **API Calls Failing**
   - Check network connectivity
   - Verify controller endpoints are accessible
   - Check broker availability

3. **Query Execution Errors**
   - Verify table exists and is timeseries-enabled
   - Check query syntax for the selected language
   - Validate time range parameters

### Debug Information

Enable debug logging by setting:

```bash
# Controller debug logging
export PINOT_CONTROLLER_LOG_LEVEL=DEBUG

# Frontend debug logging
# Check browser console for detailed error information
```

## Contributing

When extending the timeseries query functionality:

1. **Follow Existing Patterns** - Use the same patterns as SQL query integration
2. **Add Tests** - Include unit tests for new features
3. **Update Documentation** - Keep this README current
4. **Error Handling** - Implement comprehensive error handling
5. **Backward Compatibility** - Maintain compatibility with existing features
