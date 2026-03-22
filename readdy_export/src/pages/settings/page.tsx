import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface DeviceConfig {
  id: string;
  tag: string | null;
}

interface PeripheralStatus {
  name: string;
  connected: boolean;
  detail?: string;
}

const deviceConfigs: DeviceConfig[] = [
  { id: 'A', tag: 'AA:BB:CC:DD:EE:01' },
  { id: 'B', tag: 'AA:BB:CC:DD:EE:02' },
  { id: 'C', tag: null },
  { id: 'D', tag: null },
];

export default function SettingsPage() {
  const navigate = useNavigate();

  const [peripherals, setPeripherals] = useState<PeripheralStatus[]>([
    { name: 'Decent Scale', connected: true },
    { name: 'Star SM-S210i', connected: false },
  ]);
  const [espIp] = useState<string | null>('192.168.4.1');
  const [licensesOpen, setLicensesOpen] = useState(false);
  const [exportToast, setExportToast] = useState(false);

  const handleConnect = (index: number) => {
    setPeripherals((prev) =>
      prev.map((p, i) =>
        i === index ? { ...p, connected: !p.connected } : p
      )
    );
  };

  const handleExport = () => {
    setExportToast(true);
    setTimeout(() => setExportToast(false), 2500);
  };

  return (
    <div className="flex flex-col bg-[#F7F7F7]" style={{ width: '375px', minHeight: '812px' }}>
      {/* Header */}
      <header
        className="fixed top-0 bg-white border-b border-[#D4D4D4] z-10"
        style={{ width: '375px' }}
      >
        <div className="flex items-center px-4 h-14">
          <h1
            className="text-base font-normal text-[#1A1A1C]"
            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
          >
            設定
          </h1>
        </div>
      </header>

      {/* Main Content */}
      <main
        className="flex-1 overflow-y-auto"
        style={{ paddingTop: '56px', paddingBottom: '72px' }}
      >
        <div className="px-4 py-5 flex flex-col gap-6">

          {/* ── SECTION 1: DEVICES ── */}
          <section>
            <p
              className="text-xs text-[#6B6B6B] tracking-widest mb-2 px-1"
              style={{ fontFamily: 'Noto Sans JP, sans-serif', letterSpacing: '0.1em' }}
            >
              デバイス
            </p>
            <div className="bg-white border border-[#E0E0E0] rounded-xl overflow-hidden">
              {deviceConfigs.map((device, idx) => (
                <div key={device.id}>
                  <div className="flex items-center px-4 py-3">
                    {/* Device label */}
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                      <span
                        className="inline-flex items-center justify-center w-6 h-6 text-xs border border-[#6DAE72] text-[#6DAE72] rounded flex-shrink-0"
                        style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                      >
                        {device.id}
                      </span>
                      <div className="flex flex-col min-w-0">
                        <span
                          className="text-sm text-[#1A1A1C]"
                          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                        >
                          デバイス {device.id}
                        </span>
                        {device.tag ? (
                          <span
                            className="text-xs text-[#6B6B6B] truncate"
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                          >
                            タグ: {device.tag}
                          </span>
                        ) : (
                          <span
                            className="text-xs text-[#ABABAB]"
                            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                          >
                            未割り当て
                          </span>
                        )}
                      </div>
                    </div>
                    {/* Chevron */}
                    <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                      <i className="ri-arrow-right-s-line text-lg"></i>
                    </div>
                  </div>
                  {idx < deviceConfigs.length - 1 && (
                    <div className="h-px bg-[#F0F0F0] mx-4" />
                  )}
                </div>
              ))}
            </div>
          </section>

          {/* ── SECTION 2: PERIPHERALS ── */}
          <section>
            <p
              className="text-xs text-[#6B6B6B] tracking-widest mb-2 px-1"
              style={{ fontFamily: 'Noto Sans JP, sans-serif', letterSpacing: '0.1em' }}
            >
              周辺機器
            </p>
            <div className="bg-white border border-[#E0E0E0] rounded-xl overflow-hidden">
              {/* Scale */}
              <div className="flex items-center px-4 py-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-5 h-5 flex items-center justify-center text-[#6B6B6B] flex-shrink-0">
                    <i className="ri-scales-3-line text-base"></i>
                  </div>
                  <div className="flex flex-col min-w-0">
                    <span
                      className="text-sm text-[#1A1A1C]"
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      {peripherals[0].name}
                    </span>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span
                        className="w-2 h-2 rounded-full flex-shrink-0"
                        style={{
                          backgroundColor: peripherals[0].connected ? '#6DAE72' : '#C0C0C0',
                        }}
                      />
                      <span
                        className="text-xs"
                        style={{
                          fontFamily: 'Noto Sans JP, sans-serif',
                          color: peripherals[0].connected ? '#6DAE72' : '#ABABAB',
                        }}
                      >
                        {peripherals[0].connected ? '接続済み' : '未接続'}
                      </span>
                      {!peripherals[0].connected && (
                        <button
                          onClick={() => handleConnect(0)}
                          className="text-xs text-[#5B8BD4] ml-1"
                          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                        >
                          接続する
                        </button>
                      )}
                    </div>
                  </div>
                </div>
                <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                  <i className="ri-arrow-right-s-line text-lg"></i>
                </div>
              </div>

              <div className="h-px bg-[#F0F0F0] mx-4" />

              {/* Printer */}
              <div className="flex items-center px-4 py-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-5 h-5 flex items-center justify-center text-[#6B6B6B] flex-shrink-0">
                    <i className="ri-printer-line text-base"></i>
                  </div>
                  <div className="flex flex-col min-w-0">
                    <span
                      className="text-sm text-[#1A1A1C]"
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      {peripherals[1].name}
                    </span>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span
                        className="w-2 h-2 rounded-full flex-shrink-0"
                        style={{
                          backgroundColor: peripherals[1].connected ? '#6DAE72' : '#C0C0C0',
                        }}
                      />
                      <span
                        className="text-xs"
                        style={{
                          fontFamily: 'Noto Sans JP, sans-serif',
                          color: peripherals[1].connected ? '#6DAE72' : '#ABABAB',
                        }}
                      >
                        {peripherals[1].connected ? '接続済み' : '未接続'}
                      </span>
                      {!peripherals[1].connected && (
                        <button
                          onClick={() => handleConnect(1)}
                          className="text-xs text-[#5B8BD4] ml-1"
                          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                        >
                          接続する
                        </button>
                      )}
                    </div>
                  </div>
                </div>
                <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                  <i className="ri-arrow-right-s-line text-lg"></i>
                </div>
              </div>

              <div className="h-px bg-[#F0F0F0] mx-4" />

              {/* ESP32 Access Point */}
              <div className="flex items-center px-4 py-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-5 h-5 flex items-center justify-center text-[#6B6B6B] flex-shrink-0">
                    <i className="ri-wifi-line text-base"></i>
                  </div>
                  <div className="flex flex-col min-w-0">
                    <span
                      className="text-sm text-[#1A1A1C]"
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      ESP32 アクセスポイント
                    </span>
                    {espIp ? (
                      <span
                        className="text-xs text-[#6B6B6B]"
                        style={{ fontFamily: 'monospace', fontSize: '12px' }}
                      >
                        {espIp}
                      </span>
                    ) : (
                      <span
                        className="text-xs text-[#ABABAB]"
                        style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                      >
                        未設定
                      </span>
                    )}
                  </div>
                </div>
                <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                  <i className="ri-arrow-right-s-line text-lg"></i>
                </div>
              </div>
            </div>
          </section>

          {/* ── SECTION 3: DATA ── */}
          <section>
            <p
              className="text-xs text-[#6B6B6B] tracking-widest mb-2 px-1"
              style={{ fontFamily: 'Noto Sans JP, sans-serif', letterSpacing: '0.1em' }}
            >
              データ
            </p>
            <div className="bg-white border border-[#E0E0E0] rounded-xl overflow-hidden">
              {/* Export Data */}
              <button
                onClick={handleExport}
                className="w-full flex items-center px-4 py-3 hover:bg-[#F7F7F7] transition-colors"
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-5 h-5 flex items-center justify-center text-[#5B8BD4] flex-shrink-0">
                    <i className="ri-download-line text-base"></i>
                  </div>
                  <span
                    className="text-sm text-[#1A1A1C]"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    データをエクスポート
                  </span>
                </div>
                <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                  <i className="ri-arrow-right-s-line text-lg"></i>
                </div>
              </button>

              <div className="h-px bg-[#F0F0F0] mx-4" />

              {/* Cloud Sync */}
              <div className="flex items-center px-4 py-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-5 h-5 flex items-center justify-center text-[#C0C0C0] flex-shrink-0">
                    <i className="ri-cloud-line text-base"></i>
                  </div>
                  <span
                    className="text-sm text-[#1A1A1C]"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    クラウド同期
                  </span>
                  <span
                    className="ml-2 px-2 py-0.5 text-[10px] border border-[#C0C0C0] text-[#ABABAB] rounded"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    近日公開
                  </span>
                </div>
              </div>
            </div>
          </section>

          {/* ── SECTION 4: ABOUT ── */}
          <section>
            <p
              className="text-xs text-[#6B6B6B] tracking-widest mb-2 px-1"
              style={{ fontFamily: 'Noto Sans JP, sans-serif', letterSpacing: '0.1em' }}
            >
              このアプリについて
            </p>
            <div className="bg-white border border-[#E0E0E0] rounded-xl overflow-hidden">
              {/* Version */}
              <div className="flex items-center px-4 py-3">
                <span
                  className="text-sm text-[#1A1A1C] flex-1"
                  style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                >
                  バージョン
                </span>
                <span
                  className="text-sm text-[#6B6B6B]"
                  style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                >
                  1.0.0
                </span>
              </div>

              <div className="h-px bg-[#F0F0F0] mx-4" />

              {/* Open Source Licenses */}
              <button
                onClick={() => setLicensesOpen(true)}
                className="w-full flex items-center px-4 py-3 hover:bg-[#F7F7F7] transition-colors"
              >
                <span
                  className="text-sm text-[#5B8BD4] flex-1 text-left"
                  style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                >
                  オープンソースライセンス
                </span>
                <div className="w-5 h-5 flex items-center justify-center text-[#5B8BD4] flex-shrink-0">
                  <i className="ri-arrow-right-s-line text-lg"></i>
                </div>
              </button>
            </div>
          </section>

        </div>
      </main>

      {/* Bottom Navigation */}
      <nav
        className="fixed bottom-0 bg-white border-t border-[#D4D4D4] z-10"
        style={{ width: '375px' }}
      >
        <div className="grid grid-cols-4 h-16">
          {/* Home */}
          <button
            onClick={() => navigate('/')}
            className="relative flex flex-col items-center justify-center gap-1 text-[#6B6B6B] transition-colors"
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-home-5-line text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              ホーム
            </span>
          </button>

          {/* History */}
          <button
            onClick={() => navigate('/history')}
            className="relative flex flex-col items-center justify-center gap-1 text-[#6B6B6B] transition-colors"
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-history-line text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              履歴
            </span>
          </button>

          {/* Statistics */}
          <button
            onClick={() => navigate('/statistics')}
            className="relative flex flex-col items-center justify-center gap-1 text-[#6B6B6B] transition-colors"
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-bar-chart-2-line text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              統計
            </span>
          </button>

          {/* Settings — active */}
          <button className="relative flex flex-col items-center justify-center gap-1 text-[#5B8BD4] transition-colors">
            <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#5B8BD4]" />
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-settings-3-fill text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              設定
            </span>
          </button>
        </div>
      </nav>

      {/* Export toast */}
      {exportToast && (
        <div
          className="fixed bottom-20 left-1/2 -translate-x-1/2 bg-[#1A1A1C]/80 text-white text-sm px-5 py-2.5 rounded-full z-50 whitespace-nowrap"
          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
        >
          データをエクスポートしました
        </div>
      )}

      {/* Licenses modal */}
      {licensesOpen && (
        <div className="fixed inset-0 z-50 flex flex-col bg-white" style={{ width: '375px' }}>
          <header className="flex items-center px-4 h-14 border-b border-[#D4D4D4]">
            <button
              onClick={() => setLicensesOpen(false)}
              className="w-9 h-9 flex items-center justify-center text-[#1A1A1C] -ml-2 mr-2"
            >
              <i className="ri-arrow-left-line text-xl"></i>
            </button>
            <h2
              className="text-base font-normal text-[#1A1A1C]"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              オープンソースライセンス
            </h2>
          </header>
          <div className="flex-1 overflow-y-auto px-4 py-5 flex flex-col gap-4">
            {[
              { name: 'React', version: '19.0.0', license: 'MIT' },
              { name: 'Vite', version: '6.0.0', license: 'MIT' },
              { name: 'Tailwind CSS', version: '3.4.0', license: 'MIT' },
              { name: 'react-router-dom', version: '7.0.0', license: 'MIT' },
              { name: 'Remix Icon', version: '4.0.0', license: 'Apache 2.0' },
              { name: 'i18next', version: '23.0.0', license: 'MIT' },
            ].map((lib) => (
              <div key={lib.name} className="border-b border-[#F0F0F0] pb-4">
                <div className="flex items-center justify-between mb-0.5">
                  <span
                    className="text-sm text-[#1A1A1C]"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    {lib.name}
                  </span>
                  <span
                    className="text-xs text-[#6B6B6B]"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    v{lib.version}
                  </span>
                </div>
                <span
                  className="text-xs text-[#ABABAB]"
                  style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                >
                  {lib.license} ライセンス
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
