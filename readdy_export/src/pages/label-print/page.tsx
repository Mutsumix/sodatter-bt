import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

const MOCK_DEVICES: Record<string, {
  id: string;
  cropName: string;
  manufacturer: string;
  seedingDate: string;
  harvestDate: string;
  weight: number;
  daysElapsed: number;
}> = {
  A: {
    id: 'A',
    cropName: 'ミニトマト',
    manufacturer: 'タキイ種苗',
    seedingDate: '2026/01/04',
    harvestDate: '2026/02/15',
    weight: 142.5,
    daysElapsed: 42,
  },
  B: {
    id: 'B',
    cropName: 'バジル',
    manufacturer: 'サカタのタネ',
    seedingDate: '2026/02/01',
    harvestDate: '2026/03/18',
    weight: 87.0,
    daysElapsed: 45,
  },
};

// Minimal QR code rendered as a CSS grid pattern
function QRCodeMock({ size = 64 }: { size?: number }) {
  // A fixed decorative QR-like pattern using small squares
  const cells = [
    [1,1,1,1,1,1,1,0,1,0,0,1,1,1,1,1,1,1,1],
    [1,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,1],
    [1,0,1,1,1,0,1,0,1,0,1,0,1,0,1,1,1,0,1],
    [1,0,1,1,1,0,1,0,0,1,1,0,1,0,1,1,1,0,1],
    [1,0,1,1,1,0,1,0,1,0,0,1,1,0,1,1,1,0,1],
    [1,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,1],
    [1,1,1,1,1,1,1,0,1,0,1,0,1,1,1,1,1,1,1],
    [0,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,0],
    [1,0,1,1,0,1,1,1,0,0,1,0,1,1,0,1,0,1,1],
    [0,1,0,0,1,0,0,0,1,0,0,1,0,0,1,0,1,0,0],
    [1,1,0,1,0,1,1,0,1,1,0,1,1,0,0,1,0,1,1],
    [0,0,0,0,0,0,0,0,1,0,1,0,0,1,0,0,1,0,0],
    [1,1,1,1,1,1,1,0,0,1,0,1,1,0,1,0,1,1,0],
    [1,0,0,0,0,0,1,0,1,0,1,0,0,1,0,1,0,0,1],
    [1,0,1,1,1,0,1,0,0,1,1,0,1,0,1,1,0,1,0],
    [1,0,1,1,1,0,1,0,1,0,0,1,0,1,0,0,1,0,1],
    [1,0,1,1,1,0,1,0,1,1,0,0,1,0,1,1,0,1,1],
    [1,0,0,0,0,0,1,0,0,0,1,1,0,1,0,0,1,0,0],
    [1,1,1,1,1,1,1,0,1,0,1,0,1,1,0,1,1,0,1],
  ];
  const cellSize = size / cells.length;
  return (
    <div
      style={{ width: size, height: size, display: 'grid', gridTemplateRows: `repeat(${cells.length}, ${cellSize}px)`, flexShrink: 0 }}
    >
      {cells.map((row, ri) => (
        <div key={ri} style={{ display: 'grid', gridTemplateColumns: `repeat(${row.length}, ${cellSize}px)` }}>
          {row.map((cell, ci) => (
            <div
              key={ci}
              style={{
                width: cellSize,
                height: cellSize,
                backgroundColor: cell ? '#1A1A1C' : '#FFFFFF',
              }}
            />
          ))}
        </div>
      ))}
    </div>
  );
}

