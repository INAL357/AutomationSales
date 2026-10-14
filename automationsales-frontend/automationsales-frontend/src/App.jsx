// =====================================================================
// App.jsx — which page is shown for which URL
// =====================================================================
import { Routes, Route } from 'react-router-dom';

import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import RequestDemoPage from './pages/RequestDemoPage';
import DashboardPage from './pages/DashboardPage';
import LeadsPage from './pages/LeadsPage';
import CustomersPage from './pages/CustomersPage';
import TasksPage from './pages/TasksPage';
import CallsPage from './pages/CallsPage';
import LicensesPage from './pages/LicensesPage';
import RenewalsPage from './pages/RenewalsPage';
import NotificationsPage from './pages/NotificationsPage';
import ProductsPage from './pages/ProductsPage';
import VendorsPage from './pages/VendorsPage';
import DocumentsPage from './pages/DocumentsPage';
import TeamPage from './pages/TeamPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <Routes>
      {/* Public pages: no login needed */}
      <Route path="/login" element={<LoginPage />} />
      {/* <Route path="/request-demo" element={<RequestDemoPage />} /> */}

      {/* Everything inside here needs a login, and is shown inside the sidebar layout */}
      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/leads" element={<LeadsPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/tasks" element={<TasksPage />} />
        <Route path="/calls" element={<CallsPage />} />
        <Route path="/licenses" element={<LicensesPage />} />
        <Route path="/renewals" element={<RenewalsPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/vendors" element={<VendorsPage />} />
        <Route path="/documents" element={<DocumentsPage />} />

        {/* Admin only */}
        <Route path="/team" element={<ProtectedRoute adminOnly><TeamPage /></ProtectedRoute>} />
      </Route>

      {/* Any other URL */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
