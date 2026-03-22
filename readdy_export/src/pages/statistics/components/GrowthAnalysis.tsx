import { useState, useRef, useEffect, useLayoutEffect } from 'react';
import {
  harvestRecords,
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

interface PlotSize { w: number; h: number }

function linReg(xs: number[], ys: number[]) {
  const n = xs.length;
  if (n < 2) return null;
  const meanX = xs.reduce((a, b) => a + b, 0) / n;
  const meanY = ys.reduce((a, b) => a + b, 0) / n;
  const num = xs.reduce((s, x, i) => s + (x - meanX) * (ys[i] - meanY), 0);
  const den = xs.reduce((s, x) => s + (x - meanX) ** 2, 0);
  if (den === 0) return null;
  const m = num / den;
  const b = meanY - m * meanX;
  return { m, b };
}

const PAD = { left: 40, right: 10, top: 10, bottom: 28 };

export default function GrowthAnalysis() {
  const [selectedVariety, setSelectedVariety] = useState<'全品種' | VarietyKey>('全品種');
  const [filterOpen, setFilterOpen] = useState(false);
  const [tooltip, setTooltip] = useState<{ x: number; y: number; record: typeof harvestRecords[0] } | null>(null);
  const filterRef = useRef<HTMLDivElement>(null);
  const plotRef = useRef<HTMLDivElement>(null);
  const [plotSize, setPlotSize] = useState<PlotSize>({ w: 260, h: 200 });

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (filterRef.current && !filterRef.current.contains(e.target as Node)) {
        setFilterOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  useLayoutEffect(() => {
    if (plotRef.current) {
      const { width, height } = plotRef.current.getBoundingClientRect();
      setPlotSize({ w: width, h: height });
    }
  }, []);

  const records =
    selectedVariety === '全品種'
      ? harvestRecords
      : harvestRecords.filter((r) => r.variety === selectedVariety);

  const allDays = records.map((r) => r.days);
  const allWeights = records.map((r) => r.weight);

  const minDays = Math.max(Math.min(...allDays) - 3, 0);
  const maxDays = Math.max(...allDays) + 3;
  const minWeight = Math.max(Math.min(...allWeights) - 20, 0);
  const maxWeight = Math.max(...allWeights) + 20;

  const plotW = plotSize.w - PAD.left - PAD.right;
  const plotH = plotSize.h - PAD.top - PAD.bottom;

  const toX = (days: number) =>
    PAD.left + ((days - minDays) / (maxDays - minDays)) * plotW;
  const toY = (weight: number) =>
    PAD.top + (1 - (weight - minWeight) / (maxWeight - minWeight)) * plotH;

  // Trend line for single variety
  const reg =
    selectedVariety !== '全品種'
      ? linReg(records.map((r) => r.days), records.map((r) => r.weight))
      : null;

  let trendLine: { x1: number; y1: number; x2: number; y2: number } | null = null;
  if (reg) {
    const { m, b } = reg;
    const x1px = PAD.left;
    const x2px = PAD.left + plotW;
    const dayAt = (px: number) => minDays + ((px - PAD.left) / plotW) * (maxDays - minDays);
    const y1px = toY(m * dayAt(x1px) + b);
    const y2px = toY(m * dayAt(x2px) + b);
    trendLine = { x1: x1px, y1: y1px, x2: x2px, y2: y2px };
  }

  const trendAngle = trendLine
    ? (Math.atan2(trendLine.y2 - trendLine.y1, trendLine.x2 - trendLine.x1) * 180) / Math.PI
    : 0;
  const trendLen = trendLine
    ? Math.sqrt((trendLine.x2 - trendLine.x1) ** 2 + (trendLine.y2 - trendLine.y1) ** 2)
    : 0;

  return (
    <div className="flex flex-col">
      {/* Header card */}
      <div className="bg-white border border-[#E8E8E8] rounded-xl mx-4 mt-4 px-4 py-3 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p
              className="text-xs text-[#6B6B6B] mb-0.5"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              栽培日数 × 収穫量 の相関
            </p>
            <p
              className="text-sm text-[#1A1A1C]"
              style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
            >
              <span className="font-semibold">{records.length}</span> 件の収穫データ
            </p>
          </div>

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
                      onClick={() => { setSelectedVariety(v); setFilterOpen(false); setTooltip(null); }}
                      className={`w-full text-left px-4 py-2.5 text-xs transition-colors ${
                        selectedVariety === v
                          ? 'text-[#6DAE72] bg-[#6DAE72]/5'
                          : 'text-[#1A1A1C] hover:bg-[#F7F7F7]'
                      }`}
                      style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                    >
                      {v}
                    </button>
                    {i < VARIETIES.length - 1 && <div className="h-px bg-[#F0F0F0] mx-3" />}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Scatter plot card */}
      <div className="bg-white border border-[#E8E8E8] rounded-xl mx-4 mt-3 px-4 pt-4 pb-3 shadow-sm">
        {/* Plot container */}
        <div
          ref={plotRef}
          className="relative w-full"
          style={{ height: '220px' }}
        >
          {/* Y-axis label */}
          <div
            className="absolute top-0 left-0 flex flex-col justify-between pointer-events-none"
            style={{ height: `${plotSize.h - PAD.bottom}px`, width: `${PAD.left}px`, paddingTop: `${PAD.top}px` }}
          >
            {[maxWeight, Math.round((maxWeight + minWeight) / 2), minWeight].map((v) => (
              <span
                key={v}
                className="text-[9px] text-[#ABABAB] leading-none text-right pr-2"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {v}g
              </span>
            ))}
          </div>

          {/* X-axis label */}
          <div
            className="absolute bottom-0 flex justify-between"
            style={{ left: `${PAD.left}px`, right: `${PAD.right}px`, height: `${PAD.bottom}px`, alignItems: 'flex-end' }}
          >
            {[minDays, Math.round((minDays + maxDays) / 2), maxDays].map((v) => (
              <span
                key={v}
                className="text-[9px] text-[#ABABAB] leading-none"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {v}日
              </span>
            ))}
          </div>

          {/* Grid lines */}
          <div
            className="absolute"
            style={{
              left: `${PAD.left}px`,
              right: `${PAD.right}px`,
              top: `${PAD.top}px`,
              bottom: `${PAD.bottom}px`,
              borderLeft: '1px solid #F0F0F0',
              borderBottom: '1px solid #F0F0F0',
            }}
          >
            {/* Horizontal grid lines */}
            {[0, 0.5, 1].map((t) => (
              <div
                key={t}
                className="absolute w-full"
                style={{
                  top: `${t * 100}%`,
                  borderTop: '1px dashed #F0F0F0',
                }}
              />
            ))}
            {/* Vertical grid lines */}
            {[0, 0.5, 1].map((t) => (
              <div
                key={t}
                className="absolute h-full"
                style={{
                  left: `${t * 100}%`,
                  borderLeft: '1px dashed #F0F0F0',
                }}
              />
            ))}
          </div>

          {/* Trend line */}
          {trendLine && trendLen > 0 && (
            <div
              className="absolute pointer-events-none"
              style={{
                left: `${trendLine.x1}px`,
                top: `${trendLine.y1}px`,
                width: `${trendLen}px`,
                height: '2px',
                backgroundColor: selectedVariety !== '全品種'
                  ? varietyColors[selectedVariety as VarietyKey]
                  : '#6DAE72',
                opacity: 0.45,
                transformOrigin: '0 50%',
                transform: `rotate(${trendAngle}deg)`,
              }}
            />
          )}

          {/* Dots */}
          {records.map((r) => {
            const cx = toX(r.days);
            const cy = toY(r.weight);
            if (isNaN(cx) || isNaN(cy)) return null;
            return (
              <button
                key={r.id}
                onClick={(e) => {
                  const rect = plotRef.current!.getBoundingClientRect();
                  setTooltip(
                    tooltip?.record.id === r.id
                      ? null
                      : { x: cx, y: cy, record: r }
                  );
                  e.stopPropagation();
                }}
                className="absolute w-3 h-3 rounded-full border-2 border-white shadow-sm transition-transform hover:scale-125 active:scale-125"
                style={{
                  left: `${cx - 6}px`,
                  top: `${cy - 6}px`,
                  backgroundColor: varietyColors[r.variety],
                }}
                aria-label={`${r.variety} ${r.days}日 ${r.weight}g`}
              />
            );
          })}

          {/* Tooltip */}
          {tooltip && (
            <div
              className="absolute z-20 bg-[#1A1A1C]/90 text-white rounded-lg px-2.5 py-2 shadow-lg pointer-events-none"
              style={{
                left: `${Math.min(tooltip.x + 8, plotSize.w - 120)}px`,
                top: `${Math.max(tooltip.y - 56, 4)}px`,
                minWidth: '100px',
              }}
            >
              <p
                className="text-[10px] font-medium leading-tight mb-0.5"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {tooltip.record.variety}
              </p>
              <p
                className="text-[10px] text-white/70 leading-tight"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {tooltip.record.days}日間 → {tooltip.record.weight}g
              </p>
              <p
                className="text-[9px] text-white/50 leading-tight mt-0.5"
                style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
              >
                {tooltip.record.harvestedAt}
              </p>
            </div>
          )}
        </div>

        {/* Axis labels */}
        <div className="flex justify-between mt-1" style={{ paddingLeft: `${PAD.left}px` }}>
          <span className="text-[9px] text-[#ABABAB]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
            ← 栽培日数（日）
          </span>
        </div>
        <div className="flex justify-end mt-0.5">
          <span className="text-[9px] text-[#ABABAB]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
            収穫量（g） ↑
          </span>
        </div>

        {selectedVariety !== '全品種' && reg && (
          <p
            className="text-[10px] text-[#ABABAB] mt-1 text-center"
            style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
          >
            トレンドライン表示中（線形回帰）
          </p>
        )}
      </div>

      {/* Legend */}
      <div className="bg-white border border-[#E8E8E8] rounded-xl mx-4 mt-3 px-4 py-3 shadow-sm">
        <p
          className="text-xs text-[#6B6B6B] mb-2"
          style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
        >
          凡例
        </p>
        <div className="flex flex-wrap gap-x-4 gap-y-2">
          {(Object.entries(varietyColors) as [VarietyKey, string][]).map(([v, c]) => (
            <button
              key={v}
              onClick={() => setSelectedVariety(selectedVariety === v ? '全品種' : v)}
              className="flex items-center gap-1.5"
            >
              <div
                className="w-2.5 h-2.5 rounded-full border-2 border-white shadow-sm flex-shrink-0"
                style={{
                  backgroundColor: c,
                  opacity: selectedVariety === '全品種' || selectedVariety === v ? 1 : 0.3,
                }}
              />
              <span
                className="text-xs"
                style={{
                  fontFamily: 'Noto Sans JP, sans-serif',
                  color: selectedVariety === '全品種' || selectedVariety === v ? '#1A1A1C' : '#ABABAB',
                }}
              >
                {v}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
