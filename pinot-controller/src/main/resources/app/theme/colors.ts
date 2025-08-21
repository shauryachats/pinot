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

/**
 * Consolidated color theme for Pinot UI
 * All colors used throughout the application are defined here
 * Organized by semantic meaning for easy dark mode implementation
 */

export const colors = {
  // Base colors
  common: {
    black: '#000',
    white: '#fff',
    transparent: 'transparent',
  },

  // Primary brand colors
  primary: {
    50: '#EAF7F9',
    100: '#CCEAEF',
    200: '#CCEAEF',
    300: '#AADDE4',
    400: '#6EC4D1',
    500: '#4285F4', // Main primary color
    600: '#4DB3C3',
    700: '#43ABBC',
    800: '#3AA3B5',
    900: '#115293',
    A100: '#ea80fc',
    A200: '#e040fb',
    A400: '#d500f9',
    A700: '#aa00ff',
  },

  // Secondary brand colors
  secondary: {
    50: '#D9FFF9',
    100: '#9BFDEE',
    200: '#18FDE2',
    300: '#00F7D3',
    400: '#00EDC5',
    500: '#F5F7F9',
    600: '#00B585',
    700: '#00A573',
    800: '#009766',
    900: '#008757',
    A100: '#ea80fc',
    A200: '#e040fb',
    A400: '#d500f9',
    A700: '#aa00ff',
  },

  // Neutral colors
  neutral: {
    50: '#fafafa',
    100: '#f5f5f5',
    200: '#eeeeee',
    300: '#e0e0e0',
    400: '#bdbdbd',
    500: '#9e9e9e',
    600: '#757575',
    700: '#616161',
    800: '#424242',
    900: '#212121',
  },

  // Text colors
  text: {
    primary: 'rgba(0, 0, 0, 0.87)',
    secondary: '#666',
    disabled: 'rgba(0, 0, 0, 0.38)',
    hint: 'white',
    dark: '#3B454E',
    light: '#fff',
  },

  // Background colors
  background: {
    default: '#ffffff',
    paper: '#fff',
    dark: '#333333',
    light: '#f5f5f5',
    sidebar: '#333333',
    hover: '#dae2f2',
    selected: '#c0d5f8',
  },

  // Border colors
  border: {
    primary: '#4285f4',
    secondary: '#BDCCD9',
    light: '#ccc',
    lighter: '#eaeaea',
    dark: '#333333',
    divider: '#ddd',
  },

  // Status colors
  status: {
    // Success states
    success: {
      main: '#4CAF50',
      light: '#e8f5e8',
      dark: '#2e7d32',
      border: '#4caf50',
      text: '#2e7d32',
    },

    // Error states
    error: {
      main: '#f44336',
      light: '#ffebee',
      dark: '#c62828',
      border: '#f44336',
      text: '#c62828',
      alt: '#a11',
    },

    // Warning states
    warning: {
      main: '#ff9800',
      light: '#fff3e0',
      dark: '#ef6c00',
      border: '#ff9800',
      text: '#ef6c00',
    },

    // Info states
    info: {
      main: '#2196f3',
      light: '#e3f2fd',
      dark: '#1565c0',
      border: '#2196f3',
      text: '#1565c0',
    },
  },

  // Task status colors
  taskStatus: {
    completed: {
      background: '#e8f5e8',
      border: '#4caf50',
      text: '#2e7d32',
    },
    running: {
      background: '#e3f2fd',
      border: '#2196f3',
      text: '#1565c0',
    },
    waiting: {
      background: '#fff3e0',
      border: '#ff9800',
      text: '#ef6c00',
    },
    error: {
      background: '#ffebee',
      border: '#f44336',
      text: '#c62828',
    },
    unknown: {
      background: '#f3e5f5',
      border: '#9c27b0',
      text: '#7b1fa2',
    },
    dropped: {
      background: '#fce4ec',
      border: '#e91e63',
      text: '#ad1457',
    },
    timedout: {
      background: '#fff8e1',
      border: '#ffc107',
      text: '#f57c00',
    },
    aborted: {
      background: '#efebe9',
      border: '#795548',
      text: '#5d4037',
    },
  },

  // Segment status colors
  segmentStatus: {
    good: {
      background: 'rgba(76, 175, 80, 0.1)',
      border: '#4CAF50',
      text: '#4CAF50',
    },
    bad: {
      background: 'rgba(244, 67, 54, 0.1)',
      border: '#f44336',
      text: '#f44336',
    },
    consuming: {
      background: 'rgba(255, 152, 0, 0.1)',
      border: '#ff9800',
      text: '#ff9800',
    },
    error: {
      background: 'rgba(170, 17, 17, 0.1)',
      border: '#a11',
      text: '#a11',
    },
  },

  // Chart colors
  chart: {
    colors: [
      "#5470C6", "#91CC75", "#EE6666", "#FAC858", "#73C0DE",
      "#3BA272", "#FC8452", "#9A60B4", "#EA7CCC", "#6E7074",
      "#546570", "#C4CCD3", "#F05B72", "#FF715E", "#FFAF51",
      "#FFE153", "#47B39C", "#5BACE1", "#32C5E9", "#96BFFF"
    ],
    background: '#6a7985',
  },

  // CodeMirror specific colors
  codeMirror: {
    tableIcon: '#2F7E89',
    columnIcon: '#05a',
    functionIcon: '#74457a',
    hintBackground: '#dae2f2',
    border: '#eaeaea',
  },

  // Overlay and shadow colors
  overlay: {
    shadow: 'rgba(0, 0, 0, 0.2)',
    shadowLight: 'rgba(0, 0, 0, 0.1)',
    primaryLight: 'rgba(66, 133, 244, 0.05)',
    primaryMedium: 'rgba(66, 133, 244, 0.08)',
    primaryHeavy: 'rgba(66, 133, 244, 0.12)',
    primaryHeavier: 'rgba(66, 133, 244, 0.16)',
    primaryAlpha: 'rgb(66 133 244 / 0.3)',
  },

  // Semantic colors for specific use cases
  semantic: {
    link: '#4285f4',
    linkHover: '#1565c0',
    accent: '#4285f4',
    highlight: '#c0d5f8',
    muted: '#666',
    disabled: 'rgba(0, 0, 0, 0.38)',
  },
};

export default colors;
