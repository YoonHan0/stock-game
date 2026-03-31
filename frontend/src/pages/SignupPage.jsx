import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AlertDialog from '../components/AlertDialog';
import { useAuth } from '../auth/AuthContext';
import '../components/LoginScreen.css';
import './SignupPage.css';

function SignupPage() {
  const navigate = useNavigate();
  const { signupWithEmail, loginWithEmail } = useAuth();

  const [form, setForm] = useState({
    email: '',
    password: '',
    nickname: '',
  });
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

    if (!form.email.trim() || !form.password.trim() || !form.nickname.trim()) {
      showDialog('warning', '이메일, 비밀번호, 닉네임을 모두 입력해 주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await signupWithEmail({
        email: form.email.trim(),
        password: form.password,
        nickname: form.nickname.trim(),
      });

      await loginWithEmail({
        email: form.email.trim(),
        password: form.password,
      });

      navigate('/', { replace: true });
    } catch (error) {
      console.error('회원가입 요청에 실패했습니다.', error);
      showDialog('error', '회원가입에 실패했습니다. 입력 정보를 확인해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-container">
      <section className="login-card">
        <span className="badge">Stock Game</span>
        <h1 className="login-title">회원가입</h1>

        <AlertDialog
          isOpen={Boolean(dialogState)}
          type={dialogState?.type}
          message={dialogState?.message}
          onClose={closeDialog}
        />

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-label" htmlFor="signup-email">이메일</label>
          <input
            id="signup-email"
            name="email"
            type="email"
            autoComplete="email"
            className="login-input"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            disabled={submitting}
          />

          <label className="login-label" htmlFor="signup-password">비밀번호</label>
          <input
            id="signup-password"
            name="password"
            type="password"
            autoComplete="new-password"
            className="login-input"
            value={form.password}
            onChange={handleChange}
            placeholder="비밀번호 입력"
            disabled={submitting}
          />

          <label className="login-label" htmlFor="signup-nickname">닉네임</label>
          <input
            id="signup-nickname"
            name="nickname"
            type="text"
            autoComplete="nickname"
            className="login-input"
            value={form.nickname}
            onChange={handleChange}
            placeholder="닉네임 입력"
            disabled={submitting}
          />

          <button type="submit" className="login-submit" disabled={submitting}>
            {submitting ? '가입 처리 중...' : '회원가입'}
          </button>
        </form>

        <p className="auth-switch-copy">
          이미 계정이 있나요? <Link to="/login" className="auth-switch-link">로그인</Link>
        </p>
      </section>
    </div>
  );
}

export default SignupPage;
