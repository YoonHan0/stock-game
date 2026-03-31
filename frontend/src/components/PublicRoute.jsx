import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function PublicRoute() {
  const { isAuthenticated, isAuthLoading } = useAuth();

  if (isAuthLoading) {
    return <div>인증 상태를 확인하는 중입니다...</div>;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default PublicRoute;
