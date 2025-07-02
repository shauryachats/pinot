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

import React, { useState, useEffect, useCallback } from 'react';
import {
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Typography,
  Box,
  makeStyles,
  Button,
  Input
} from '@material-ui/core';
import Alert from '@material-ui/lab/Alert';
import { UnControlled as CodeMirror } from 'react-codemirror2';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';
import 'codemirror/mode/javascript/javascript';
import { getTimeSeriesQueryResult } from '../../requests';
import { useHistory, useLocation } from 'react-router';
import TableToolbar from '../TableToolbar';
import { Resizable } from 're-resizable';

const useStyles = makeStyles((theme) => ({
  rightPanel: {},
  codeMirror: {
    height: '100%',
    '& .CodeMirror': {
      height: '100%',
      border: '1px solid #BDCCD9',
      fontSize: '13px',
    },
  },
  queryOutput: {
    '& .CodeMirror': { height: 430, border: '1px solid #BDCCD9' },
  },
  btn: {
    margin: '10px 10px 0 0',
    height: 30,
  },
  checkBox: {
    margin: '20px 0',
  },
  actionBtns: {
    margin: '20px 0',
    height: 50,
  },
  runNowBtn: {
    marginLeft: 'auto',
    paddingLeft: '10px',
  },
  formatSQLBtn: {
    marginLeft: 'auto',
    paddingLeft: '30px',
  },
  formatMSE: {
    marginLeft: '-30px',
    paddingLeft: 'auto',
  },
  sqlDiv: {
    height: '100%',
    border: '1px #BDCCD9 solid',
    borderRadius: 4,
    marginBottom: '20px',
    paddingBottom: '48px',
  },
  sqlError: {
    whiteSpace: 'pre',
    overflow: "auto"
  },
  timeoutControl: {
    // Removed bottom: 10 to align with other inputs
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
  formControl: {
    margin: theme.spacing(1),
    minWidth: 200,
  },
  gridItem: {
    padding: theme.spacing(1),
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
  timeout: number;
}

const TimeseriesQueryPage = () => {
  const classes = useStyles();
  const history = useHistory();
  const location = useLocation();

  const getCurrentTimestamp = () => {
    return Math.floor(Date.now() / 1000).toString();
  };

  const getOneHourAgoTimestamp = () => {
    return Math.floor((Date.now() - 60 * 60 * 1000) / 1000).toString();
  };

  const [config, setConfig] = useState<TimeseriesQueryConfig>({
    queryLanguage: 'm3ql',
    query: '',
    startTime: getOneHourAgoTimestamp(),
    endTime: getCurrentTimestamp(),
    timeout: 60000,
  });

  const [rawOutput, setRawOutput] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [shouldAutoExecute, setShouldAutoExecute] = useState<boolean>(false);

  // Update config when URL parameters change
  useEffect(() => {
    const urlParams = new URLSearchParams(location.search);
    const newConfig = {
      queryLanguage: urlParams.get('language') || 'm3ql',
      query: urlParams.get('query') || '',
      startTime: urlParams.get('start') || getOneHourAgoTimestamp(),
      endTime: urlParams.get('end') || getCurrentTimestamp(),
      timeout: parseInt(urlParams.get('timeout') || '60000'),
    };

    setConfig(newConfig);

    // Auto-execute if we have a query and either start or end time
    if (newConfig.query && (newConfig.startTime || newConfig.endTime)) {
      setShouldAutoExecute(true);
    }
  }, [location.search]);

  // Auto-execute query when shouldAutoExecute is true
  useEffect(() => {
    if (shouldAutoExecute && config.query && !isLoading) {
      setShouldAutoExecute(false);
      handleExecuteQuery();
    }
  }, [shouldAutoExecute, config.query, isLoading]);

  const updateURL = useCallback((newConfig: TimeseriesQueryConfig) => {
    const params = new URLSearchParams();
    // Always include language, even if it's the default
    params.set('language', newConfig.queryLanguage);
    if (newConfig.query) {
      params.set('query', newConfig.query);
    }
    if (newConfig.startTime) {
      params.set('start', newConfig.startTime);
    }
    if (newConfig.endTime) {
      params.set('end', newConfig.endTime);
    }
    if (newConfig.timeout && newConfig.timeout !== 60000) {
      params.set('timeout', newConfig.timeout.toString());
    }

    const newURL = params.toString() ? `?${params.toString()}` : '';
    history.push({
      pathname: location.pathname,
      search: newURL
    });
  }, [history, location.pathname]);

  const handleConfigChange = (field: keyof TimeseriesQueryConfig, value: any) => {
    const newConfig = { ...config, [field]: value };
    setConfig(newConfig);
  };

  const handleQueryChange = (editor: any, data: any, value: string) => {
    const newConfig = { ...config, query: value };
    setConfig(newConfig);
  };

  const handleExecuteQuery = useCallback(async () => {
    if (!config.query.trim()) {
      setError('Please enter a query');
      return;
    }

    // Update URL with current config before executing
    updateURL(config);

    setIsLoading(true);
    setError('');
    setRawOutput('');

    try {
      // Prepare the request payload
      const requestPayload = {
        language: config.queryLanguage,
        query: config.query,
        start: config.startTime,
        end: config.endTime,
        step: '1m', // Default step, could be made configurable
        trace: false,
        queryOptions: `timeoutMs=${config.timeout}`
      };

      // Call the timeseries API
      const response = await getTimeSeriesQueryResult(requestPayload);

      // Display the raw response
      setRawOutput(JSON.stringify(response.data, null, 2));
    } catch (error) {
      console.error('Error executing timeseries query:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error occurred';
      setError(`Query execution failed: ${errorMessage}`);
    } finally {
      setIsLoading(false);
    }
  }, [config, updateURL]);

  const sqlEditorTooltip = "This editor supports timeseries queries. Enter your M3QL query here.";

  return (
    <Grid container>
      <Grid item xs={12} className={classes.rightPanel}>
        <Resizable
          defaultSize={{
            width: '100%',
            height: 148,
          }}
          minHeight={148}
          maxWidth={'100%'}
          maxHeight={'50vh'}
          enable={{bottom: true}}>
          <div className={classes.sqlDiv}>
            <TableToolbar name="Timeseries Query Editor" showSearchBox={false} showTooltip={true} tooltipText={sqlEditorTooltip} />
            <CodeMirror
              value={config.query}
              onChange={handleQueryChange}
              options={{
                lineNumbers: true,
                mode: 'text/plain',
                theme: 'default',
                lineWrapping: true,
                indentWithTabs: true,
                smartIndent: true,
              }}
              className={classes.codeMirror}
              autoCursor={false}
            />
          </div>
        </Resizable>

        <Grid container className={classes.checkBox} spacing={2}>
          <Grid item xs={3} className={classes.gridItem}>
            <FormControl fullWidth={true} className={classes.formControl}>
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

          <Grid item xs={3} className={classes.gridItem}>
            <FormControl fullWidth={true} className={classes.formControl}>
              <InputLabel>Start Time (Unix timestamp)</InputLabel>
              <Input
                type="text"
                value={config.startTime}
                onChange={(e) => handleConfigChange('startTime', e.target.value)}
                placeholder={getOneHourAgoTimestamp()}
              />
            </FormControl>
          </Grid>

          <Grid item xs={3} className={classes.gridItem}>
            <FormControl fullWidth={true} className={classes.formControl}>
              <InputLabel>End Time (Unix timestamp)</InputLabel>
              <Input
                type="text"
                value={config.endTime}
                onChange={(e) => handleConfigChange('endTime', e.target.value)}
                placeholder={getCurrentTimestamp()}
              />
            </FormControl>
          </Grid>

          <Grid item xs={3} className={classes.gridItem}>
            <FormControl fullWidth={true} className={classes.formControl}>
              <InputLabel>Timeout (Milliseconds)</InputLabel>
              <Input
                type="number"
                value={config.timeout}
                onChange={(e) => handleConfigChange('timeout', parseInt(e.target.value) || 60000)}
              />
            </FormControl>
          </Grid>

          <Grid item xs={12} style={{ marginTop: '8px' }}>
            <Button
              variant="contained"
              color="primary"
              onClick={handleExecuteQuery}
              disabled={isLoading || !config.query.trim()}
              endIcon={<span style={{fontSize: '0.8em', lineHeight: 1}}>{navigator.platform.includes('Mac') ? '⌘↵' : 'Ctrl+↵'}</span>}
            >
              {isLoading ? 'Running Query...' : 'Run Query'}
            </Button>
          </Grid>
        </Grid>

        {/* Error Display */}
        {error && (
          <Alert severity="error" className={classes.sqlError}>
            {error}
          </Alert>
        )}

        {/* Raw Output Display */}
        {rawOutput && (
          <div style={{ marginTop: '20px' }}>
            <Typography variant="h6" style={{ marginBottom: '16px', color: '#3B454E' }}>
              Query Results
            </Typography>
            <div className={classes.rawOutput}>
              {rawOutput}
            </div>
          </div>
        )}
      </Grid>
    </Grid>
  );
};

export default TimeseriesQueryPage;
