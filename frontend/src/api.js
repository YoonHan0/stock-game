import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
});

export const getTodayQuiz = async () => {
    const response = await api.get('/quiz/today');
    return response.data;
};

export const submitVote = async (voteData) => {
    const response = await api.post('/quiz/vote', voteData);
    return response.data;
};

export const getVoteStats = async (quizId) => {
    const response = await api.get(`/quiz/${quizId}/stats`);
    return response.data;
};

export const getIsVoteState = async (quizId, userId) => {
    
    // 이런 식으로 '' or undefined 일 때의 처리가 필요
    // if(quizId) {
    //     alert('존재하는 투표가 없습니다.')
    // }

    const response = await api.get(`/quiz/${quizId}/check-vote`, 
        {
            params: { userId }
        }
    );
    
    return response.data;
};