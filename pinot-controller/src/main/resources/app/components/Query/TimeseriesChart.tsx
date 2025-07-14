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

import React, { useMemo } from 'react';
import Plot from 'react-plotly.js';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
  chartContainer: {
    width: '100%',
    height: '400px',
    border: '1px solid #e2e8f0',
    borderRadius: 6,
    backgroundColor: '#ffffff',
    marginBottom: theme.spacing(2),
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
  },
  noDataMessage: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '400px',
    color: '#64748b',
    fontSize: '1.1rem',
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  statsTable: {
    marginTop: theme.spacing(2),
    backgroundColor: '#f8fafc',
    borderRadius: 4,
    border: '1px solid #e2e8f0',
    overflow: 'hidden',
  },
  statsTableHeader: {
    backgroundColor: '#f1f5f9',
    padding: '8px 12px',
    color: '#334155',
    fontSize: '11px',
    fontWeight: 600,
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    borderBottom: '1px solid #e2e8f0',
  },
  statsTableRow: {
    display: 'flex',
    borderBottom: '1px solid #e2e8f0',
    '&:last-child': {
      borderBottom: 'none',
    },
  },
  statsTableCell: {
    padding: '6px 12px',
    fontSize: '10px',
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    color: '#334155',
    flex: 1,
    textAlign: 'left' as const,
    '&:first-child': {
      fontWeight: 600,
      color: '#334155',
      display: 'flex',
      alignItems: 'center',
      gap: '6px',
      cursor: 'pointer',
      '&:hover': {
        backgroundColor: '#f1f5f9',
        borderRadius: '4px',
      },
    },
  },
  colorIndicator: {
    width: '12px',
    height: '12px',
    borderRadius: '2px',
    flexShrink: 0,
  },
}));

interface TimeseriesData {
  timestamp: number;
  value: number;
  metric?: string;
}

interface TimeseriesChartProps {
  data: any;
  isLoading?: boolean;
}

