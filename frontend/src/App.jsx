import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import PublicRoute from './components/PublicRoute';
import LoginScreen from './components/LoginScreen';
import HomePage from './pages/HomePage';
import SignupPage from './pages/SignupPage';
import MyPage from './pages/MyPage';
import RankingPage from './pages/RankingPage';

function App() {
  return (
    <Routes>
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<LoginScreen />} />
        <Route path="/signup" element={<SignupPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/ranking" element={<RankingPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;