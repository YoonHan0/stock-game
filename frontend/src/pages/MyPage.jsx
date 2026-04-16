import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Target, TrendingUp, TrendingDown, Clock, CheckCircle, MinusCircle, LogOut } from 'lucide-react';
import { getUserProfile, getVoteHistory } from '../api';
import { useAuth } from '../auth/AuthContext';
import Alert from '../components/Alert';
import BottomNav from '../components/BottomNav';
import './MyPage.css';

function MyPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [profile, setProfile] = useState(null);
  const [history, setHistory] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchProfile = useCallback(async () => {
    try {
      const data = await getUserProfile();
      setProfile(data);
    } catch (err) {
      console.error('프로필 로딩 실패', err);
      setError('프로필을 불러오지 못했습니다.');
    }
  }, []);

  const fetchHistory = useCallback(async (pageNum) => {
    setHistoryLoading(true);
    try {
      const data = await getVoteHistory(pageNum, 10);
      if (pageNum === 0) {
        setHistory(data.items);
      } else {
        setHistory(prev => [...prev, ...data.items]);
      }
      setPage(data.page);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error('히스토리 로딩 실패', err);
      setError('투표 히스토리를 불러오지 못했습니다.');
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!user) return;
    (async () => {
      setLoading(true);
      await Promise.all([fetchProfile(), fetchHistory(0)]);
      setLoading(false);
    })();
  }, [user, fetchProfile, fetchHistory]);

  const handleLoadMore = () => {
    if (page + 1 < totalPages) {
      fetchHistory(page + 1);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const getResultIcon = (item) => {
    if (item.settlementStatus === '정산 대기') {
      return <Clock size={16} className="result-icon pending" />;
    }
    if (item.isCorrect === true) {
      return <CheckCircle size={16} className="result-icon correct" />;
    }
    if (item.isCorrect === false) {
      return <MinusCircle size={16} className="result-icon wrong" />;
    }
    return <Target size={16} className="result-icon flat" />;
  };

  const getResultText = (item) => {
    if (item.settlementStatus === '정산 대기') return '정산 대기';
    if (item.isCorrect === true) return '적중';
    if (item.isCorrect === false) return '미적중';
    return '보합';
  };

  const getResultClass = (item) => {
    if (item.settlementStatus === '정산 대기') return 'pending';
    if (item.isCorrect === true) return 'correct';
    if (item.isCorrect === false) return 'wrong';
    return 'flat';
  };

  if (loading) {
    return (
      <div className="mypage-container">
        <div className="mypage-card">
          <p className="mypage-loading">로딩 중...</p>
        </div>
        <BottomNav />
      </div>
    );
  }

  return (
    <div className="mypage-container has-bottom-nav">
      <div className="mypage-card">
        <div className="mypage-header">
          <h1 className="mypage-title">마이페이지</h1>
        </div>

        {error && <Alert type="error" message={error} onClose={() => setError('')} />}

        {profile && (
          <div className="profile-section">
            <div className="profile-avatar">
              {profile.nickname?.charAt(0)?.toUpperCase()}
            </div>
            <h2 className="profile-nickname">{profile.nickname}</h2>
            <p className="profile-email">{profile.email}</p>

            <div className="stats-grid">
              <div className="stat-item">
                <Trophy size={20} className="stat-icon points" />
                <span className="stat-value">{profile.points?.toLocaleString()}</span>
                <span className="stat-label">포인트</span>
              </div>
              <div className="stat-item">
                <Target size={20} className="stat-icon accuracy" />
                <span className="stat-value">{profile.accuracy}%</span>
                <span className="stat-label">적중률</span>
              </div>
              <div className="stat-item">
                <TrendingUp size={20} className="stat-icon votes" />
                <span className="stat-value">{profile.totalVotes}</span>
                <span className="stat-label">총 투표</span>
              </div>
            </div>

            <button type="button" className="logout-btn" onClick={handleLogout}>
              <LogOut size={14} /> 로그아웃
            </button>
          </div>
        )}

        <div className="history-section">
          <h3 className="history-title">투표 히스토리</h3>

          {history.length === 0 ? (
            <div className="history-empty">
              <p>아직 투표 기록이 없습니다.</p>
              <p className="history-empty-sub">오늘의 퀴즈에 참여해 보세요!</p>
            </div>
          ) : (
            <>
              <div className="history-list">
                {history.map((item, idx) => (
                  <div key={`${item.quizId}-${idx}`} className={`history-item ${getResultClass(item)}`}>
                    <div className="history-item-top">
                      <span className="history-date">{item.quizDate}</span>
                      <span className={`history-result-badge ${getResultClass(item)}`}>
                        {getResultIcon(item)}
                        {getResultText(item)}
                      </span>
                    </div>
                    <div className="history-item-mid">
                      <span className="history-stock">{item.stockName}</span>
                      <span className={`history-prediction ${item.prediction?.toLowerCase()}`}>
                        {item.prediction === 'UP' ? (
                          <><TrendingUp size={14} /> 상승</>
                        ) : (
                          <><TrendingDown size={14} /> 하락</>
                        )}
                      </span>
                    </div>
                    <div className="history-item-bottom">
                      {item.settlementStatus === '정산 완료' && item.pointsEarned != null && (
                        <span className="history-points">+{item.pointsEarned}P</span>
                      )}
                      {item.quizResult && (
                        <span className="history-quiz-result">결과: {item.quizResult}</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {page + 1 < totalPages && (
                <button
                  type="button"
                  className="load-more-btn"
                  onClick={handleLoadMore}
                  disabled={historyLoading}
                >
                  {historyLoading ? '불러오는 중...' : '더 보기'}
                </button>
              )}
            </>
          )}
        </div>
      </div>
      <BottomNav />
    </div>
  );
}

export default MyPage;