const TimeseriesChart: React.FC<TimeseriesChartProps> = ({ data, isLoading = false }) => {
  const classes = useStyles();
  const [statistics, setStatistics] = React.useState<any[]>([]);
  const [selectedMetrics, setSelectedMetrics] = React.useState<Set<string>>(new Set());

  const { chartData, layout } = useMemo(() => {
    if (!data || isLoading) {
      return { chartData: [], layout: {} };
    }

    // Debug: Log the data structure
    console.log('TimeseriesChart received data:', data);

    try {
      // Handle different response formats
      let timeseriesData: TimeseriesData[] = [];

      // Check if data has a results property (common in timeseries APIs)
      if (data.results && Array.isArray(data.results)) {
        timeseriesData = data.results.flatMap((result: any) => {
          if (result.data && Array.isArray(result.data)) {
            return result.data.map((point: any) => ({
              timestamp: point[0] || point.timestamp || point.time,
              value: point[1] || point.value,
              metric: result.metric || result.__name__ || 'value'
            }));
          }
          return [];
        });
      }
      // Check if data is directly an array of points
      else if (Array.isArray(data)) {
        timeseriesData = data.map((point: any) => ({
          timestamp: point[0] || point.timestamp || point.time,
          value: point[1] || point.value,
          metric: point.metric || 'value'
        }));
      }
      // Check if data has a data property
      else if (data.data && Array.isArray(data.data)) {
        timeseriesData = data.data.map((point: any) => ({
          timestamp: point[0] || point.timestamp || point.time,
          value: point[1] || point.value,
          metric: point.metric || 'value'
        }));
      }
      // Handle Prometheus API format
      else if (data.data && data.data.result && Array.isArray(data.data.result)) {
        timeseriesData = data.data.result.flatMap((result: any) => {
          if (result.values && Array.isArray(result.values)) {
            return result.values.map((point: any) => ({
              timestamp: point[0],
              value: parseFloat(point[1]),
              metric: result.metric?.__name__ || result.metric?.job || 'value'
            }));
          }
          return [];
        });
      }
      // Handle Prometheus instant query format
      else if (data.data && data.data.result && Array.isArray(data.data.result)) {
        timeseriesData = data.data.result.map((result: any) => {
          if (result.value && Array.isArray(result.value)) {
            return {
              timestamp: result.value[0],
              value: parseFloat(result.value[1]),
              metric: result.metric?.__name__ || result.metric?.job || 'value'
            };
          }
          return null;
        }).filter(Boolean);
      }

      // If no data was parsed, try to extract any array-like structure
      if (timeseriesData.length === 0) {
        console.log('No data parsed, attempting fallback parsing...');
        // Try to find any array in the data structure
        const findArrays = (obj: any): any[] => {
          if (Array.isArray(obj)) return [obj];
          if (typeof obj === 'object' && obj !== null) {
            for (const key in obj) {
              const result = findArrays(obj[key]);
              if (result.length > 0) return result;
            }
          }
          return [];
        };

        const arrays = findArrays(data);
        if (arrays.length > 0) {
          console.log('Found arrays in data:', arrays);
          // Try to parse the first array as timeseries data
          const firstArray = arrays[0];
          if (firstArray.length > 0 && Array.isArray(firstArray[0])) {
            timeseriesData = firstArray.map((point: any) => ({
              timestamp: point[0] || 0,
              value: parseFloat(point[1]) || 0,
              metric: 'value'
            }));
          }
        }
      }

      if (timeseriesData.length === 0) {
        return { chartData: [], layout: {} };
      }

      // Group data by metric
      const groupedData = timeseriesData.reduce((acc: any, point) => {
        const metric = point.metric || 'value';
        if (!acc[metric]) {
          acc[metric] = [];
        }
        acc[metric].push({
          x: new Date(point.timestamp * 1000), // Convert Unix timestamp to Date
          y: point.value
        });
        return acc;
      }, {});

      // Calculate time range for padding
      const allTimestamps = timeseriesData.map(point => point.timestamp);
      const minTimestamp = Math.min(...allTimestamps);
      const maxTimestamp = Math.max(...allTimestamps);
      const timeRange = maxTimestamp - minTimestamp;
      const paddingAmount = Math.max(timeRange * 0.05, 60); // 5% of range or minimum 60 seconds

      const paddedMinTime = new Date((minTimestamp - paddingAmount) * 1000);
      const paddedMaxTime = new Date((maxTimestamp + paddingAmount) * 1000);

      // Grafana-like color palette
      const grafanaColors = [
        '#7EB26D', '#EAB839', '#6ED0E0', '#EF843C', '#E24D42',
        '#1F78C1', '#BA43A9', '#705DA0', '#508642', '#CCA300',
        '#3F6833', '#2F575E', '#99440A', '#58140C', '#052B51',
        '#511749', '#3F2B5B', '#0F1419', '#000000', '#000000'
      ];

      // Calculate statistics for each metric with colors
      const calculatedStats = Object.entries(groupedData).map(([metric, points]: [string, any], index: number) => {
        const values = points.map((p: any) => p.y).filter((v: number) => !isNaN(v) && isFinite(v));
        const min = Math.min(...values);
        const max = Math.max(...values);
        const total = values.reduce((sum: number, val: number) => sum + val, 0);
        const avg = total / values.length;

        return {
          metric,
          color: grafanaColors[index % grafanaColors.length],
          min: min.toFixed(2),
          max: max.toFixed(2),
          avg: avg.toFixed(2),
          total: total.toFixed(2),
          count: values.length
        };
      });

      // Update statistics state
      setStatistics(calculatedStats);

      // Convert to Plotly format with Grafana colors
      const chartData = Object.entries(groupedData).map(([metric, points]: [string, any], index: number) => ({
        x: points.map((p: any) => p.x),
        y: points.map((p: any) => p.y),
        type: 'scatter' as const,
        mode: 'lines+markers' as const,
        name: metric,
        line: {
          width: 1.5,
          color: grafanaColors[index % grafanaColors.length]
        },
        marker: {
          size: 8,
          color: grafanaColors[index % grafanaColors.length],
          line: {
            width: 0
          }
        },
        hoverinfo: '%{x|%Y-%m-%d %H:%M:%S}' as const,
        hovertemplate: '<b>%{fullData.name}</b>:    %{y}<extra></extra>',
        visible: selectedMetrics.size === 0 || selectedMetrics.has(metric) ? true : 'legendonly'
      }));

      // Create layout with padded time range
      const layout = {
        title: {
          text: '',
          font: {
            size: 14,
            color: '#334155',
            family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
          }
        },
        xaxis: {
          title: {
            text: '',
            font: {
              size: 11,
              color: '#64748b',
              family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
            }
          },
          type: 'date' as const,
          showgrid: true,
          gridcolor: '#f1f5f9',
          gridwidth: 1,
          color: '#64748b',
          tickfont: {
            color: '#64748b',
            size: 10,
            family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
          },
          zeroline: false,
          showline: false,
          tickformat: '%Y-%m-%d %H:%M:%S',
          tickmode: 'auto' as const,
          nticks: 16,
          range: [paddedMinTime, paddedMaxTime], // Use padded time range
          dtick: 'auto' as const
        },
        yaxis: {
          title: {
            text: '',
            font: {
              size: 11,
              color: '#64748b',
              family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
            }
          },
          showgrid: true,
          gridcolor: '#f1f5f9',
          color: '#64748b',
          tickfont: {
            color: '#64748b',
            size: 10,
            family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
          },
          zeroline: false,
          showline: false,
          range: undefined, // Let Plotly auto-range with padding
          fixedrange: true // Disable y-axis zooming
        },
        margin: {
          l: 50,
          r: 20,
          t: 20,
          b: 60
        },
        plot_bgcolor: '#ffffff',
        paper_bgcolor: '#ffffff',
        font: {
          family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
          size: 11,
          color: '#334155'
        },
        hovermode: 'x unified' as const,
        showlegend: false,
        hoverlabel: {
          bgcolor: '#f8fafc',
          bordercolor: '#e2e8f0',
          font: {
            family: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
            size: 11,
            color: '#334155'
          }
        }
      };

      return { chartData, layout };
    } catch (error) {
      console.error('Error parsing timeseries data:', error);
      return { chartData: [], layout: {} };
    }
  }, [data, isLoading, selectedMetrics]);

  const config = {
    displayModeBar: true,
    displaylogo: false,
    modeBarButtonsToRemove: ['pan2d', 'lasso2d', 'select2d', 'autoScale2d', 'hoverClosestCartesian', 'hoverCompareCartesian'] as any,
    responsive: true,
    modeBarButtons: [
      ['zoom2d', 'resetScale2d']
    ],
    scrollZoom: false // Disable scroll zoom to prevent y-axis zooming
  };

  const handleMetricClick = (metric: string) => {
    setSelectedMetrics(prev => {
      const newSet = new Set(prev);
      if (newSet.has(metric)) {
        // If metric is already selected, remove it (show all)
        newSet.delete(metric);
      } else {
        // If metric is not selected, clear all and add only this one
        newSet.clear();
        newSet.add(metric);
      }
      return newSet;
    });
  };

  if (isLoading) {
    return (
      <div className={classes.chartContainer}>
        <div className={classes.noDataMessage}>
          Loading chart data...
        </div>
      </div>
    );
  }

  if (!data || chartData.length === 0) {
    return (
      <div className={classes.chartContainer}>
        <div className={classes.noDataMessage}>
          No timeseries data available. Run a query to see visualization.
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Chart */}
      <div className={classes.chartContainer}>
        <Plot
          data={chartData}
          layout={layout}
          config={config}
          style={{ width: '100%', height: '100%' }}
          useResizeHandler={true}
        />
      </div>

      {/* Statistics Table */}
      {statistics.length > 0 && (
        <div className={classes.statsTable}>
          <div className={classes.statsTableHeader}>
            Statistics Summary
          </div>
          <div className={classes.statsTableRow}>
            <div className={classes.statsTableCell}>Metric</div>
            <div className={classes.statsTableCell}>Min</div>
            <div className={classes.statsTableCell}>Max</div>
            <div className={classes.statsTableCell}>Avg</div>
            <div className={classes.statsTableCell}>Total</div>
            <div className={classes.statsTableCell}>Count</div>
          </div>
                    {statistics.map((stat, index) => {
            const isSelected = selectedMetrics.has(stat.metric);
            const hasSelection = selectedMetrics.size > 0;
            const isGrayedOut = hasSelection && !isSelected;

            return (
              <div key={stat.metric} className={classes.statsTableRow}>
                <div
                  className={classes.statsTableCell}
                  onClick={() => handleMetricClick(stat.metric)}
                  style={{
                    backgroundColor: isSelected ? '#f1f5f9' : 'transparent',
                    borderRadius: isSelected ? '4px' : '0px',
                    opacity: isGrayedOut ? 0.4 : 1,
                    filter: isGrayedOut ? 'grayscale(0.8)' : 'none',
                  }}
                >
                  <div
                    className={classes.colorIndicator}
                    style={{
                      backgroundColor: stat.color,
                      opacity: isGrayedOut ? 0.6 : 1,
                    }}
                  />
                  <span style={{
                    color: isGrayedOut ? '#94a3b8' : '#334155',
                    fontWeight: isSelected ? 600 : 600,
                  }}>
                    {stat.metric}
                  </span>
                </div>
                <div className={classes.statsTableCell} style={{ opacity: isGrayedOut ? 0.4 : 1 }}>
                  {stat.min}
                </div>
                <div className={classes.statsTableCell} style={{ opacity: isGrayedOut ? 0.4 : 1 }}>
                  {stat.max}
                </div>
                <div className={classes.statsTableCell} style={{ opacity: isGrayedOut ? 0.4 : 1 }}>
                  {stat.avg}
                </div>
                <div className={classes.statsTableCell} style={{ opacity: isGrayedOut ? 0.4 : 1 }}>
                  {stat.total}
                </div>
                <div className={classes.statsTableCell} style={{ opacity: isGrayedOut ? 0.4 : 1 }}>
                  {stat.count}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default TimeseriesChart;
