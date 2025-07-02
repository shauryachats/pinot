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

import React, { useState, useEffect } from 'react';
import {
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Checkbox,
  FormControlLabel,
  Typography,
  Box,
  Divider,
  makeStyles,
  Paper
} from '@material-ui/core';
import CustomDialog from '../CustomDialog';
import { TableData } from 'Models';
import { getTimeSeriesQueryResult } from '../../requests';

const useStyles = makeStyles((theme) => ({
  root: {
    minWidth: '800px',
    maxWidth: '1200px',
  },
  section: {
    marginBottom: theme.spacing(3),
  },
  sectionTitle: {
    marginBottom: theme.spacing(2),
    fontWeight: 600,
    color: theme.palette.primary.main,
  },
  formControl: {
    margin: theme.spacing(1),
    minWidth: 200,
  },
  textField: {
    margin: theme.spacing(1),
  },
  queryTextArea: {
    width: '100%',
    minHeight: '150px',
    fontFamily: 'monospace',
    fontSize: '13px',
    padding: theme.spacing(1),
    border: '1px solid #BDCCD9',
    borderRadius: '4px',
    resize: 'vertical',
  },
  timeInput: {
    fontFamily: 'monospace',
  },
  queryPreview: {
    backgroundColor: '#f5f5f5',
    padding: theme.spacing(2),
    borderRadius: theme.spacing(1),
    marginTop: theme.spacing(2),
  },
  queryPreviewTitle: {
    fontWeight: 600,
    marginBottom: theme.spacing(1),
  },
  queryPreviewContent: {
    fontFamily: 'monospace',
    fontSize: '12px',
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-all',
  },
  rawOutput: {
    backgroundColor: '#f8f9fa',
    border: '1px solid #dee2e6',
    borderRadius: '4px',
    padding: theme.spacing(2),
    fontFamily: 'monospace',
    fontSize: '12px',
    whiteSpace: 'pre-wrap',
    maxHeight: '400px',
    overflowY: 'auto',
  },
}));

// Configurable supported query languages
const SUPPORTED_QUERY_LANGUAGES = [
  { value: 'm3ql', label: 'M3QL' },
  // Easy to add more languages in the future:
  // { value: 'promql', label: 'PromQL' },
  // { value: 'influxql', label: 'InfluxQL' },
  // { value: 'flux', label: 'Flux' },
];

interface TimeseriesQueryConfig {
  queryLanguage: string;
  query: string;
  startTime: string;
  endTime: string;
  useMultiStageEngine: boolean;
  tracing: boolean;
  timeout: number;
}

interface TimeseriesQueryModalProps {
  open: boolean;
  handleClose: (event: React.MouseEvent<HTMLElement, MouseEvent>) => void;
  handleExecute: (query: string, config: TimeseriesQueryConfig) => void;
  tableList: TableData;
  tableSchema: TableData;
  selectedTable: string;
}

