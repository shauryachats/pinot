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

import { colors } from './colors';
import { darkColors } from './darkColors';

/**
 * Theme utilities for accessing colors and implementing theme switching
 */

export type ThemeMode = 'light' | 'dark';

// Current theme mode (default to light)
let currentThemeMode: ThemeMode = 'light';

/**
 * Set the current theme mode
 * @param mode - The theme mode to set
 */
export const setThemeMode = (mode: ThemeMode): void => {
  currentThemeMode = mode;
  // Store in localStorage for persistence
  localStorage.setItem('pinot_ui:themeMode', mode);

  // Update the document body class for CSS-based styling
  document.body.classList.remove('theme-light', 'theme-dark');
  document.body.classList.add(`theme-${mode}`);
};

/**
 * Get the current theme mode
 * @returns The current theme mode
 */
export const getThemeMode = (): ThemeMode => {
  // Try to get from localStorage first
  const stored = localStorage.getItem('pinot_ui:themeMode') as ThemeMode;
  if (stored && (stored === 'light' || stored === 'dark')) {
    currentThemeMode = stored;
  }
  return currentThemeMode;
};

/**
 * Initialize theme mode from localStorage
 */
export const initializeThemeMode = (): void => {
  const mode = getThemeMode();
  // Update the document body class for CSS-based styling
  document.body.classList.remove('theme-light', 'theme-dark');
  document.body.classList.add(`theme-${mode}`);
};

/**
 * Get colors for the current theme mode
 * @returns The colors object for the current theme
 */
export const getThemeColors = () => {
  return currentThemeMode === 'dark' ? darkColors : colors;
};

/**
 * Get a specific color by path
 * @param path - The dot-separated path to the color (e.g., 'primary.500', 'status.success.main')
 * @returns The color value
 */
export const getColor = (path: string): string => {
  const colorObj = getThemeColors();
  const keys = path.split('.');
  let current: any = colorObj;

  for (const key of keys) {
    if (current && typeof current === 'object' && key in current) {
      current = current[key];
    } else {
      console.warn(`Color path "${path}" not found in theme`);
      return '#000'; // fallback
    }
  }

  return current;
};

/**
 * Get chart colors
 * @returns Array of chart colors
 */
export const getChartColors = (): string[] => {
  return getThemeColors().chart.colors;
};

/**
 * Get a chart color by index
 * @param index - The index of the color
 * @returns The color value
 */
export const getChartColor = (index: number): string => {
  const colors = getChartColors();
  return colors[index % colors.length];
};

/**
 * Get task status colors
 * @param status - The task status
 * @returns The color object for the status
 */
export const getTaskStatusColors = (status: string) => {
  const statusColors = getThemeColors().taskStatus;
  const normalizedStatus = status.toLowerCase();

  switch (normalizedStatus) {
    case 'completed':
      return statusColors.completed;
    case 'running':
      return statusColors.running;
    case 'waiting':
      return statusColors.waiting;
    case 'error':
    case 'task_error':
    case 'failed':
      return statusColors.error;
    case 'unknown':
      return statusColors.unknown;
    case 'dropped':
      return statusColors.dropped;
    case 'timed_out':
    case 'timedout':
      return statusColors.timedout;
    case 'aborted':
      return statusColors.aborted;
    default:
      return statusColors.unknown;
  }
};

/**
 * Get segment status colors
 * @param status - The segment status
 * @returns The color object for the status
 */
export const getSegmentStatusColors = (status: string) => {
  const statusColors = getThemeColors().segmentStatus;
  const normalizedStatus = status.toLowerCase();

  switch (normalizedStatus) {
    case 'good':
      return statusColors.good;
    case 'bad':
      return statusColors.bad;
    case 'consuming':
      return statusColors.consuming;
    case 'error':
      return statusColors.error;
    default:
      return statusColors.good;
  }
};

/**
 * Create a theme-aware color function for use in styled components
 * @param colorPath - The path to the color in the theme
 * @returns A function that returns the color value
 */
export const createThemeColor = (colorPath: string) => {
  return () => getColor(colorPath);
};

/**
 * Get overlay colors with opacity
 * @param baseColor - The base color path
 * @param opacity - The opacity value (0-1)
 * @returns The color with opacity
 */
export const getColorWithOpacity = (baseColor: string, opacity: number): string => {
  const color = getColor(baseColor);

  // If it's already an rgba color, extract the RGB values
  if (color.startsWith('rgba(')) {
    const match = color.match(/rgba?\(([^)]+)\)/);
    if (match) {
      const values = match[1].split(',').map(v => v.trim());
      return `rgba(${values[0]}, ${values[1]}, ${values[2]}, ${opacity})`;
    }
  }

  // If it's a hex color, convert to rgba
  if (color.startsWith('#')) {
    const hex = color.replace('#', '');
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  }

  return color;
};

/**
 * Toggle between light and dark mode
 */
export const toggleThemeMode = (): void => {
  const newMode = currentThemeMode === 'light' ? 'dark' : 'light';
  setThemeMode(newMode);
};

export default {
  setThemeMode,
  getThemeMode,
  initializeThemeMode,
  getThemeColors,
  getColor,
  getChartColors,
  getChartColor,
  getTaskStatusColors,
  getSegmentStatusColors,
  createThemeColor,
  getColorWithOpacity,
  toggleThemeMode,
};
