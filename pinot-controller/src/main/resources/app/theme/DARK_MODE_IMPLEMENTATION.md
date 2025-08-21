# Dark Mode Implementation

## Overview
This document describes the dark mode implementation for the Pinot UI application, which provides a complete dark theme that can be toggled via a button in the header.

## Files Created/Updated

### New Files
1. **`theme/darkColors.ts`** - Dark mode color palette
2. **`components/DarkModeToggle.tsx`** - Dark mode toggle component
3. **`contexts/ThemeContext.tsx`** - Theme context for state management
4. **`styles/darkMode.css`** - CSS overrides for dark mode
5. **`theme/DARK_MODE_IMPLEMENTATION.md`** - This documentation

### Updated Files
1. **`theme/themeUtils.ts`** - Updated to support dark mode switching
2. **`components/Header.tsx`** - Added dark mode toggle button
3. **`App.tsx`** - Added ThemeProvider and theme initialization

## Architecture

### Color System
The dark mode implementation uses a dual-color system:

- **Light Theme Colors** (`theme/colors.ts`) - Original light theme colors
- **Dark Theme Colors** (`theme/darkColors.ts`) - Dark mode equivalent colors

### Theme Management
- **ThemeContext** - Provides theme state and functions to components
- **themeUtils** - Utility functions for theme switching and color access
- **localStorage** - Persists theme preference across sessions

### Component Integration
- **DarkModeToggle** - Toggle button in header
- **ThemeProvider** - Wraps the entire application
- **CSS Classes** - Applied to document body for styling

## Usage

### Using the Dark Mode Toggle
The dark mode toggle is automatically available in the header next to the timezone selector. Users can click the sun/moon icon to switch between light and dark modes.

### Accessing Theme Colors in Components
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

### Using Theme-Aware Colors
```typescript
import { getColor } from '../theme/themeUtils';

const styles = {
  backgroundColor: getColor('background.default'),
  color: getColor('text.primary'),
  border: `1px solid ${getColor('border.secondary')}`,
};
```

## Color Mapping

### Background Colors
| Light Mode | Dark Mode | Usage |
|------------|-----------|-------|
| `#ffffff` | `#121212` | Main background |
| `#fff` | `#1e1e1e` | Paper/surface background |
| `#333333` | `#000000` | Dark backgrounds |
| `#f5f5f5` | `#2d2d2d` | Light backgrounds |

### Text Colors
| Light Mode | Dark Mode | Usage |
|------------|-----------|-------|
| `rgba(0, 0, 0, 0.87)` | `rgba(255, 255, 255, 0.87)` | Primary text |
| `#666` | `#b0b0b0` | Secondary text |
| `#3B454E` | `#e0e0e0` | Dark text |

### Border Colors
| Light Mode | Dark Mode | Usage |
|------------|-----------|-------|
| `#BDCCD9` | `#404040` | Secondary borders |
| `#ccc` | `#555555` | Light borders |
| `#eaeaea` | `#333333` | Lighter borders |

### Status Colors
Status colors (success, error, warning, info) maintain their semantic meaning but are adjusted for better contrast in dark mode:

- **Success**: Green tones adjusted for dark backgrounds
- **Error**: Red tones adjusted for dark backgrounds
- **Warning**: Orange tones adjusted for dark backgrounds
- **Info**: Blue tones adjusted for dark backgrounds

## CSS Integration

### Material-UI Overrides
The dark mode CSS provides overrides for Material-UI components:

```css
.theme-dark .MuiPaper-root {
  background-color: #1e1e1e;
  color: rgba(255, 255, 255, 0.87);
}
```

### CodeMirror Integration
Dark mode styles for the code editor:

```css
.theme-dark .CodeMirror {
  background-color: #1e1e1e;
  color: rgba(255, 255, 255, 0.87);
}
```

### Custom Scrollbars
Dark mode scrollbar styling:

```css
.theme-dark ::-webkit-scrollbar-thumb {
  background: #555555;
}
```

## Implementation Details

### Theme Initialization
The theme is initialized when the app starts:

```typescript
// In App.tsx
React.useEffect(() => {
  initializeThemeMode();
}, []);
```

### Theme Persistence
Theme preference is stored in localStorage:

```typescript
localStorage.setItem('pinot_ui:themeMode', mode);
```

### Body Class Management
CSS classes are applied to the document body:

```typescript
document.body.classList.remove('theme-light', 'theme-dark');
document.body.classList.add(`theme-${mode}`);
```

### Context State Management
The ThemeContext provides reactive theme state:

```typescript
const { themeMode, isDarkMode, toggleTheme, setTheme } = useTheme();
```

## Benefits

1. **User Preference**: Users can choose their preferred theme
2. **Accessibility**: Dark mode reduces eye strain in low-light environments
3. **Consistency**: All components automatically adapt to theme changes
4. **Performance**: Theme switching is instant and doesn't require page reload
5. **Persistence**: Theme preference is remembered across sessions

## Future Enhancements

### System Theme Detection
```typescript
// Detect system preference
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
```

### Automatic Theme Switching
```typescript
// Switch theme based on time of day
const hour = new Date().getHours();
const isNightTime = hour < 6 || hour > 18;
```

### Custom Theme Support
```typescript
// Support for custom color schemes
interface CustomTheme {
  primary: string;
  secondary: string;
  // ... other colors
}
```

## Testing

### Manual Testing
1. Click the dark mode toggle in the header
2. Verify all components adapt to the new theme
3. Check that theme preference persists after page reload
4. Test in different browsers and devices

### Automated Testing
```typescript
// Test theme switching
test('should toggle theme mode', () => {
  const { getByLabelText } = render(<DarkModeToggle />);
  const toggle = getByLabelText('Switch to dark mode');
  fireEvent.click(toggle);
  expect(document.body).toHaveClass('theme-dark');
});
```

## Troubleshooting

### Common Issues

1. **Theme not persisting**: Check localStorage implementation
2. **CSS not applying**: Verify CSS import and class application
3. **Component not updating**: Ensure component uses theme context
4. **Performance issues**: Check for unnecessary re-renders

### Debug Tools
```typescript
// Debug theme state
console.log('Current theme:', getThemeMode());
console.log('Theme colors:', getThemeColors());
```

## Conclusion

The dark mode implementation provides a complete, user-friendly dark theme for the Pinot UI application. It maintains consistency with the existing design system while providing an alternative visual experience that reduces eye strain and improves accessibility.
