import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import { getSocialLoginUrl } from '../api';
import { useAuth } from '../auth/AuthContext';
import AlertDialog from './AlertDialog';
import './LoginScreen.css';

const SOCIAL_LOGIN_PROVIDERS = [
  { key: 'google', label: 'Google로 로그인', className: 'social-google' },
  { key: 'kakao', label: 'Kakao로 로그인', className: 'social-kakao' },
//   { key: 'naver', label: 'Naver로 로그인', className: 'social-naver' },
];

function LoginScreen() {
  const navigate = useNavigate();
  const { loginWithEmail } = useAuth();

  const [form, setForm] = useState({ email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [dialogState, setDialogState] = useState(null);

  const showDialog = (type, message) => {
    setDialogState({ type, message });
  };

  const closeDialog = () => {
    setDialogState(null);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.email.trim() || !form.password.trim()) {
      showDialog('warning', '이메일과 비밀번호를 입력해 주세요.');
      return;
    }

    try {
      setSubmitting(true);

      await loginWithEmail({
        email: form.email.trim(),
        password: form.password,
      });

      navigate('/', { replace: true });
    } catch (error) {
      console.error('로그인 요청에 실패했습니다.', error);
      showDialog('error', '로그인에 실패했습니다. 이메일/비밀번호를 확인해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSocialLogin = (provider) => {
    const socialLoginUrl = getSocialLoginUrl(provider);

    if (!socialLoginUrl) {
      showDialog('warning', '소셜 로그인 설정이 아직 준비되지 않았습니다.');
      return;
    }

    window.location.href = socialLoginUrl;
  };

  return (
    <div className="login-container">
      <section className="login-card">
        <span className="badge">Stock Game</span>
        <h1 className="login-title">로그인</h1>
        {/* <p className="login-desc">세션 쿠키 기반 인증으로 안전하게 접속합니다.</p> */}

        <AlertDialog
          isOpen={Boolean(dialogState)}
          type={dialogState?.type}
          message={dialogState?.message}
          onClose={closeDialog}
        />

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-label" htmlFor="email">이메일</label>
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            className="login-input"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            disabled={submitting}
          />

          <label className="login-label" htmlFor="password">비밀번호</label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            className="login-input"
            value={form.password}
            onChange={handleChange}
            placeholder="비밀번호 입력"
            disabled={submitting}
          />

          <button type="submit" className="login-submit" disabled={submitting}>
            <LogIn size={16} />
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="social-login-section">
          <div className="social-divider">
            <span>또는 소셜 계정으로 로그인</span>
          </div>

          <div className="social-buttons">
            {SOCIAL_LOGIN_PROVIDERS.map((provider) => (
              <button
                key={provider.key}
                type="button"
                className={`social-btn ${provider.className}`}
                onClick={() => handleSocialLogin(provider.key)}
                disabled={submitting}
              >
                {provider.label}
              </button>
            ))}
          </div>
        </div>

        <p className="auth-switch-copy">
          아직 계정이 없나요? <Link to="/signup" className="auth-switch-link">회원가입</Link>
        </p>
      </section>
    </div>
  );
}

export default LoginScreen;
