import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface HarvestRecord {
  id: string;
  variety: string;
  manufacturer: string;
  device: string;
  seedingDate: string;
  harvestDate: string;
  days: number;
  weight: number;
  imageUrl: string;
}

const mockRecords: HarvestRecord[] = [
  {
    id: '1',
    variety: 'Sunny Lettuce',
    manufacturer: 'Takii Seed',
    device: 'A',
    seedingDate: '2026/01/04',
    harvestDate: '2026/02/15',
    days: 42,
    weight: 185,
    imageUrl: 'https://readdy.ai/api/search-image?query=Fresh%20green%20lettuce%20head%20with%20crisp%20leaves%2C%20soft%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture%2C%20vibrant%20green%20color&width=48&height=48&seq=hist-lettuce-1&orientation=squarish',
  },
  {
    id: '2',
    variety: 'Mini Tomato',
    manufacturer: 'Sakata Seed',
    device: 'B',
    seedingDate: '2026/01/10',
    harvestDate: '2026/02/20',
    days: 41,
    weight: 142,
    imageUrl: 'https://readdy.ai/api/search-image?query=Small%20cherry%20tomato%20cluster%20with%20bright%20red%20color%2C%20soft%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=48&height=48&seq=hist-tomato-1&orientation=squarish',
  },
  {
    id: '3',
    variety: 'Basil',
    manufacturer: 'Tokita Seed',
    device: 'C',
    seedingDate: '2026/01/18',
    harvestDate: '2026/02/28',
    days: 41,
    weight: 98,
    imageUrl: 'https://readdy.ai/api/search-image?query=Fresh%20basil%20herb%20bunch%20with%20vibrant%20green%20leaves%2C%20soft%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=48&height=48&seq=hist-basil-1&orientation=squarish',
  },
  {
    id: '4',
    variety: 'Spinach',
    manufacturer: 'Takii Seed',
    device: 'D',
    seedingDate: '2025/12/20',
    harvestDate: '2026/01/28',
    days: 39,
    weight: 210,
    imageUrl: 'https://readdy.ai/api/search-image?query=Fresh%20spinach%20leaves%20bunch%20with%20deep%20green%20color%2C%20soft%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=48&height=48&seq=hist-spinach-1&orientation=squarish',
  },
  {
    id: '5',
    variety: 'Radish',
    manufacturer: 'Sakata Seed',
    device: 'A',
    seedingDate: '2025/12/05',
    harvestDate: '2026/01/12',
    days: 38,
    weight: 165,
    imageUrl: 'https://readdy.ai/api/search-image?query=Fresh%20red%20radish%20with%20green%20tops%2C%20soft%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=48&height=48&seq=hist-radish-1&orientation=squarish',
  },
];

const groupByMonth = (records: HarvestRecord[]) => {
  const groups: Record<string, HarvestRecord[]> = {};
  records.forEach((r) => {
    const [year, month] = r.harvestDate.split('/');
    const key = `${year}/${month}`;
    if (!groups[key]) groups[key] = [];
    groups[key].push(r);
  });
  return groups;
};

const formatMonthLabel = (key: string) => {
  const [year, month] = key.split('/');
  const date = new Date(Number(year), Number(month) - 1, 1);
  return `${year}年${date.toLocaleString('ja-JP', { month: 'long' })}`;
};

const formatDateRange = (seeding: string, harvest: string, days: number) => {
  const s = seeding.slice(5).replace('/', '/');
  const h = harvest.slice(5).replace('/', '/');
  return `${s} → ${h}（${days}日間）`;
};

