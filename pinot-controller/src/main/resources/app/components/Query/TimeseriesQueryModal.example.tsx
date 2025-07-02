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

import React, { useState } from 'react';
import { Button, Box, Typography, Paper } from '@material-ui/core';
import TimeseriesQueryModal from './TimeseriesQueryModal';
import { TableData } from 'Models';

// Example data for demonstration
const mockTableList: TableData = {
  columns: ['Tables'],
  records: [
    ['events_table'],
    ['metrics_table'],
    ['logs_table'],
    ['timeseries_data']
  ]
};

const mockTableSchema: TableData = {
  columns: ['Column Name', 'Data Type'],
  records: [
    ['timestamp'],
    ['value'],
    ['metric_name'],
    ['host'],
    ['service']
  ]
};

/**
 * Example component demonstrating how to integrate the TimeseriesQueryModal
 */
const TimeseriesQueryExample: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const [lastQuery, setLastQuery] = useState<string>('');
  const [lastConfig, setLastConfig] = useState<any>(null);

  const handleExecute = (query: string, config: any) => {
    console.log('Timeseries query executed:', { query, config });
    setLastQuery(query);
    setLastConfig(config);
  };

  const handleClose = () => {
    setModalOpen(false);
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Timeseries Query Modal Example
      </Typography>

      <Paper style={{ padding: '20px', marginBottom: '20px' }}>
        <Typography variant="h6" gutterBottom>
          Integration Example
        </Typography>

        <Button
          variant="contained"
          color="primary"
          onClick={() => setModalOpen(true)}
          style={{ marginBottom: '20px' }}
        >
          Open Timeseries Query Modal
        </Button>

        <Typography variant="body2" color="textSecondary">
          Click the button above to open the timeseries query modal. The modal provides:
        </Typography>

        <ul>
          <li>Table selection from available tables</li>
          <li>Query language selection (M3QL)</li>
          <li>Time range configuration with Unix timestamps</li>
          <li>Query input with sample query generation</li>
          <li>Query options (tracing, multi-stage engine, timeout)</li>
          <li>Raw output display</li>
        </ul>
      </Paper>

      {lastQuery && (
        <Paper style={{ padding: '20px', marginBottom: '20px' }}>
          <Typography variant="h6" gutterBottom>
            Last Executed Query
          </Typography>

          <Typography variant="subtitle2" gutterBottom>
            Query:
          </Typography>
          <pre style={{
            backgroundColor: '#f5f5f5',
            padding: '10px',
            borderRadius: '4px',
            overflow: 'auto'
          }}>
            {lastQuery}
          </pre>

          <Typography variant="subtitle2" gutterBottom style={{ marginTop: '10px' }}>
            Configuration:
          </Typography>
          <pre style={{
            backgroundColor: '#f5f5f5',
            padding: '10px',
            borderRadius: '4px',
            overflow: 'auto'
          }}>
            {JSON.stringify(lastConfig, null, 2)}
          </pre>
        </Paper>
      )}

      <TimeseriesQueryModal
        open={modalOpen}
        handleClose={handleClose}
        handleExecute={handleExecute}
        tableList={mockTableList}
        tableSchema={mockTableSchema}
        selectedTable="timeseries_data"
      />
    </Box>
  );
};

/**
 * Example showing how to extend the modal with custom query languages
 */
const ExtendedTimeseriesQueryExample: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);

  // Example of how to extend the modal with additional query languages
  const extendedTableList: TableData = {
    columns: ['Tables'],
    records: [
      ['prometheus_metrics'],
      ['influxdb_data'],
      ['custom_timeseries']
    ]
  };

  const handleExecute = (query: string, config: any) => {
    console.log('Extended timeseries query executed:', { query, config });

    // Example of handling different query languages
    switch (config.queryLanguage) {
      case 'm3ql':
        console.log('Executing M3QL query:', query);
        break;
      case 'promql':
        console.log('Executing PromQL query:', query);
        break;
      case 'influxql':
        console.log('Executing InfluxQL query:', query);
        break;
      default:
        console.log('Executing query in language:', config.queryLanguage);
    }
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Extended Timeseries Query Example
      </Typography>

      <Paper style={{ padding: '20px', marginBottom: '20px' }}>
        <Typography variant="h6" gutterBottom>
          Multi-Language Support Example
        </Typography>

        <Typography variant="body2" color="textSecondary" gutterBottom>
          This example demonstrates how the modal can be extended to support multiple query languages.
          To add new languages, you would:
        </Typography>

        <ol>
          <li>Update the SUPPORTED_QUERY_LANGUAGES array in the modal component</li>
          <li>Add language-specific sample query generation</li>
          <li>Implement language-specific query validation</li>
          <li>Add language-specific result formatting</li>
        </ol>

        <Button
          variant="contained"
          color="secondary"
          onClick={() => setModalOpen(true)}
          style={{ marginTop: '10px' }}
        >
          Open Extended Modal
        </Button>
      </Paper>

      <TimeseriesQueryModal
        open={modalOpen}
        handleClose={() => setModalOpen(false)}
        handleExecute={handleExecute}
        tableList={extendedTableList}
        tableSchema={mockTableSchema}
        selectedTable="prometheus_metrics"
      />
    </Box>
  );
};

/**
 * Example showing how to integrate with the main Query page
 */
const QueryPageIntegrationExample: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);

  const handleExecute = async (query: string, config: any) => {
    console.log('Query page integration - timeseries query executed:', { query, config });

    // Example of how the main Query page would handle timeseries queries
    // This could include:
    // - Updating the main query interface
    // - Storing query history
    // - Updating query statistics
    // - Triggering result visualization

    setModalOpen(false);
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Query Page Integration Example
      </Typography>

      <Paper style={{ padding: '20px' }}>
        <Typography variant="h6" gutterBottom>
          Main Query Page Integration
        </Typography>

        <Typography variant="body2" color="textSecondary" gutterBottom>
          This example shows how the timeseries modal integrates with the main Query page.
          The integration includes:
        </Typography>

        <ul>
          <li>Button in the main query interface</li>
          <li>Modal opens when button is clicked</li>
          <li>Results are displayed within the modal</li>
          <li>Query execution is handled separately from SQL queries</li>
        </ul>

        <Button
          variant="contained"
          color="primary"
          onClick={() => setModalOpen(true)}
          style={{ marginTop: '10px' }}
        >
          Timeseries Query
        </Button>
      </Paper>

      <TimeseriesQueryModal
        open={modalOpen}
        handleClose={() => setModalOpen(false)}
        handleExecute={handleExecute}
        tableList={mockTableList}
        tableSchema={mockTableSchema}
        selectedTable="events_table"
      />
    </Box>
  );
};

export {
  TimeseriesQueryExample,
  ExtendedTimeseriesQueryExample,
  QueryPageIntegrationExample
};

export default TimeseriesQueryExample;
