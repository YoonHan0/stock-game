import { useEffect } from 'react';
import { AlertTriangle, CheckCircle2, Info, XCircle } from 'lucide-react';
import './AlertDialog.css';

const TYPE_CONFIG = {
  success: {
    icon: CheckCircle2,
    title: '성공',
  },
  warning: {
    icon: AlertTriangle,
    title: '주의',
  },
  error: {
    icon: XCircle,
    title: '오류',
  },
  info: {
    icon: Info,
    title: '안내',
  },
};

function AlertDialog({ isOpen, type = 'info', message, onClose }) {
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const onKeyDown = (event) => {
      if (event.key === 'Escape' || event.key === 'Enter') {
        onClose();
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => {
      window.removeEventListener('keydown', onKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen || !message) {
    return null;
  }

  const config = TYPE_CONFIG[type] || TYPE_CONFIG.info;
  const Icon = config.icon;

  return (
    <div className="alert-dialog-overlay" role="presentation" onClick={onClose}>
      <section
        className={`alert-dialog alert-dialog-${type}`}
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-message"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="alert-dialog-head">
          <span className="alert-dialog-icon-wrap" aria-hidden="true">
            <Icon size={20} className="alert-dialog-icon" />
          </span>
          <h2 id="alert-dialog-title" className="alert-dialog-title">
            {config.title}
          </h2>
        </div>

        <p id="alert-dialog-message" className="alert-dialog-message">
          {message}
        </p>

        <button type="button" className="alert-dialog-confirm" onClick={onClose} autoFocus>
          확인
        </button>
      </section>
    </div>
  );
}

export default AlertDialog;
