import { NavLink } from 'react-router-dom';
import { Home, User, Trophy } from 'lucide-react';
import './BottomNav.css';

function BottomNav() {
  return (
    <nav className="bottom-nav">
      <NavLink to="/" end className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
        <Home size={20} />
        <span>홈</span>
      </NavLink>
      <NavLink to="/mypage" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
        <User size={20} />
        <span>마이</span>
      </NavLink>
      <NavLink to="/ranking" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
        <Trophy size={20} />
        <span>랭킹</span>
      </NavLink>
    </nav>
  );
}

export default BottomNav;