export default function LabelPrintPage() {
  const navigate = useNavigate();
  const { deviceId } = useParams<{ deviceId: string }>();
  const device = MOCK_DEVICES[deviceId ?? 'A'] ?? MOCK_DEVICES['A'];

  const [printerConnected, setPrinterConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [printing, setPrinting] = useState(false);

  // Simulate printer auto-connecting after 1.5s
  useEffect(() => {
    const t = setTimeout(() => setPrinterConnected(true), 1500);
    return () => clearTimeout(t);
  }, []);

  const triggerToast = (msg: string) => {
    setToastMessage(msg);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 2500);
  };

  const handleConnect = () => {
    setConnecting(true);
    setTimeout(() => {
      setConnecting(false);
      setPrinterConnected(true);
      triggerToast('プリンター接続済み');
    }, 1200);
  };

  const handlePrint = () => {
    if (!printerConnected) {
      triggerToast('先にプリンターを接続してください');
      return;
    }
    setPrinting(true);
    setTimeout(() => {
      setPrinting(false);
      triggerToast('ラベルをプリンターに送信しました');
    }, 1800);
  };

  const handleBack = () => navigate(`/harvest/${device.id}`);

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
          ラベルプレビュー
        </h1>
      </header>

      {/* Scrollable Content */}
      <main className="flex-1 pt-14 pb-36 overflow-y-auto">
        <div className="px-4 py-8 flex flex-col gap-6">

          {/* Label Mockup */}
          <div className="flex flex-col items-center gap-2">
            {/* Receipt/Label card */}
            <div
              className="w-full bg-white border border-[#D4D4D4] rounded"
              style={{ maxWidth: '300px' }}
            >
              {/* Perforated top edge */}
              <div
                className="w-full h-3 flex items-center overflow-hidden"
                style={{ borderBottom: '1px dashed #D4D4D4' }}
              >
                <div className="flex w-full justify-between px-1">
                  {Array.from({ length: 28 }).map((_, i) => (
                    <div key={i} className="w-1 h-1 rounded-full bg-[#D4D4D4]" />
                  ))}
                </div>
              </div>

              {/* Label content */}
              <div className="px-5 pt-5 pb-5 flex flex-col gap-3">
                {/* App name watermark */}
                <div className="flex items-center gap-1.5 mb-1">
                  <div className="w-4 h-4 flex items-center justify-center">
                    <i className="ri-leaf-line text-sm text-[#6DAE72]"></i>
                  </div>
                  <span className="text-[10px] text-[#ABABAB] tracking-widest uppercase" style={{ fontFamily: FONT }}>
                    Sodatter-BT
                  </span>
                </div>

                {/* Crop name */}
                <div>
                  <p className="text-lg font-bold text-[#1A1A1C] leading-tight" style={{ fontFamily: FONT }}>
                    {device.cropName}
                  </p>
                  <p className="text-xs text-[#6B6B6B] mt-0.5" style={{ fontFamily: FONT }}>
                    {device.manufacturer}
                  </p>
                </div>

                {/* Thin divider */}
                <div className="w-full h-px bg-[#D4D4D4]" />

                {/* Dates row */}
                <div className="flex flex-col gap-1.5">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 flex items-center justify-center flex-shrink-0">
                      <i className="ri-seedling-line text-xs text-[#6DAE72]"></i>
                    </div>
                    <span className="text-sm text-[#1A1A1C]" style={{ fontFamily: FONT }}>
                      播種：&nbsp;
                      <span className="font-medium">{device.seedingDate}</span>
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 flex items-center justify-center flex-shrink-0">
                      <i className="ri-scissors-line text-xs text-[#6DAE72]"></i>
                    </div>
                    <span className="text-sm text-[#1A1A1C]" style={{ fontFamily: FONT }}>
                      収穫：&nbsp;
                      <span className="font-medium">{device.harvestDate}</span>
                    </span>
                  </div>
                </div>

                {/* Thin divider */}
                <div className="w-full h-px bg-[#D4D4D4]" />

                {/* Weight + QR row */}
                <div className="flex items-end justify-between">
                  <div className="flex flex-col gap-1">
                    <span className="text-xs text-[#6B6B6B]" style={{ fontFamily: FONT }}>重量</span>
                    <span
                      className="text-[#1A1A1C] font-normal leading-none"
                      style={{ fontSize: '36px', fontFamily: FONT, letterSpacing: '-0.5px' }}
                    >
                      {device.weight % 1 === 0 ? `${device.weight}.0` : `${device.weight}`}
                    </span>
                    <span className="text-sm text-[#6B6B6B]" style={{ fontFamily: FONT }}>g</span>
                    <div className="flex items-center gap-1.5 mt-2">
                      <span
                        className="inline-flex items-center justify-center w-5 h-5 text-[10px] border border-[#6DAE72] text-[#6DAE72] rounded"
                        style={{ fontFamily: FONT }}
                      >
                        {device.id}
                      </span>
                      <span className="text-xs text-[#6B6B6B]" style={{ fontFamily: FONT }}>
                        Day {device.daysElapsed}
                      </span>
                    </div>
                  </div>

                  {/* QR Code */}
                  <div className="flex flex-col items-center gap-1">
                    <div className="border border-[#D4D4D4] rounded p-1 bg-white">
                      <QRCodeMock size={64} />
                    </div>
                  </div>
                </div>
              </div>

              {/* Perforated bottom edge */}
              <div
                className="w-full h-3 flex items-center overflow-hidden"
                style={{ borderTop: '1px dashed #D4D4D4' }}
              >
                <div className="flex w-full justify-between px-1">
                  {Array.from({ length: 28 }).map((_, i) => (
                    <div key={i} className="w-1 h-1 rounded-full bg-[#D4D4D4]" />
                  ))}
                </div>
              </div>
            </div>

            {/* Caption below label */}
            <p className="text-xs text-[#6B6B6B] text-center" style={{ fontFamily: FONT }}>
              QRコードは生育フォトログにリンクします
            </p>
          </div>

          {/* Printer Status Section */}
          <div className="border border-[#D4D4D4] rounded-lg bg-white px-4 py-3">
            <div className="flex items-center gap-3">
              {/* Printer icon */}
              <div className="w-8 h-8 flex items-center justify-center border border-[#5B8BD4] rounded text-[#5B8BD4] flex-shrink-0">
                <i className="ri-printer-line text-base"></i>
              </div>

              {/* Printer name + status */}
              <div className="flex-1 min-w-0">
                <p className="text-sm text-[#1A1A1C] truncate" style={{ fontFamily: FONT }}>
                  Star SM-S210i
                </p>
                <div className="flex items-center gap-1.5 mt-0.5">
                  <span
                    className="w-2 h-2 rounded-full flex-shrink-0 transition-colors duration-500"
                    style={{ backgroundColor: printerConnected ? '#6DAE72' : '#D4D4D4' }}
                  />
                  <span
                    className="text-xs transition-colors duration-500"
                    style={{ fontFamily: FONT, color: printerConnected ? '#6B6B6B' : '#ABABAB' }}
                  >
                    {printerConnected ? '接続済み' : '未接続'}
                  </span>
                </div>
              </div>

              {/* Connect link — only when not connected */}
              {!printerConnected && (
                <button
                  onClick={handleConnect}
                  disabled={connecting}
                  className="text-sm text-[#5B8BD4] hover:underline flex-shrink-0 disabled:opacity-50"
                  style={{ fontFamily: FONT }}
                >
                  {connecting ? '接続中…' : '接続する'}
                </button>
              )}
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
          onClick={handlePrint}
          disabled={printing}
          className="w-full h-12 border border-[#5B8BD4] rounded-lg text-[#5B8BD4] text-base bg-white flex items-center justify-center gap-2 transition-all hover:border-2 active:border-2 font-normal disabled:opacity-50"
          style={{ fontFamily: FONT }}
        >
          {printing ? (
            <>
              <div className="w-5 h-5 flex items-center justify-center">
                <i className="ri-loader-4-line text-base animate-spin"></i>
              </div>
              印刷中…
            </>
          ) : (
            <>
              <div className="w-5 h-5 flex items-center justify-center">
                <i className="ri-printer-line text-base"></i>
              </div>
              印刷
            </>
          )}
        </button>
        <button
          onClick={() => navigate('/')}
          className="w-full h-12 border border-[#6DAE72] rounded-lg text-[#6DAE72] text-base bg-white flex items-center justify-center gap-2 transition-all hover:border-2 active:border-2 font-normal"
          style={{ fontFamily: FONT }}
        >
          <div className="w-5 h-5 flex items-center justify-center">
            <i className="ri-home-5-line text-base"></i>
          </div>
          完了 — ホームに戻る
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
