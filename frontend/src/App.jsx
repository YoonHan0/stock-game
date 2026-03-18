import { useEffect, useState, useCallback } from 'react';
import { getTodayQuiz, submitVote, getVoteStats, getIsVoteState } from './api';
import { TrendingUp, TrendingDown, RefreshCw } from 'lucide-react';

function App() {

  const [quiz, setQuiz] = useState(null);
  const [loading, setLoading] = useState(true);
  const [voted, setVoted] = useState(false);    // 투표 여부 
  const [stats, setStats] = useState(null);     // 투표 통계 정보

  useEffect(() => {
    fetchQuiz();
  }, []);

  const fetchQuiz = async () => {
    setLoading(true);
    try {
      const data = await getTodayQuiz();
      setQuiz(data);
    } catch (error) {
      console.error("퀴즈를 불러오는데 실패했습니다.", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {

    const data = await getVoteStats(quiz.quizId);

    setStats(data);

  };

  const handleVote = async (prediction) => {
    
    const userId = 'test-user-6'    // 임시 유저 아이디
    const isVodeState = await getIsVoteState(quiz.quizId || '', userId);
    
    if (isVodeState) {
      alert("이미 오늘의 투표를 완료하셨습니다!");
      return;
    }

    try {
      await submitVote({
        quizId: quiz.quizId,
        prediction: prediction,
        userId: "test-user-6" // 임시 유저 아이디
      });

      setVoted(true);
      fetchStats();

      alert(`${prediction === 'UP' ? '상승' : '하락'}에 투표하셨습니다!`);

    } catch (error) {
      alert("투표 중 오류가 발생했습니다.");
      console.error("투표 중 오류가 발생했습니다.", error);
    }
  };

  if (loading) return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  if (!quiz) return <div className="flex justify-center items-center h-screen">오늘의 퀴즈가 없습니다.</div>;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md text-center">
        <h1 className="text-xl font-bold text-gray-500 mb-2">오늘의 주가 예측 퀴즈</h1>
        <h2 className="text-4xl font-black text-gray-900 mb-6">{quiz.stockName}</h2>
        
        <div className="bg-gray-100 rounded-xl p-6 mb-8">
          <p className="text-sm text-gray-500 mb-1">현재가 (기준가: {quiz.basePrice?.toLocaleString()}원)</p>
          <p className="text-3xl font-mono font-bold text-blue-600">
            {quiz.currentPrice?.toLocaleString()}원
          </p>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <button 
            className="flex flex-col items-center justify-center p-6 bg-red-50 hover:bg-red-100 border-2 border-red-200 rounded-2xl transition-all group"
            onClick={() => handleVote('UP')}
            disabled={voted}
          >
            <TrendingUp size={48} className="text-red-500 group-hover:scale-110 transition-transform" />
            <span className="mt-2 font-bold text-red-600 text-lg">상승</span>
          </button>
          
          <button 
            className="flex flex-col items-center justify-center p-6 bg-blue-50 hover:bg-blue-100 border-2 border-blue-200 rounded-2xl transition-all group"
            onClick={() => handleVote('DOWN')}
            disabled={voted}
          >
            <TrendingDown size={48} className="text-blue-500 group-hover:scale-110 transition-transform" />
            <span className="mt-2 font-bold text-blue-600 text-lg">하락</span>
          </button>
        </div>

        {
          voted && stats && (
            <div className="mt-8 w-full animate-fade-in">
              <div className="flex justify-between mb-2 font-bold text-sm">
                <span className="text-red-500">상승 {stats.upPercentage}%</span>
                <span className="text-blue-500">하락 {stats.downPercentage}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-4 flex overflow-hidden">
                <div 
                  className="bg-red-500 h-full transition-all duration-1000" 
                  style={{ width: `${stats.upPercentage}%` }}
                />
                <div 
                  className="bg-blue-500 h-full transition-all duration-1000" 
                  style={{ width: `${stats.downPercentage}%` }}
                />
              </div>
              <p className="mt-2 text-xs text-gray-400">총 {stats.upCount + stats.downCount}명 참여</p>
            </div>
          )
        }

        <button onClick={fetchQuiz} className="mt-8 text-gray-400 hover:text-gray-600 flex items-center gap-2 text-sm mx-auto">
          <RefreshCw size={16} /> 새로고침
        </button>
      </div>
    </div>
  );
}

export default App;