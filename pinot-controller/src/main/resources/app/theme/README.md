# Pinot UI Theme System

This directory contains the consolidated theme system for the Pinot UI application. All colors used throughout the application are now centralized here for easy maintenance and future dark mode implementation.

## File Structure

```
theme/
├── colors.ts          # Consolidated color definitions
├── themeUtils.ts      # Theme utility functions
├── index.ts           # Main theme configuration (Material-UI)
├── typography.ts      # Typography configuration
├── color/             # Legacy color files (deprecated)
│   ├── primary.ts
│   └── secondary.ts
└── README.md          # This file
```

## Color Organization

The colors are organized by semantic meaning:

### Base Colors
- `common`: Basic colors like black, white, transparent

### Brand Colors
- `primary`: Primary brand colors (shades 50-900, A100-A700)
- `secondary`: Secondary brand colors (shades 50-900, A100-A700)

### Neutral Colors
- `neutral`: Grayscale colors (shades 50-900)

### Text Colors
- `text`: Text colors for different contexts (primary, secondary, disabled, etc.)

### Background Colors
- `background`: Background colors for different UI elements

### Border Colors
- `border`: Border colors for different UI elements

### Status Colors
- `status`: Colors for different status states (success, error, warning, info)
- `taskStatus`: Colors for task status indicators
- `segmentStatus`: Colors for segment status indicators

### Special Purpose Colors
- `chart`: Chart-specific colors
- `codeMirror`: CodeMirror editor colors
- `overlay`: Overlay and shadow colors
- `semantic`: Semantic colors for specific use cases

## Usage

### Importing Colors

```typescript
import { colors } from '../theme';
import { getColor, getThemeColors } from '../theme/themeUtils';

// Direct access to colors
const primaryColor = colors.primary[500];
const successColor = colors.status.success.main;

// Using utility functions
const primaryColor = getColor('primary.500');
const successColor = getColor('status.success.main');
```

### Using in Components

```typescript
import { makeStyles } from '@material-ui/core/styles';
import { colors } from '../theme';

const useStyles = makeStyles((theme) => ({
  container: {
    backgroundColor: colors.background.default,
    border: `1px solid ${colors.border.secondary}`,
  },
  button: {
    backgroundColor: colors.primary[500],
    color: colors.common.white,
    '&:hover': {
      backgroundColor: colors.primary[600],
    },
  },
}));
```

### Using Theme Utilities

```typescript
import {
  getTaskStatusColors,
  getSegmentStatusColors,
  getChartColor
} from '../theme/themeUtils';

// Get task status colors
const taskColors = getTaskStatusColors('completed');
// Returns: { background: '#e8f5e8', border: '#4caf50', text: '#2e7d32' }

// Get segment status colors
const segmentColors = getSegmentStatusColors('good');
// Returns: { background: 'rgba(76, 175, 80, 0.1)', border: '#4CAF50', text: '#4CAF50' }

// Get chart color by index
const chartColor = getChartColor(0);
// Returns: "#5470C6"
```

## Migration Guide

### Replacing Hardcoded Colors

**Before:**
```typescript
const styles = {
  backgroundColor: '#4285f4',
  borderColor: '#BDCCD9',
  color: '#666',
};
```

**After:**
```typescript
import { colors } from '../theme';

const styles = {
  backgroundColor: colors.primary[500],
  borderColor: colors.border.secondary,
  color: colors.text.secondary,
};
```

### Replacing Status Colors

**Before:**
```typescript
const statusColors = {
  completed: '#e8f5e8',
  error: '#ffebee',
};
```

**After:**
```typescript
import { getTaskStatusColors } from '../theme/themeUtils';

const statusColors = getTaskStatusColors('completed');
// Returns complete color object with background, border, and text colors
```

## Dark Mode Implementation

Dark mode has been fully implemented and is available via a toggle button in the header. The implementation includes:

### Features
- **Toggle Button**: Located in the header next to the timezone selector
- **Persistent Preference**: Theme choice is saved in localStorage
- **Instant Switching**: No page reload required
- **Complete Coverage**: All components adapt to theme changes
- **CSS Integration**: Material-UI and custom component overrides

### Usage
1. Click the sun/moon icon in the header to toggle between light and dark modes
2. The theme preference is automatically saved and restored on page reload
3. All components using the theme system automatically adapt

### Implementation Details
- **Dark Colors**: `theme/darkColors.ts` - Complete dark mode color palette
- **Theme Context**: `contexts/ThemeContext.tsx` - React context for theme state
- **Toggle Component**: `components/DarkModeToggle.tsx` - Header toggle button
- **CSS Overrides**: `styles/darkMode.css` - Dark mode styling

### Example Usage in Components
```typescript
import { useTheme } from '../contexts/ThemeContext';
import { getThemeColors } from '../theme/themeUtils';

const MyComponent = () => {
  const { isDarkMode } = useTheme();
  const colors = getThemeColors(); // Returns appropriate colors for current theme

  return (
    <div style={{ backgroundColor: colors.background.default }}>
      {/* Component content */}
    </div>
  );
};
```

For complete documentation, see `theme/DARK_MODE_IMPLEMENTATION.md`.

## Benefits

1. **Centralized Management**: All colors are defined in one place
2. **Consistency**: Ensures consistent color usage across the application
3. **Maintainability**: Easy to update colors globally
4. **Dark Mode Ready**: Structure supports easy dark mode implementation
5. **Type Safety**: TypeScript provides autocomplete and type checking
6. **Semantic Organization**: Colors are organized by meaning, not just values

## Legacy Files

The `color/primary.ts` and `color/secondary.ts` files are kept for backward compatibility but are deprecated. New code should use the consolidated `colors.ts` file.
