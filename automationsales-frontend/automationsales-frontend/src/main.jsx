// The starting point of the app: puts <App /> inside <div id="root"> in index.html.
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './styles.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* BrowserRouter lets us use real URLs like /leads and /customers */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
);