const TimeseriesQueryModal: React.FC<TimeseriesQueryModalProps> = ({
  open,
  handleClose,
  handleExecute,
  tableList,
  tableSchema,
  selectedTable,
}) => {
  const classes = useStyles();

  const [config, setConfig] = useState<TimeseriesQueryConfig>({
    queryLanguage: 'm3ql',
    query: '',
    startTime: '',
    endTime: '',
    useMultiStageEngine: false,
    tracing: false,
    timeout: 60000,
  });

  const [rawOutput, setRawOutput] = useState<string>('');



  const handleConfigChange = (field: keyof TimeseriesQueryConfig, value: any) => {
    setConfig(prev => ({ ...prev, [field]: value }));
  };

  const handleQueryChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    setConfig(prev => ({ ...prev, query: event.target.value }));
  };

  const getCurrentTimestamp = () => {
    return Math.floor(Date.now() / 1000).toString();
  };

  const getOneHourAgoTimestamp = () => {
    return Math.floor((Date.now() - 60 * 60 * 1000) / 1000).toString();
  };

  const handleExecuteQuery = async () => {
    if (config.query.trim()) {
      try {
        // Prepare the request payload
        const requestPayload = {
          language: config.queryLanguage,
          query: config.query,
          start: config.startTime,
          end: config.endTime,
          // step: '1m', // Default step, could be made configurable
          // trace: config.tracing,
          // queryOptions: `timeoutMs=${config.timeout}${config.useMultiStageEngine ? ',useMultistageEngine=true' : ''}`
        };

        // Call the timeseries API
        const response = await getTimeSeriesQueryResult(requestPayload);

        // Display the raw response
        setRawOutput(JSON.stringify(response.data, null, 2));

        // Call the parent handler
        handleExecute(config.query, config);
      } catch (error) {
        console.error('Error executing timeseries query:', error);
        const errorOutput = {
          error: true,
          message: error.response?.data?.message || error.message || 'Unknown error occurred',
          details: error.response?.data || error,
          timestamp: new Date().toISOString()
        };
        setRawOutput(JSON.stringify(errorOutput, null, 2));
      }
    }
  };

  const generateSampleQuery = () => {
    const sampleQueries = {
      m3ql: `SELECT COUNT(*) FROM your_table_name
WHERE timestamp >= ${config.startTime || getOneHourAgoTimestamp()}
  AND timestamp <= ${config.endTime || getCurrentTimestamp()}
GROUP BY time_bucket(1h, timestamp)`,
    };

    const sampleQuery = sampleQueries[config.queryLanguage as keyof typeof sampleQueries] || '';
    setConfig(prev => ({ ...prev, query: sampleQuery }));
  };

  return (
    <CustomDialog
      open={open}
      handleClose={handleClose}
      handleSave={handleExecuteQuery}
      title="Timeseries Query Builder"
      btnOkText="Run Query"
      btnCancelText="Cancel"
      size="lg"
      showTitleDivider
      showFooterDivider
    >
      <div className={classes.root}>
        {/* Table and Query Language Configuration */}
        <div className={classes.section}>
          <Typography variant="h6" className={classes.sectionTitle}>
            Table & Query Language Configuration
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <FormControl className={classes.formControl} fullWidth>
                <InputLabel>Query Language</InputLabel>
                <Select
                  value={config.queryLanguage}
                  onChange={(e) => handleConfigChange('queryLanguage', e.target.value)}
                >
                  {SUPPORTED_QUERY_LANGUAGES.map((lang) => (
                    <MenuItem key={lang.value} value={lang.value}>
                      {lang.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </div>

        <Divider />

        {/* Time Range Configuration */}
        <div className={classes.section}>
          <Typography variant="h6" className={classes.sectionTitle}>
            Time Range (Unix Timestamp)
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField
                className={classes.textField}
                label="Start Time (Unix timestamp)"
                value={config.startTime}
                onChange={(e) => handleConfigChange('startTime', e.target.value)}
                placeholder={getOneHourAgoTimestamp()}
                fullWidth
                InputProps={{
                  className: classes.timeInput,
                }}
                helperText="Unix timestamp in seconds (e.g., 1672531200)"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                className={classes.textField}
                label="End Time (Unix timestamp)"
                value={config.endTime}
                onChange={(e) => handleConfigChange('endTime', e.target.value)}
                placeholder={getCurrentTimestamp()}
                fullWidth
                InputProps={{
                  className: classes.timeInput,
                }}
                helperText="Unix timestamp in seconds (e.g., 1704067199)"
              />
            </Grid>
          </Grid>
          <Box mt={1}>
            <Typography variant="body2" color="textSecondary">
              Current time: {getCurrentTimestamp()} | One hour ago: {getOneHourAgoTimestamp()}
            </Typography>
          </Box>
        </div>

        <Divider />

        {/* Query Input */}
        <div className={classes.section}>
          <Typography variant="h6" className={classes.sectionTitle}>
            {config.queryLanguage.toUpperCase()} Query
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                multiline
                rows={8}
                variant="outlined"
                value={config.query}
                onChange={handleQueryChange}
                placeholder={`Enter your ${config.queryLanguage.toUpperCase()} query here...`}
                fullWidth
                InputProps={{
                  className: classes.queryTextArea,
                }}
              />
            </Grid>
            <Grid item xs={12}>
                             <Box display="flex" style={{ gap: '8px' }}>
                <button
                  onClick={generateSampleQuery}
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#f0f0f0',
                    border: '1px solid #ccc',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '12px'
                  }}
                >
                  Generate Sample Query
                </button>
                <Typography variant="body2" color="textSecondary" style={{ alignSelf: 'center' }}>
                  Click to generate a sample query for the selected time range
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </div>

        <Divider />

        {/* Query Options */}
        <div className={classes.section}>
          <Typography variant="h6" className={classes.sectionTitle}>
            Query Options
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={config.useMultiStageEngine}
                    onChange={(e) => handleConfigChange('useMultiStageEngine', e.target.checked)}
                    color="primary"
                  />
                }
                label="Use Multi-Stage Engine"
              />
            </Grid>
            <Grid item xs={4}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={config.tracing}
                    onChange={(e) => handleConfigChange('tracing', e.target.checked)}
                    color="primary"
                  />
                }
                label="Tracing"
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                className={classes.textField}
                label="Timeout (ms)"
                type="number"
                value={config.timeout}
                onChange={(e) => handleConfigChange('timeout', parseInt(e.target.value) || 60000)}
                fullWidth
              />
            </Grid>
          </Grid>
        </div>

        <Divider />

        {/* Raw Output Display */}
        {rawOutput && (
          <>
            <div className={classes.section}>
              <Typography variant="h6" className={classes.sectionTitle}>
                Raw Output
              </Typography>
              <div className={classes.rawOutput}>
                {rawOutput}
              </div>
            </div>
            <Divider />
          </>
        )}

        {/* Query Preview */}
        <div className={classes.section}>
          <Typography variant="h6" className={classes.sectionTitle}>
            Query Preview
          </Typography>
          <div className={classes.queryPreview}>
            <Typography variant="subtitle2" className={classes.queryPreviewTitle}>
              {config.queryLanguage.toUpperCase()} Query:
            </Typography>
            <div className={classes.queryPreviewContent}>
              {config.query || `Enter your ${config.queryLanguage.toUpperCase()} query above...`}
            </div>
            {config.startTime && config.endTime && (
              <>
                <Typography variant="subtitle2" className={classes.queryPreviewTitle} style={{ marginTop: '16px' }}>
                  Time Range:
                </Typography>
                <div className={classes.queryPreviewContent}>
                  Start: {config.startTime} ({new Date(parseInt(config.startTime) * 1000).toISOString()})
                  End: {config.endTime} ({new Date(parseInt(config.endTime) * 1000).toISOString()})
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </CustomDialog>
  );
};

export default TimeseriesQueryModal;
