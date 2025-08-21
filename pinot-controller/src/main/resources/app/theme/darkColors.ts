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
 * Dark mode color palette for Pinot UI
 * Mirrors the structure of light theme colors but with dark mode appropriate values
 */

export const darkColors = {
  // Base colors
  common: {
    black: '#000',
    white: '#fff',
    transparent: 'transparent',
  },

  // Primary brand colors (same as light mode for consistency)
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

  // Secondary brand colors (same as light mode for consistency)
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

  // Neutral colors (inverted for dark mode)
  neutral: {
    50: '#212121',
    100: '#424242',
    200: '#616161',
    300: '#757575',
    400: '#9e9e9e',
    500: '#bdbdbd',
    600: '#e0e0e0',
    700: '#eeeeee',
    800: '#f5f5f5',
    900: '#fafafa',
  },

  // Text colors (inverted for dark mode)
  text: {
    primary: 'rgba(255, 255, 255, 0.87)',
    secondary: '#b0b0b0',
    disabled: 'rgba(255, 255, 255, 0.38)',
    hint: 'rgba(255, 255, 255, 0.6)',
    dark: '#e0e0e0',
    light: '#000',
  },

  // Background colors (inverted for dark mode)
  background: {
    default: '#121212',
    paper: '#1e1e1e',
    dark: '#000000',
    light: '#2d2d2d',
    sidebar: '#1a1a1a',
    hover: '#2a2a2a',
    selected: '#3a3a3a',
  },

  // Border colors (adjusted for dark mode)
  border: {
    primary: '#4285f4',
    secondary: '#404040',
    light: '#555555',
    lighter: '#333333',
    dark: '#e0e0e0',
    divider: '#404040',
  },

  // Status colors (same as light mode for consistency)
  status: {
    // Success states
    success: {
      main: '#4CAF50',
      light: '#1b5e20',
      dark: '#81c784',
      border: '#4caf50',
      text: '#81c784',
    },

    // Error states
    error: {
      main: '#f44336',
      light: '#c62828',
      dark: '#e57373',
      border: '#f44336',
      text: '#e57373',
      alt: '#ff6b6b',
    },

    // Warning states
    warning: {
      main: '#ff9800',
      light: '#ef6c00',
      dark: '#ffb74d',
      border: '#ff9800',
      text: '#ffb74d',
    },

    // Info states
    info: {
      main: '#2196f3',
      light: '#1565c0',
      dark: '#64b5f6',
      border: '#2196f3',
      text: '#64b5f6',
    },
  },

  // Task status colors (adjusted for dark mode)
  taskStatus: {
    completed: {
      background: '#1b5e20',
      border: '#4caf50',
      text: '#81c784',
    },
    running: {
      background: '#1565c0',
      border: '#2196f3',
      text: '#64b5f6',
    },
    waiting: {
      background: '#ef6c00',
      border: '#ff9800',
      text: '#ffb74d',
    },
    error: {
      background: '#c62828',
      border: '#f44336',
      text: '#e57373',
    },
    unknown: {
      background: '#7b1fa2',
      border: '#9c27b0',
      text: '#ba68c8',
    },
    dropped: {
      background: '#ad1457',
      border: '#e91e63',
      text: '#f06292',
    },
    timedout: {
      background: '#f57c00',
      border: '#ffc107',
      text: '#ffd54f',
    },
    aborted: {
      background: '#5d4037',
      border: '#795548',
      text: '#a1887f',
    },
  },

  // Segment status colors (adjusted for dark mode)
  segmentStatus: {
    good: {
      background: 'rgba(76, 175, 80, 0.2)',
      border: '#4CAF50',
      text: '#81c784',
    },
    bad: {
      background: 'rgba(244, 67, 54, 0.2)',
      border: '#f44336',
      text: '#e57373',
    },
    consuming: {
      background: 'rgba(255, 152, 0, 0.2)',
      border: '#ff9800',
      text: '#ffb74d',
    },
    error: {
      background: 'rgba(255, 107, 107, 0.2)',
      border: '#ff6b6b',
      text: '#ff8a80',
    },
  },

  // Chart colors (same as light mode for consistency)
  chart: {
    colors: [
      "#5470C6", "#91CC75", "#EE6666", "#FAC858", "#73C0DE",
      "#3BA272", "#FC8452", "#9A60B4", "#EA7CCC", "#6E7074",
      "#546570", "#C4CCD3", "#F05B72", "#FF715E", "#FFAF51",
      "#FFE153", "#47B39C", "#5BACE1", "#32C5E9", "#96BFFF"
    ],
    background: '#4a4a4a',
  },

  // CodeMirror specific colors (adjusted for dark mode)
  codeMirror: {
    tableIcon: '#2F7E89',
    columnIcon: '#05a',
    functionIcon: '#74457a',
    hintBackground: '#2a2a2a',
    border: '#404040',
  },

  // Overlay and shadow colors (adjusted for dark mode)
  overlay: {
    shadow: 'rgba(0, 0, 0, 0.5)',
    shadowLight: 'rgba(0, 0, 0, 0.3)',
    primaryLight: 'rgba(66, 133, 244, 0.1)',
    primaryMedium: 'rgba(66, 133, 244, 0.15)',
    primaryHeavy: 'rgba(66, 133, 244, 0.2)',
    primaryHeavier: 'rgba(66, 133, 244, 0.25)',
    primaryAlpha: 'rgba(66, 133, 244, 0.3)',
  },

  // Semantic colors for specific use cases (adjusted for dark mode)
  semantic: {
    link: '#4285f4',
    linkHover: '#64b5f6',
    accent: '#4285f4',
    highlight: '#3a3a3a',
    muted: '#b0b0b0',
    disabled: 'rgba(255, 255, 255, 0.38)',
  },
};

export default darkColors;
