import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ThemeProvider } from 'styled-components';
import GlobalStyle from './components/General/GlobalStyle';

import Routes from './Routes';
import theme from './theme';
import AuthProvider from './contexts/AuthProvider';

/**
 * Main App component. Global elements are setup in this component.
 */
function App() {
  return (
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <Router>
          <GlobalStyle />
          <Routes />
        </Router>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
