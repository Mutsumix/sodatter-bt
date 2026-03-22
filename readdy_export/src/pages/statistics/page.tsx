import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import HarvestTrends from './components/HarvestTrends';
import GrowthAnalysis from './components/GrowthAnalysis';

type TabKey = 'trends' | 'analysis';

const TABS: { key: TabKey; label: string }[] = [
  { key: 'trends', label: '収穫推移' },
  { key: 'analysis', label: '生育分析' },
];

export default function StatisticsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<TabKey>('trends');

  return (
    <div className="flex flex-col bg-[#F7F8FA]" style={{ width: '375px', minHeight: '812px' }}>
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
            統計
          </h1>
        </div>

        {/* Tab switcher */}
        <div className="flex border-b border-[#F0F0F0]">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className="relative flex-1 flex items-center justify-center h-10 transition-colors"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              <span
                className={`text-sm transition-colors ${
                  activeTab === tab.key
                    ? 'text-[#5B8BD4] font-medium'
                    : 'text-[#6B6B6B]'
                }`}
              >
                {tab.label}
              </span>
              {activeTab === tab.key && (
                <div className="absolute bottom-0 left-4 right-4 h-0.5 bg-[#5B8BD4] rounded-full" />
              )}
            </button>
          ))}
        </div>
      </header>

      {/* Main Content */}
      <main
        className="flex-1 overflow-y-auto"
        style={{ paddingTop: '96px', paddingBottom: '72px' }}
      >
        {activeTab === 'trends' && <HarvestTrends />}
        {activeTab === 'analysis' && <GrowthAnalysis />}
        <div className="h-4" />
      </main>

      {/* Bottom Navigation — 4 tabs */}
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

          {/* Statistics — active */}
          <button className="relative flex flex-col items-center justify-center gap-1 text-[#5B8BD4]">
            <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#5B8BD4]" />
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-bar-chart-2-fill text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              統計
            </span>
          </button>

          {/* Settings */}
          <button
            onClick={() => navigate('/settings')}
            className="relative flex flex-col items-center justify-center gap-1 text-[#6B6B6B] transition-colors"
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-settings-3-line text-xl"></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              設定
            </span>
          </button>
        </div>
      </nav>
    </div>
  );
}