export default function HistoryPage() {
  const navigate = useNavigate();
  const [filterOpen, setFilterOpen] = useState(false);
  const [activeFilter, setActiveFilter] = useState<string | null>(null);

  const filtered = activeFilter
    ? mockRecords.filter((r) => r.device === activeFilter)
    : mockRecords;

  const grouped = groupByMonth(filtered);
  const sortedMonths = Object.keys(grouped).sort((a, b) => (a > b ? -1 : 1));

  return (
    <div className="flex flex-col bg-white" style={{ width: '375px', minHeight: '812px' }}>
      {/* Header */}
      <header
        className="fixed top-0 bg-white border-b border-[#D4D4D4] z-10"
        style={{ width: '375px' }}
      >
        <div className="flex items-center justify-between px-4 h-14">
          <h1
            className="text-base font-normal text-[#1A1A1C]"
            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
          >
            履歴
          </h1>
          <button
            onClick={() => setFilterOpen((v) => !v)}
            className="w-9 h-9 flex items-center justify-center border border-[#D4D4D4] rounded text-[#6B6B6B] hover:border-[#6B6B6B] transition-colors"
            aria-label="Filter"
          >
            <i className="ri-filter-3-line text-lg"></i>
          </button>
        </div>

        {/* Filter chips dropdown */}
        {filterOpen && (
          <div className="px-4 pb-3 flex items-center gap-2 border-t border-[#F0F0F0]">
            <span
              className="text-xs text-[#6B6B6B] mr-1"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              デバイス：
            </span>
            {['A', 'B', 'C', 'D'].map((d) => (
              <button
                key={d}
                onClick={() => setActiveFilter(activeFilter === d ? null : d)}
                className={`px-3 py-1 text-xs rounded border transition-colors ${
                  activeFilter === d
                    ? 'border-[#6DAE72] text-[#6DAE72] bg-[#6DAE72]/5'
                    : 'border-[#D4D4D4] text-[#6B6B6B]'
                }`}
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {d}
              </button>
            ))}
            {activeFilter && (
              <button
                onClick={() => setActiveFilter(null)}
                className="ml-auto text-xs text-[#5B8BD4]"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                クリア
              </button>
            )}
          </div>
        )}
      </header>

      {/* Main Content */}
      <main
        className="flex-1 overflow-y-auto"
        style={{ paddingTop: filterOpen ? '96px' : '56px', paddingBottom: '64px' }}
      >
        <div className="px-4 py-4">
          {sortedMonths.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-[#6B6B6B]">
              <i className="ri-inbox-line text-4xl mb-3"></i>
              <p className="text-sm" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
                収穫記録がありません。
              </p>
            </div>
          ) : (
            <>
              {sortedMonths.map((month) => (
                <div key={month} className="mb-6">
                  {/* Month header */}
                  <p
                    className="text-xs text-[#6B6B6B] tracking-widest mb-3"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif', letterSpacing: '0.08em' }}
                  >
                    {formatMonthLabel(month)}
                  </p>

                  {/* Cards */}
                  <div className="flex flex-col gap-3">
                    {grouped[month].map((record) => (
                      <div
                        key={record.id}
                        className="relative bg-white border border-[#E0E0E0] rounded-lg overflow-hidden flex items-center"
                        style={{ minHeight: '72px' }}
                      >
                        {/* Left green accent bar */}
                        <div
                          className="absolute left-0 top-0 bottom-0 w-[3px] bg-[#6DAE72]"
                        />

                        {/* Thumbnail */}
                        <div className="ml-4 mr-3 flex-shrink-0">
                          <div className="w-12 h-12 rounded overflow-hidden border border-[#E0E0E0]">
                            <img
                              src={record.imageUrl}
                              alt={record.variety}
                              className="w-full h-full object-cover object-top"
                            />
                          </div>
                        </div>

                        {/* Center info */}
                        <div className="flex-1 py-3 min-w-0">
                          <p
                            className="text-base text-[#1A1A1C] leading-tight mb-1 truncate"
                            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                          >
                            {record.variety}
                          </p>
                          <div className="flex items-center gap-2">
                            <span
                              className="inline-flex items-center justify-center w-5 h-5 text-[10px] border border-[#6DAE72] text-[#6DAE72] rounded flex-shrink-0"
                              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                            >
                              {record.device}
                            </span>
                            <span
                              className="text-xs text-[#6B6B6B] truncate"
                              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                            >
                              {formatDateRange(record.seedingDate, record.harvestDate, record.days)}
                            </span>
                          </div>
                        </div>

                        {/* Right weight */}
                        <div className="px-4 flex-shrink-0">
                          <span
                            className="text-base text-[#1A1A1C]"
                            style={{
                              fontFamily: 'Noto Sans JP, sans-serif',
                              fontWeight: 600,
                            }}
                          >
                            {record.weight}g
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}

              {/* Statistics teaser card */}
              <div
                className="border border-dashed border-[#C8C8C8] rounded-lg bg-[#F7F7F7] flex flex-col items-center justify-center py-6 gap-2 mt-2"
              >
                <div className="w-8 h-8 flex items-center justify-center text-[#ABABAB]">
                  <i className="ri-bar-chart-2-line text-2xl"></i>
                </div>
                <p
                  className="text-sm text-[#ABABAB]"
                  style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                >
                  統計 — 近日公開
                </p>
              </div>
            </>
          )}
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

          {/* History — active */}
          <button
            className="relative flex flex-col items-center justify-center gap-1 text-[#5B8BD4] transition-colors"
          >
            <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#5B8BD4]" />
            <div className="w-6 h-6 flex items-center justify-center">
              <i className="ri-history-fill text-xl"></i>
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
