// Wrap a page in <ProtectedRoute> to require a login.
// Add adminOnly to also require the ADMIN role.
//
// Note: this only decides what the screen shows. The real protection is in
// Spring Security (SecurityConfig + @PreAuthorize), which rejects the API calls.
import { Navigate } from 'react-router-dom';
import { getLogin, isAdmin } from '../api';

export default function ProtectedRoute({ adminOnly = false, children }) {
  if (!getLogin()) {
    return <Navigate to="/login" replace />;
  }
  if (adminOnly && !isAdmin()) {
    return <Navigate to="/" replace />;
  }
  return children;
}
