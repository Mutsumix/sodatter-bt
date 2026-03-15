import { useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

const MOCK_DEVICES: Record<string, { cropName: string; daysElapsed: number; recentLogs: { date: string; imageUrl: string }[] }> = {
  A: {
    cropName: 'サニーレタス',
    daysElapsed: 32,
    recentLogs: [
      {
        date: '01/20',
        imageUrl: 'https://readdy.ai/api/search-image?query=Young%20lettuce%20seedling%20growing%20in%20a%20small%20pot%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20macro%20photography%2C%20high%20detail%2C%20green%20leaves&width=96&height=96&seq=ref-log-a-1&orientation=squarish',
      },
      {
        date: '02/01',
        imageUrl: 'https://readdy.ai/api/search-image?query=Healthy%20lettuce%20plant%20with%20multiple%20leaves%20in%20a%20pot%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20high%20detail&width=96&height=96&seq=ref-log-a-2&orientation=squarish',
      },
      {
        date: '02/10',
        imageUrl: 'https://readdy.ai/api/search-image?query=Mature%20lettuce%20plant%20with%20full%20leaves%20growing%20vigorously%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20high%20detail&width=96&height=96&seq=ref-log-a-3&orientation=squarish',
      },
    ],
  },
  B: {
    cropName: 'バジル',
    daysElapsed: 15,
    recentLogs: [],
  },
};

type FlowState = 'viewfinder' | 'preview';

export default function PhotoRecordPage() {
  const navigate = useNavigate();
  const { deviceId = 'A' } = useParams<{ deviceId: string }>();
  const device = MOCK_DEVICES[deviceId] ?? MOCK_DEVICES['A'];

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [flowState, setFlowState] = useState<FlowState>('viewfinder');
  const [capturedPhoto, setCapturedPhoto] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);

  const handleShutter = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => {
        setCapturedPhoto(ev.target?.result as string);
        setFlowState('preview');
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSkip = () => {
    navigate(`/detail/${deviceId}`);
  };

  const handleRetake = () => {
    setCapturedPhoto(null);
    setFlowState('viewfinder');
  };

  const handleSave = () => {
    setShowToast(true);
    setTimeout(() => {
      setShowToast(false);
      navigate(`/detail/${deviceId}`);
    }, 1800);
  };

  // ── Preview screen ──────────────────────────────────────────────
  if (flowState === 'preview' && capturedPhoto) {
    return (
      <div
        className="relative flex flex-col bg-black overflow-hidden"
        style={{ width: '375px', height: '812px', fontFamily: FONT }}
      >
        {/* Full-screen photo preview */}
        <img
          src={capturedPhoto}
          alt="Captured"
          className="absolute inset-0 w-full h-full object-cover"
        />

        {/* Bottom action bar */}
        <div
          className="absolute bottom-0 left-0 right-0 z-10 flex items-center gap-3 px-4 pb-10 pt-4"
          style={{ background: 'linear-gradient(to top, rgba(0,0,0,0.70) 80%, rgba(0,0,0,0))' }}
        >
          <button
            onClick={handleRetake}
            className="flex-1 h-12 border border-[#D4D4D4] rounded-lg text-white text-base bg-transparent transition-all active:opacity-70"
            style={{ fontFamily: FONT }}
          >
            撮り直す
          </button>
          <button
            onClick={handleSave}
            className="flex-1 h-12 border border-[#6DAE72] rounded-lg text-[#6DAE72] text-base bg-white transition-all active:opacity-80"
            style={{ fontFamily: FONT }}
          >
            保存
          </button>
        </div>

        {/* Toast */}
        {showToast && (
          <div
            className="absolute bottom-28 left-1/2 -translate-x-1/2 z-20 px-5 py-2.5 rounded-lg bg-[#1A1A1C]/80 text-white text-sm whitespace-nowrap"
            style={{ fontFamily: FONT }}
          >
            写真を保存しました
          </div>
        )}
      </div>
    );
  }

  // ── Viewfinder screen ───────────────────────────────────────────
  return (
    <div
      className="relative flex flex-col bg-black overflow-hidden"
      style={{ width: '375px', height: '812px', fontFamily: FONT }}
    >
      {/* Simulated camera feed */}
      <img
        src="https://readdy.ai/api/search-image?query=Close-up%20live%20camera%20view%20of%20a%20green%20leafy%20vegetable%20plant%20growing%20in%20a%20small%20indoor%20pot%2C%20natural%20daylight%2C%20slightly%20blurred%20background%2C%20realistic%20camera%20viewfinder%20perspective%2C%20vibrant%20green%2C%20no%20text&width=375&height=812&seq=photo-viewfinder-bg&orientation=portrait"
        alt=""
        className="absolute inset-0 w-full h-full object-cover"
        aria-hidden="true"
      />

      {/* Info bar — top */}
      <div
        className="absolute top-0 left-0 right-0 z-10 bg-white border-b border-[#6DAE72] flex items-center px-4"
        style={{ height: '44px' }}
      >
        <p className="text-sm text-[#1A1A1C] truncate" style={{ fontFamily: FONT }}>
          デバイス {deviceId}&nbsp;—&nbsp;
          <span className="text-[#1A1A1C]">{device.cropName}</span>
          <span className="text-[#5B8BD4] ml-2">{device.daysElapsed}日目</span>
        </p>
      </div>

      {/* Reference thumbnails strip */}
      {device.recentLogs.length > 0 && (
        <div
          className="absolute z-10 left-0 right-0 flex items-center gap-2 px-4"
          style={{ bottom: '108px' }}
        >
          {device.recentLogs.slice(-3).map((log, idx) => (
            <div key={idx} className="flex flex-col items-center gap-1 flex-shrink-0">
              <div
                className="overflow-hidden rounded"
                style={{ width: '48px', height: '48px', border: '1px solid #D4D4D4' }}
              >
                <img
                  src={log.imageUrl}
                  alt={`Ref ${log.date}`}
                  className="w-full h-full object-cover object-top"
                />
              </div>
              <span
                className="text-[0.5rem] text-white/80"
                style={{ fontFamily: FONT }}
              >
                {log.date}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* Bottom controls */}
      <div
        className="absolute bottom-0 left-0 right-0 z-10 flex items-center justify-center pb-10 pt-4"
        style={{
          height: '100px',
          background: 'linear-gradient(to top, rgba(0,0,0,0.65) 70%, rgba(0,0,0,0))',
        }}
      >
        {/* Skip — left */}
        <button
          onClick={handleSkip}
          className="absolute left-8 text-white/70 text-sm bg-transparent border-none outline-none active:opacity-50 transition-opacity"
          style={{ fontFamily: FONT }}
        >
          スキップ
        </button>

        {/* Shutter button — center */}
        <button
          onClick={handleShutter}
          className="flex items-center justify-center rounded-full bg-white active:scale-95 transition-transform"
          style={{
            width: '64px',
            height: '64px',
            border: '2px solid #6DAE72',
            flexShrink: 0,
          }}
          aria-label="Take photo"
        >
          <div
            className="rounded-full bg-white"
            style={{ width: '52px', height: '52px', border: '1.5px solid #D4D4D4' }}
          />
        </button>
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        className="hidden"
        onChange={handleFileChange}
      />
    </div>
  );
}
