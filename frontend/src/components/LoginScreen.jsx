import { useState } from 'react';
import { LogIn } from 'lucide-react';
import { getSocialLoginUrl, login } from '../api';
import Alert from './Alert';
import './LoginScreen.css';

const SOCIAL_LOGIN_PROVIDERS = [
  { key: 'google', label: 'Google로 로그인', className: 'social-google' },
  { key: 'kakao', label: 'Kakao로 로그인', className: 'social-kakao' },
//   { key: 'naver', label: 'Naver로 로그인', className: 'social-naver' },
];

function LoginScreen({ onLoginSuccess }) {
  const [form, setForm] = useState({ email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [alertState, setAlertState] = useState(null);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.email.trim() || !form.password.trim()) {
      setAlertState({ type: 'warning', message: '이메일과 비밀번호를 입력해 주세요.' });
      return;
    }

    try {
      setSubmitting(true);
      setAlertState(null);

      await login({
        email: form.email.trim(),
        password: form.password,
      });

      window.dispatchEvent(new Event('auth:login-success'));
      setAlertState({ type: 'success', message: '로그인에 성공했습니다.' });

      console.log("=== 로그인 성공 확인 ===")

      if (onLoginSuccess) {
        await onLoginSuccess();
      }
    } catch (error) {
      console.error('로그인 요청에 실패했습니다.', error);
      setAlertState({
        type: 'error',
        message: '로그인에 실패했습니다. 이메일/비밀번호를 확인해 주세요.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleSocialLogin = (provider) => {
    const socialLoginUrl = getSocialLoginUrl(provider);

    if (!socialLoginUrl) {
      setAlertState({ type: 'warning', message: '소셜 로그인 설정이 아직 준비되지 않았습니다.' });
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

        {alertState ? (
          <Alert
            type={alertState.type}
            message={alertState.message}
            onClose={() => setAlertState(null)}
          />
        ) : null}

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
      </section>
    </div>
  );
}

export default LoginScreen;
