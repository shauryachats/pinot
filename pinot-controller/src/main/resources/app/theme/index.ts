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

import { createMuiTheme } from '@material-ui/core/styles';

import colors from './colors';
import typography from './typography';

const theme = createMuiTheme({
  palette: {
    common: {
      black: colors.common.black,
      white: colors.common.white,
    },
    primary: {
      light: colors.primary[50],
      main: colors.primary[500],
      dark: colors.primary[900],
      contrastText: colors.common.white,
    },
    secondary: {
      light: colors.secondary[50],
      main: colors.secondary[500],
      dark: colors.secondary[900],
      contrastText: colors.common.white,
    },
    text: {
      primary: colors.text.primary,
      disabled: colors.text.disabled,
      hint: colors.text.hint,
    },
    background: {
      default: colors.background.default,
      paper: colors.background.paper,
    },
    error: {
      main: colors.status.error.main,
      light: colors.status.error.light,
      dark: colors.status.error.dark,
    },
    warning: {
      main: colors.status.warning.main,
      light: colors.status.warning.light,
      dark: colors.status.warning.dark,
    },
    success: {
      main: colors.status.success.main,
      light: colors.status.success.light,
      dark: colors.status.success.dark,
    },
    info: {
      main: colors.status.info.main,
      light: colors.status.info.light,
      dark: colors.status.info.dark,
    },
  },
  typography,
});

// Export the consolidated colors for use throughout the application
export { colors };

// TODO: remove all StyledButton usages
export default theme;
