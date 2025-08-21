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

import { getChartColors, getChartColor } from '../theme/themeUtils';

// Standard chart colors matching the ECharts palette
// Now imported from the consolidated theme
export const CHART_COLORS = getChartColors();

/**
 * Default number of series that can be rendered in the chart
 */
export const DEFAULT_SERIES_LIMIT = 40;

/**
 * Chart padding percentage for time axis and series
 */
export const CHART_PADDING_PERCENTAGE = 0.05; // 5%

/**
 * Get color for a series by index
 * @param index - The series index
 * @returns The color for the series
 */
export const getSeriesColor = (index: number): string => {
  return getChartColor(index);
};
