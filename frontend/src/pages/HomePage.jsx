import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TrendingUp, TrendingDown, RefreshCw, LogOut } from 'lucide-react';
import { getTodayQuiz, submitVote, getVoteStats, getIsVoteState } from '../api';
import { useAuth } from '../auth/AuthContext';
import Alert from '../components/Alert';
import '../App.css';

function VoteBar({ stats }) {
  const total = stats ? stats.upCount + stats.downCount : 0;
  const upPct = total === 0 ? 0 : stats.upPercentage;
  const downPct = total === 0 ? 0 : stats.downPercentage;

  return (
    <div className="vote-bar-wrap">
      <div className="vote-bar-track">
        {total === 0 ? (
          <div className="vote-bar-empty" />
        ) : (
          <>
            <div className="vote-bar-up" style={{ width: `${upPct}%` }} />
            <div className="vote-bar-down" style={{ width: `${downPct}%` }} />
          </>
        )}
      </div>
      <div className="vote-bar-labels">
        <span className="vbl-up">
          <span className="vbl-dot up" />
          상승 {total === 0 ? '-' : `${upPct}%`}
        </span>
        <span className="vbl-count">{total === 0 ? '아직 참여자 없음' : `${total}명 참여`}</span>
        <span className="vbl-down">
          하락 {total === 0 ? '-' : `${downPct}%`}
          <span className="vbl-dot down" />
        </span>
      </div>
    </div>
  );
}

function HomePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [quiz, setQuiz] = useState(null);
  const [loading, setLoading] = useState(true);
  const [voted, setVoted] = useState(false);
  const [stats, setStats] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [alertState, setAlertState] = useState(null);

  const showAlert = useCallback((type, message) => {
    setAlertState({ type, message });
  }, []);

  const clearAlert = useCallback(() => {
    setAlertState(null);
  }, []);

  const fetchQuiz = useCallback(async () => {
    const currentUserId = user?.id != null ? String(user.id) : null;
    if (!currentUserId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setErrorMessage('');
    clearAlert();

    try {
      const data = await getTodayQuiz();
      setQuiz(data);

      if (!data?.quizId) {
        setVoted(false);
        setStats(null);
        showAlert('warning', '현재 진행 중인 퀴즈가 없습니다. 잠시 후 다시 확인해 주세요.');
        return;
      }

      const [hasVoted, voteStats] = await Promise.all([
        getIsVoteState(data.quizId, currentUserId),
        getVoteStats(data.quizId),
      ]);

      setVoted(hasVoted);
      setStats(voteStats);
    } catch (error) {
      console.error('퀴즈를 불러오는데 실패했습니다.', error);
      setErrorMessage('데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
      setQuiz(null);
      setVoted(false);
      setStats(null);
      showAlert('error', '퀴즈 데이터를 가져오지 못했습니다. 네트워크 상태를 확인해 주세요.');
    } finally {
      setLoading(false);
    }
  }, [clearAlert, showAlert, user]);

  useEffect(() => {
    fetchQuiz();
  }, [fetchQuiz]);

  const fetchStats = useCallback(async (quizId) => {
    const data = await getVoteStats(quizId);
    setStats(data);
  }, []);

  const handleVote = async (prediction) => {
    const currentUserId = user?.id != null ? String(user.id) : null;

    if (!currentUserId) {
      showAlert('warning', '로그인 후 투표할 수 있습니다.');
      return;
    }

    if (!quiz?.quizId || submitting) {
      return;
    }

    const alreadyVoted = await getIsVoteState(quiz.quizId, currentUserId);
    if (alreadyVoted) {
      setVoted(true);
      await fetchStats(quiz.quizId);
      showAlert('warning', '이미 오늘 투표를 완료했습니다. 실시간 현황을 확인해 주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await submitVote({ quizId: quiz.quizId, prediction, userId: currentUserId });
      setVoted(true);
      await fetchStats(quiz.quizId);
      showAlert('success', prediction === 'UP' ? '상승에 투표하였습니다.' : '하락에 투표하였습니다.');
    } catch (error) {
      console.error('투표 중 오류가 발생했습니다.', error);
      showAlert('error', '투표 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  if (loading) {
    return (
      <div className="app-container">
        <div className="stock-card">
          <Alert type="info" message="실시간 데이터 확인 중입니다." />
          <span className="badge">실시간 데이터 확인 중</span>
          <p className="state-title">오늘의 종목을 불러오는 중입니다</p>
          <p className="state-desc">최신 시세와 참여 현황을 정리하고 있습니다.</p>
        </div>
      </div>
    );
  }

  if (!quiz) {
    return (
      <div className="app-container">
        <div className="stock-card">
          <Alert
            type={errorMessage ? 'error' : 'warning'}
            message={errorMessage || '현재 진행 중인 퀴즈가 없습니다.'}
            onClose={clearAlert}
          />
          <span className="badge">오늘의 퀴즈 없음</span>
          <p className="state-title">진행 중인 주식 퀴즈가 없습니다</p>
          <p className="state-desc">{errorMessage || '잠시 후 새로고침하면 새로운 종목이 열릴 수 있습니다.'}</p>
          <button type="button" className="refresh-btn" onClick={fetchQuiz}>
            <RefreshCw size={14} /> 다시 불러오기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <div className="stock-card">
        <div className="card-top">
          <span className="badge">오늘의 주식 퀴즈</span>
          <div className="card-actions">
            <button type="button" className="refresh-btn" onClick={handleLogout}>
              <LogOut size={14} /> 로그아웃
            </button>
            <button type="button" className="refresh-btn icon-only" onClick={fetchQuiz}>
              <RefreshCw size={15} />
            </button>
          </div>
        </div>

        {alertState ? (
          <Alert type={alertState.type} message={alertState.message} onClose={clearAlert} />
        ) : null}

        <div className="stock-info">
          <h1 className="stock-name">{quiz.stockName}</h1>
          <p className="game-desc">오늘 종가 기준으로 <strong>내일 종가</strong>가 오를지, 내릴지 예측해 보세요</p>
          <div className="price-row">
            <div className="price-item">
              <span className="price-label blue">현재가</span>
              <span className="price-value blue">{quiz.currentPrice?.toLocaleString()}원</span>
            </div>
          </div>
        </div>

        <div className="divider" />

        {!voted ? (
          <div className="vote-section">
            <p className="section-title">내일 종가를 예측해 보세요</p>
            <p className="section-desc">1인 1회</p>
            <div className="vote-buttons">
              <button
                type="button"
                className="vote-btn up"
                onClick={() => handleVote('UP')}
                disabled={submitting}
              >
                <TrendingUp size={22} />
                상승
              </button>
              <button
                type="button"
                className="vote-btn down"
                onClick={() => handleVote('DOWN')}
                disabled={submitting}
              >
                <TrendingDown size={22} />
                하락
              </button>
            </div>
            <VoteBar stats={stats} />
          </div>
        ) : (
          <div className="stats-section">
            <div className="stats-header">
              <p className="section-title">실시간 투표 현황</p>
              <span className="voted-chip">참여 완료</span>
            </div>
            <VoteBar stats={stats} />
            <div className="result-notice">
              <span className="result-notice-icon">⏰</span>
              <span>정답은 <strong>내일 장 마감 후</strong> 확인할 수 있어요</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default HomePage;
