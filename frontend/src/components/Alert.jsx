import { AlertTriangle, CheckCircle2, Info, X, XCircle } from 'lucide-react';
import './Alert.css';

const TYPE_CONFIG = {
  success: {
    icon: CheckCircle2,
    label: '성공',
  },
  warning: {
    icon: AlertTriangle,
    label: '주의',
  },
  error: {
    icon: XCircle,
    label: '오류',
  },
  info: {
    icon: Info,
    label: '안내',
  },
};

function Alert({ type = 'info', message, onClose }) {
  if (!message) {
    return null;
  }

  const config = TYPE_CONFIG[type] || TYPE_CONFIG.info;
  const Icon = config.icon;

  return (
    <div className={`app-alert app-alert-${type}`} role="status" aria-live="polite">
      <div className="app-alert-main">
        <Icon size={18} className="app-alert-icon" />
        <div className="app-alert-copy">
          <span className="app-alert-label">{config.label}</span>
          <p>{message}</p>
        </div>
      </div>
      {onClose ? (
        <button type="button" className="app-alert-close" onClick={onClose} aria-label="알림 닫기">
          <X size={16} />
        </button>
      ) : null}
    </div>
  );
}

export default Alert;
