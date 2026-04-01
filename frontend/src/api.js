import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
});

const SOCIAL_LOGIN_URL_MAP = {
    google: import.meta.env.VITE_SOCIAL_LOGIN_GOOGLE_URL || `${API_BASE_URL}/auth/oauth2/authorization/google`,
    kakao: import.meta.env.VITE_SOCIAL_LOGIN_KAKAO_URL || `${API_BASE_URL}/auth/oauth2/authorization/kakao`,
    naver: import.meta.env.VITE_SOCIAL_LOGIN_NAVER_URL || `${API_BASE_URL}/auth/oauth2/authorization/naver`,
};

export const getSocialLoginUrl = (provider) => {
    return SOCIAL_LOGIN_URL_MAP[provider] || null;
};

export const getMyInfo = async () => {
    const response = await api.get('/auth/me');
    return response.data;
};

export const login = async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
};

export const signup = async (payload) => {
    const response = await api.post('/auth/signup', payload);
    return response.data;
};

export const logout = async () => {
    const response = await api.post('/auth/logout');
    return response.data;
};

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

export const getIsVoteState = async (quizId) => {
    const response = await api.get(`/quiz/${quizId}/check-vote`);

    return response.data;
};