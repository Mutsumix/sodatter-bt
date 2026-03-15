import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

// Simulated QR recognition after 2.5s for demo purposes
const SCAN_DELAY_MS = 2500;

export default function QRScanPage() {
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(true);
  const [detected, setDetected] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (scanning) {
      timerRef.current = setTimeout(() => {
        setDetected(true);
        setScanning(false);
        // Navigate to photo record screen with device info
        setTimeout(() => {
          navigate('/photo-record/A');
        }, 400);
      }, SCAN_DELAY_MS);
    }
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [scanning, navigate]);

  const handleCancel = () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    navigate(-1);
  };

  return (
    <div
      className="relative flex flex-col bg-black overflow-hidden"
      style={{ width: '375px', height: '812px', fontFamily: FONT }}
    >
      {/* Simulated camera background */}
      <img
        src="https://readdy.ai/api/search-image?query=Blurred%20garden%20background%20with%20green%20plants%20and%20natural%20light%2C%20bokeh%20effect%2C%20soft%20focus%2C%20outdoor%20gardening%20scene%2C%20warm%20natural%20tones%2C%20no%20text%2C%20no%20people&width=375&height=812&seq=qr-camera-bg&orientation=portrait"
        alt=""
        className="absolute inset-0 w-full h-full object-cover opacity-80"
        aria-hidden="true"
      />

      {/* Dark overlay top */}
      <div
        className="absolute top-0 left-0 right-0 flex items-center justify-center z-10"
        style={{
          height: '160px',
          background: 'linear-gradient(to bottom, rgba(0,0,0,0.72) 60%, rgba(0,0,0,0))',
        }}
      >
        <p
          className="text-white text-base text-center px-8 mt-12"
          style={{ fontFamily: FONT, letterSpacing: '0.01em' }}
        >
          デバイスのタグをスキャンしてください
        </p>
      </div>

      {/* Scan frame — centered */}
      <div className="absolute inset-0 flex items-center justify-center z-10">
        <div className="relative" style={{ width: '220px', height: '220px' }}>
          {/* Corner brackets */}
          {/* Top-left */}
          <span
            className="absolute top-0 left-0"
            style={{
              width: '28px', height: '28px',
              borderTop: '2px solid #5B8BD4',
              borderLeft: '2px solid #5B8BD4',
            }}
          />
          {/* Top-right */}
          <span
            className="absolute top-0 right-0"
            style={{
              width: '28px', height: '28px',
              borderTop: '2px solid #5B8BD4',
              borderRight: '2px solid #5B8BD4',
            }}
          />
          {/* Bottom-left */}
          <span
            className="absolute bottom-0 left-0"
            style={{
              width: '28px', height: '28px',
              borderBottom: '2px solid #5B8BD4',
              borderLeft: '2px solid #5B8BD4',
            }}
          />
          {/* Bottom-right */}
          <span
            className="absolute bottom-0 right-0"
            style={{
              width: '28px', height: '28px',
              borderBottom: '2px solid #5B8BD4',
              borderRight: '2px solid #5B8BD4',
            }}
          />

          {/* Scanning line animation */}
          {scanning && (
            <div
              className="absolute left-0 right-0"
              style={{
                height: '1.5px',
                background: 'linear-gradient(to right, transparent, #5B8BD4, transparent)',
                animation: 'scanLine 1.6s ease-in-out infinite',
                top: '50%',
              }}
            />
          )}

          {/* Detected flash */}
          {detected && (
            <div
              className="absolute inset-0 rounded"
              style={{
                border: '2px solid #6DAE72',
                background: 'rgba(109,174,114,0.08)',
              }}
            />
          )}
        </div>
      </div>

      {/* Dark overlay bottom */}
      <div
        className="absolute bottom-0 left-0 right-0 flex flex-col items-center justify-end z-10 pb-12"
        style={{
          height: '200px',
          background: 'linear-gradient(to top, rgba(0,0,0,0.72) 60%, rgba(0,0,0,0))',
        }}
      >
        {scanning ? (
          <button
            onClick={handleCancel}
            className="text-white text-base bg-transparent border-none outline-none active:opacity-70 transition-opacity"
            style={{ fontFamily: FONT }}
          >
            キャンセル
          </button>
        ) : (
          <p className="text-white text-sm" style={{ fontFamily: FONT }}>
            QRコードを検出しました…
          </p>
        )}
      </div>

      <style>{`
        @keyframes scanLine {
          0%   { top: 8%; opacity: 0.6; }
          50%  { top: 88%; opacity: 1; }
          100% { top: 8%; opacity: 0.6; }
        }
      `}</style>
    </div>
  );
}
