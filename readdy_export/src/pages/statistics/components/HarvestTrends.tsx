import { useState, useRef, useEffect } from 'react';
import {
  monthlyData,
  dailyDataByMonth,
  varietyColors,
  type VarietyKey,
} from '../../../mocks/statistics';

const VARIETIES: ('全品種' | VarietyKey)[] = [
  '全品種',
  'サニーレタス',
  'ミニトマト',
  'バジル',
  'ほうれん草',
  'ラディッシュ',
];

const CHART_H = 150;
const BAR_MIN_H = 4;

export default function HarvestTrends() {
  const [selectedVariety, setSelectedVariety] = useState<'全品種' | VarietyKey>('全品種');
  const [filterOpen, setFilterOpen] = useState(false);
  const [drillMonth, setDrillMonth] = useState<string | null>(null);
  const filterRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (filterRef.current && !filterRef.current.contains(e.target as Node)) {
        setFilterOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Build monthly chart data
  const chartData = monthlyData.map((m) => {
    const value =
      selectedVariety === '全品種'
        ? m.total
        : (m.byVariety[selectedVariety] ?? 0);
    return { ...m, value };
  });

  const maxValue = Math.max(...chartData.map((d) => d.value), 1);
  const totalWeight = chartData.reduce((s, d) => s + d.value, 0);

  // Daily drilldown data
  const dailyEntries = drillMonth ? (dailyDataByMonth[drillMonth] ?? []) : [];
  const filteredDaily =
    selectedVariety === '全品種'
      ? dailyEntries
      : dailyEntries.filter((d) => d.variety === selectedVariety);
  const maxDailyValue = Math.max(...filteredDaily.map((d) => d.weight), 1);

  const barColor = (variety: VarietyKey | null) => {
    if (!variety) return '#6DAE72';
    return varietyColors[variety];
  };

  return (
    <div className="flex flex-col">
      {/* Summary row */}
      <div className="bg-white border border-[#E8E8E8] rounded-xl mx-4 mt-4 px-4 py-3 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p
              className="text-xs text-[#6B6B6B] mb-0.5"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              {drillMonth
                ? `${drillMonth.replace('/', '年')}月 合計`
                : '表示期間合計'}
            </p>
            <p
              className="text-2xl font-semibold text-[#1A1A1C]"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              {drillMonth
                ? `${filteredDaily.reduce((s, d) => s + d.weight, 0).toLocaleString()}g`
                : `${totalWeight.toLocaleString()}g`}
            </p>
          </div>

          {/* Variety filter */}
          <div className="relative" ref={filterRef}>
            <button
              onClick={() => setFilterOpen((v) => !v)}
              className="flex items-center gap-1.5 px-3 py-1.5 border border-[#D4D4D4] rounded-lg text-xs text-[#1A1A1C] hover:border-[#6B6B6B] transition-colors"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              <span>{selectedVariety}</span>
              <div className="w-3 h-3 flex items-center justify-center">
                <i className={`ri-arrow-${filterOpen ? 'up' : 'down'}-s-line text-sm text-[#6B6B6B]`}></i>
              </div>
            </button>
            {filterOpen && (
              <div className="absolute right-0 top-9 bg-white border border-[#E0E0E0] rounded-xl shadow-lg z-20 overflow-hidden min-w-[120px]">
                {VARIETIES.map((v, i) => (
                  <div key={v}>
                    <button
                      onClick={() => {
                        setSelectedVariety(v);
                        setFilterOpen(false);
                      }}
                      className={`w-full text-left px-4 py-2.5 text-xs transition-colors ${
                        selectedVariety === v
                          ? 'text-[#6DAE72] bg-[#6DAE72]/5'
                          : 'text-[#1A1A1C] hover:bg-[#F7F7F7]'
                      }`}
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      {v}
                    </button>
                    {i < VARIETIES.length - 1 && (
                      <div className="h-px bg-[#F0F0F0] mx-3" />
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Chart card */}
      <div className="bg-white border border-[#E8E8E8] rounded-xl mx-4 mt-3 px-4 pt-4 pb-3 shadow-sm">
        {/* Drilldown header */}
        {drillMonth && (
          <div className="flex items-center gap-2 mb-4">
            <button
              onClick={() => setDrillMonth(null)}
              className="w-7 h-7 flex items-center justify-center rounded border border-[#D4D4D4] text-[#6B6B6B] hover:border-[#6B6B6B] transition-colors"
            >
              <i className="ri-arrow-left-s-line text-lg"></i>
            </button>
            <span
              className="text-sm text-[#1A1A1C]"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              {drillMonth.replace('/', '年')}月 — 日別
            </span>
          </div>
        )}

        {/* Chart area */}
        <div className="flex">
          {/* Y-axis */}
          <div
            className="flex flex-col justify-between pr-2"
            style={{ height: `${CHART_H}px`, width: '36px' }}
          >
            {[maxValue, Math.round(maxValue / 2), 0].map((v) => (
              <span
                key={v}
                className="text-[10px] text-[#ABABAB] leading-none"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {v}g
              </span>
            ))}
          </div>

          {/* Bars */}
          <div className="flex-1 flex flex-col">
            {/* Grid lines */}
            <div
              className="relative flex-1 flex items-end"
              style={{ height: `${CHART_H}px` }}
            >
              {/* Horizontal grid lines */}
              <div className="absolute inset-0 flex flex-col justify-between pointer-events-none">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="w-full h-px bg-[#F0F0F0]" />
                ))}
              </div>

              {/* Bars wrapper */}
              <div className="relative z-10 w-full flex items-end gap-1 px-1" style={{ height: `${CHART_H}px` }}>
                {!drillMonth &&
                  chartData.map((d) => {
                    const h = Math.max(
                      (d.value / maxValue) * (CHART_H - 8),
                      d.value > 0 ? BAR_MIN_H : 0
                    );
                    return (
                      <button
                        key={d.monthKey}
                        onClick={() => setDrillMonth(d.monthKey)}
                        className="flex-1 flex flex-col items-center group"
                        style={{ height: `${CHART_H}px` }}
                        title={`${d.label}: ${d.value}g`}
                      >
                        <div className="flex-1 flex items-end w-full">
                          <div
                            className="w-full rounded-t transition-opacity group-hover:opacity-75"
                            style={{
                              height: `${h}px`,
                              backgroundColor:
                                selectedVariety === '全品種'
                                  ? '#6DAE72'
                                  : barColor(selectedVariety as VarietyKey),
                            }}
                          />
                        </div>
                        <span
                          className="text-[9px] text-[#6B6B6B] mt-1 leading-none"
                          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                        >
                          {d.label}
                        </span>
                      </button>
                    );
                  })}

                {drillMonth &&
                  (filteredDaily.length === 0 ? (
                    <div className="w-full h-full flex items-center justify-center">
                      <span
                        className="text-xs text-[#ABABAB]"
                        style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                      >
                        データなし
                      </span>
                    </div>
                  ) : (
                    filteredDaily.map((d) => {
                      const h = Math.max(
                        (d.weight / maxDailyValue) * (CHART_H - 8),
                        d.weight > 0 ? BAR_MIN_H : 0
                      );
                      return (
                        <div
                          key={d.day}
                          className="flex-1 flex flex-col items-center"
                          style={{ height: `${CHART_H}px` }}
                        >
                          <div className="flex-1 flex items-end w-full">
                            <div
                              className="w-full rounded-t"
                              style={{
                                height: `${h}px`,
                                backgroundColor: barColor(d.variety),
                              }}
                            />
                          </div>
                          <span
                            className="text-[8px] text-[#6B6B6B] mt-1 leading-none"
                            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                          >
                            {d.label}
                          </span>
                        </div>
                      );
                    })
                  ))}
              </div>
            </div>
          </div>
        </div>

        {!drillMonth && (
          <p
            className="text-[10px] text-[#ABABAB] mt-2 text-center"
            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
          >
            棒グラフをタップすると日別表示に切り替わります
          </p>
        )}
      </div>

      {/* Variety legend */}
      {selectedVariety === '全品種' && !drillMonth && (
        <div className="mx-4 mt-3 bg-white border border-[#E8E8E8] rounded-xl px-4 py-3 shadow-sm">
          <p
            className="text-xs text-[#6B6B6B] mb-2"
            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
          >
            品種別合計
          </p>
          <div className="flex flex-col gap-2">
            {(Object.keys(varietyColors) as VarietyKey[]).map((v) => {
              const total = monthlyData.reduce(
                (s, m) => s + (m.byVariety[v] ?? 0),
                0
              );
              const pct = Math.round((total / totalWeight) * 100);
              return (
                <div key={v} className="flex items-center gap-2">
                  <div
                    className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                    style={{ backgroundColor: varietyColors[v] }}
                  />
                  <span
                    className="text-xs text-[#1A1A1C] flex-1"
                    style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                  >
                    {v}
                  </span>
                  <div className="flex items-center gap-2">
                    <div className="w-20 h-1.5 bg-[#F0F0F0] rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{
                          width: `${pct}%`,
                          backgroundColor: varietyColors[v],
                        }}
                      />
                    </div>
                    <span
                      className="text-xs text-[#6B6B6B] w-12 text-right"
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      {total}g
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
