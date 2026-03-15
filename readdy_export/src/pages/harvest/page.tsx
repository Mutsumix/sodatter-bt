import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

const MOCK_DEVICES: Record<string, {
  id: string;
  cropName: string;
  manufacturer: string;
  seedingDate: string;
  daysElapsed: number;
}> = {
  A: {
    id: 'A',
    cropName: 'ミニトマト',
    manufacturer: 'タキイ種苗',
    seedingDate: '2026/01/04',
    daysElapsed: 42,
  },
  B: {
    id: 'B',
    cropName: 'バジル',
    manufacturer: 'サカタのタネ',
    seedingDate: '2026/02/01',
    daysElapsed: 15,
  },
};

function formatDateJP(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function formatDateDisplay(dateStr: string): string {
  const [y, m, d] = dateStr.split('-');
  return `${y}/${m}/${d}`;
}

export default function HarvestPage() {
  const navigate = useNavigate();
  const { deviceId } = useParams<{ deviceId: string }>();
  const device = MOCK_DEVICES[deviceId ?? 'A'] ?? MOCK_DEVICES['A'];

  const [weight, setWeight] = useState<number | null>(null);
  const [scaleConnected, setScaleConnected] = useState(false);
  const [harvestDate, setHarvestDate] = useState(formatDateJP(new Date()));
  const [completed, setCompleted] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  // Simulate scale connecting after 2s and sending a weight reading
  useEffect(() => {
    const connectTimer = setTimeout(() => {
      setScaleConnected(true);
      setWeight(142.5);
    }, 2000);
    return () => clearTimeout(connectTimer);
  }, []);

  const handleTare = () => {
    setWeight(0);
  };

  const handleCompleteHarvest = () => {
    setCompleted(true);
  };

  const handlePrintLabel = () => {
    navigate(`/label-print/${device.id}`);
  };

  const triggerToast = (msg: string) => {
    setToastMessage(msg);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 2500);
  };

  const handleBack = () => navigate(`/detail/${device.id}`);

  if (completed) {
    return (
      <div
        className="flex flex-col items-center justify-center bg-white"
        style={{ width: '375px', minHeight: '812px', fontFamily: FONT }}
      >
        <div className="flex flex-col items-center gap-6 px-8">
          <div className="w-16 h-16 flex items-center justify-center border border-[#6DAE72] rounded-full">
            <i className="ri-scissors-line text-3xl text-[#6DAE72]"></i>
          </div>
          <div className="text-center flex flex-col gap-2">
            <p className="text-lg text-[#1A1A1C]">収穫が完了しました</p>
            <p className="text-sm text-[#6B6B6B]">
              デバイス {device.id}（{device.cropName}）
            </p>
            {weight !== null && weight > 0 && (
              <p className="text-sm text-[#6B6B6B]">
                収穫量：<span className="text-[#1A1A1C]">{weight} g</span>
              </p>
            )}
            <p className="text-sm text-[#6B6B6B]">
              収穫日：{formatDateDisplay(harvestDate)}
            </p>
          </div>
          <button
            onClick={() => navigate('/')}
            className="w-full h-12 border border-[#5B8BD4] rounded-lg text-[#5B8BD4] text-base bg-white transition-all hover:border-2 active:border-2"
            style={{ fontFamily: FONT }}
          >
            ホームに戻る
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className="flex flex-col bg-white"
      style={{ width: '375px', minHeight: '812px', fontFamily: FONT }}
    >
      {/* Header */}
      <header
        className="fixed top-0 bg-white border-b border-[#D4D4D4] z-10 flex items-center px-4 h-14 gap-3"
        style={{ width: '375px' }}
      >
        <button
          onClick={handleBack}
          className="w-8 h-8 flex items-center justify-center text-[#6B6B6B] hover:text-[#1A1A1C] transition-colors"
          aria-label="Back"
        >
          <i className="ri-arrow-left-line text-xl"></i>
        </button>
        <h1 className="text-xl font-normal text-[#1A1A1C]" style={{ fontFamily: FONT }}>
          収穫
        </h1>
      </header>

      {/* Scrollable Content */}
      <main className="flex-1 pt-14 pb-32 overflow-y-auto">
        <div className="px-4 py-6 flex flex-col gap-6">

          {/* Crop Info Card */}
          <div className="border border-[#D4D4D4] rounded-xl bg-white px-4 py-3 flex items-center gap-3">
            <span
              className="inline-flex items-center justify-center w-6 h-6 text-xs border border-[#6DAE72] text-[#6DAE72] rounded flex-shrink-0"
              style={{ fontFamily: FONT }}
            >
              {device.id}
            </span>
            <span className="text-base text-[#1A1A1C] flex-1" style={{ fontFamily: FONT }}>
              {device.cropName}
            </span>
            <div className="w-px h-4 bg-[#D4D4D4] flex-shrink-0"></div>
            <span className="text-sm text-[#6B6B6B] flex-shrink-0" style={{ fontFamily: FONT }}>
              播種日 {device.seedingDate}
            </span>
            <div className="w-px h-4 bg-[#D4D4D4] flex-shrink-0"></div>
            <span className="text-sm text-[#5B8BD4] flex-shrink-0 font-medium" style={{ fontFamily: FONT }}>
              Day {device.daysElapsed}
            </span>
          </div>

          {/* Weight Display — focal point */}
          <div className="flex flex-col items-center py-10 gap-5">
            {/* Weight value + Tare */}
            <div className="flex items-end gap-4">
              <span
                className="text-[#1A1A1C] font-normal leading-none"
                style={{ fontSize: '48px', fontFamily: FONT, letterSpacing: '-0.5px' }}
              >
                {weight === null ? '---' : weight % 1 === 0 ? `${weight}.0` : `${weight}`}
              </span>
              <span
                className="text-2xl text-[#6B6B6B] font-normal mb-1"
                style={{ fontFamily: FONT }}
              >
                g
              </span>
              <button
                onClick={handleTare}
                className="mb-1 px-3 h-7 border border-[#5B8BD4] rounded text-xs text-[#5B8BD4] bg-white hover:bg-[#5B8BD4]/5 transition-colors flex-shrink-0"
                style={{ fontFamily: FONT }}
              >
                風袋引き
              </button>
            </div>

            {/* Scale connection status */}
            <div className="flex items-center gap-2">
              <span
                className="w-2 h-2 rounded-full flex-shrink-0"
                style={{ backgroundColor: scaleConnected ? '#6DAE72' : '#D4D4D4' }}
              ></span>
              <span
                className="text-sm"
                style={{
                  fontFamily: FONT,
                  color: scaleConnected ? '#6B6B6B' : '#ABABAB',
                }}
              >
                {scaleConnected ? 'Decent Scale：接続済み' : '未接続'}
              </span>
            </div>
          </div>

          {/* Harvest Date */}
          <div className="flex flex-col gap-2">
            <label
              className="text-sm text-[#1A1A1C]"
              style={{ fontFamily: FONT }}
              htmlFor="harvest-date"
            >
              収穫日
            </label>
            <div className="relative">
              <input
                id="harvest-date"
                type="date"
                value={harvestDate}
                onChange={(e) => setHarvestDate(e.target.value)}
                className="w-full h-12 border border-[#D4D4D4] rounded-lg px-4 pr-12 text-base text-[#1A1A1C] bg-white focus:outline-none focus:border-[#5B8BD4] transition-colors appearance-none"
                style={{ fontFamily: FONT }}
              />
              <div className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center pointer-events-none">
                <i className="ri-calendar-line text-base text-[#5B8BD4]"></i>
              </div>
            </div>
          </div>

        </div>
      </main>

      {/* Fixed Bottom Actions */}
      <div
        className="fixed bottom-0 bg-white border-t border-[#D4D4D4] px-4 py-4 z-10 flex flex-col gap-3"
        style={{ width: '375px' }}
      >
        <button
          onClick={handleCompleteHarvest}
          className="w-full h-12 border border-[#6DAE72] rounded-lg text-[#6DAE72] text-base bg-white flex items-center justify-center gap-2 transition-all hover:border-2 active:border-2 font-normal"
          style={{ fontFamily: FONT }}
        >
          <div className="w-5 h-5 flex items-center justify-center">
            <i className="ri-scissors-line text-base text-[#6DAE72]"></i>
          </div>
          収穫を完了する
        </button>
        <button
          onClick={handlePrintLabel}
          className="w-full h-12 border border-[#5B8BD4] rounded-lg text-[#5B8BD4] text-base bg-white flex items-center justify-center gap-2 transition-all hover:border-2 active:border-2 font-normal"
          style={{ fontFamily: FONT }}
        >
          <div className="w-5 h-5 flex items-center justify-center">
            <i className="ri-printer-line text-base text-[#5B8BD4]"></i>
          </div>
          ラベルを印刷
        </button>
      </div>

      {/* Toast */}
      {showToast && (
        <div
          className="fixed bottom-36 left-1/2 -translate-x-1/2 z-50 bg-[#1A1A1C]/80 text-white text-sm px-5 py-2.5 rounded-full whitespace-nowrap"
          style={{ fontFamily: FONT }}
        >
          {toastMessage}
        </div>
      )}
    </div>
  );
}
