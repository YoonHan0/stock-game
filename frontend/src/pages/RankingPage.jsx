import { useCallback, useEffect, useState } from 'react';
import { Trophy, Crown, Medal } from 'lucide-react';
import { getTopRankings, getMyRanking } from '../api';
import { useAuth } from '../auth/AuthContext';
import Alert from '../components/Alert';
import BottomNav from '../components/BottomNav';
import './RankingPage.css';

function RankingPage() {
  const { user } = useAuth();

  const [rankings, setRankings] = useState([]);
  const [myRanking, setMyRanking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      const [top, me] = await Promise.all([
        getTopRankings(5),
        getMyRanking(),
      ]);
      setRankings(top);
      setMyRanking(me);
    } catch (err) {
      console.error('랭킹 로딩 실패', err);
      setError('랭킹을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const getRankIcon = (rank) => {
    if (rank === 1) return <Crown size={18} className="rank-icon gold" />;
    if (rank === 2) return <Medal size={18} className="rank-icon silver" />;
    if (rank === 3) return <Medal size={18} className="rank-icon bronze" />;
    return <span className="rank-number">{rank}</span>;
  };

  if (loading) {
    return (
      <div className="ranking-container">
        <div className="ranking-card">
          <p className="ranking-loading">로딩 중...</p>
        </div>
        <BottomNav />
      </div>
    );
  }

  return (
    <div className="ranking-container has-bottom-nav">
      <div className="ranking-card">
        <div className="ranking-header">
          <h1 className="ranking-title">랭킹</h1>
        </div>

        {error && <Alert type="error" message={error} onClose={() => setError('')} />}

        {myRanking && (
          <div className="my-ranking-section">
            <div className="my-ranking-card">
              <Trophy size={24} className="my-ranking-icon" />
              <div className="my-ranking-info">
                <span className="my-ranking-label">나의 순위</span>
                <span className="my-ranking-value">
                  {myRanking.rank}위
                  <span className="my-ranking-total"> / {myRanking.totalUsers}명</span>
                </span>
              </div>
              <div className="my-ranking-points">
                <span className="my-points-value">{myRanking.points?.toLocaleString()}</span>
                <span className="my-points-label">P</span>
              </div>
            </div>
          </div>
        )}

        <div className="top-ranking-section">
          <h3 className="section-heading">Top 5</h3>

          {rankings.length === 0 ? (
            <div className="ranking-empty">
              <p>아직 랭킹 데이터가 없습니다.</p>
            </div>
          ) : (
            <div className="ranking-list">
              {rankings.map((entry, idx) => (
                <div key={idx} className={`ranking-item rank-${entry.rank}`}>
                  <div className="ranking-rank">
                    {getRankIcon(entry.rank)}
                  </div>
                  <div className="ranking-user">
                    <span className="ranking-nickname">{entry.nickname}</span>
                  </div>
                  <div className="ranking-points">
                    <span className="ranking-points-value">{entry.points?.toLocaleString()}</span>
                    <span className="ranking-points-label">P</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <BottomNav />
    </div>
  );
}

export default RankingPage;
