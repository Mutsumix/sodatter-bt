export type VarietyKey = 'サニーレタス' | 'バジル' | 'ミニトマト' | 'ほうれん草' | 'ラディッシュ';

export interface MonthlyEntry {
  monthKey: string;
  label: string;
  total: number;
  byVariety: Partial<Record<VarietyKey, number>>;
}

export interface DailyEntry {
  day: number;
  label: string;
  weight: number;
  variety: VarietyKey;
}

export interface HarvestRecord {
  id: string;
  variety: VarietyKey;
  days: number;
  weight: number;
  harvestedAt: string;
}

export const varietyColors: Record<VarietyKey, string> = {
  'サニーレタス': '#7FC97F',
  'バジル': '#A8D5A2',
  'ミニトマト': '#FF9F8C',
  'ほうれん草': '#4DAF7A',
  'ラディッシュ': '#F48FB1',
};

export const monthlyData: MonthlyEntry[] = [
  {
    monthKey: '2025/10',
    label: '10月',
    total: 460,
    byVariety: { 'サニーレタス': 340, 'バジル': 120 },
  },
  {
    monthKey: '2025/11',
    label: '11月',
    total: 585,
    byVariety: { 'サニーレタス': 280, 'ミニトマト': 195, 'バジル': 110 },
  },
  {
    monthKey: '2025/12',
    label: '12月',
    total: 375,
    byVariety: { 'ほうれん草': 210, 'ミニトマト': 165 },
  },
  {
    monthKey: '2026/01',
    label: '1月',
    total: 560,
    byVariety: { 'サニーレタス': 185, 'ほうれん草': 210, 'ラディッシュ': 165 },
  },
  {
    monthKey: '2026/02',
    label: '2月',
    total: 425,
    byVariety: { 'サニーレタス': 185, 'ミニトマト': 142, 'バジル': 98 },
  },
  {
    monthKey: '2026/03',
    label: '3月',
    total: 220,
    byVariety: { 'ミニトマト': 220 },
  },
];

export const dailyDataByMonth: Record<string, DailyEntry[]> = {
  '2026/01': [
    { day: 5, label: '1/5', weight: 55, variety: 'ほうれん草' },
    { day: 8, label: '1/8', weight: 48, variety: 'サニーレタス' },
    { day: 12, label: '1/12', weight: 165, variety: 'ラディッシュ' },
    { day: 15, label: '1/15', weight: 80, variety: 'ほうれん草' },
    { day: 19, label: '1/19', weight: 72, variety: 'サニーレタス' },
    { day: 22, label: '1/22', weight: 75, variety: 'ほうれん草' },
    { day: 28, label: '1/28', weight: 65, variety: 'サニーレタス' },
  ],
  '2026/02': [
    { day: 3, label: '2/3', weight: 62, variety: 'バジル' },
    { day: 7, label: '2/7', weight: 48, variety: 'ミニトマト' },
    { day: 11, label: '2/11', weight: 55, variety: 'サニーレタス' },
    { day: 15, label: '2/15', weight: 130, variety: 'サニーレタス' },
    { day: 20, label: '2/20', weight: 94, variety: 'ミニトマト' },
    { day: 24, label: '2/24', weight: 36, variety: 'バジル' },
    { day: 28, label: '2/28', weight: 0, variety: 'バジル' },
  ],
  '2026/03': [
    { day: 10, label: '3/10', weight: 95, variety: 'ミニトマト' },
    { day: 18, label: '3/18', weight: 125, variety: 'ミニトマト' },
  ],
  '2025/10': [
    { day: 4, label: '10/4', weight: 110, variety: 'サニーレタス' },
    { day: 10, label: '10/10', weight: 45, variety: 'バジル' },
    { day: 16, label: '10/16', weight: 130, variety: 'サニーレタス' },
    { day: 22, label: '10/22', weight: 75, variety: 'バジル' },
    { day: 28, label: '10/28', weight: 100, variety: 'サニーレタス' },
  ],
  '2025/11': [
    { day: 3, label: '11/3', weight: 98, variety: 'サニーレタス' },
    { day: 8, label: '11/8', weight: 55, variety: 'バジル' },
    { day: 14, label: '11/14', weight: 110, variety: 'ミニトマト' },
    { day: 19, label: '11/19', weight: 127, variety: 'サニーレタス' },
    { day: 24, label: '11/24', weight: 55, variety: 'バジル' },
    { day: 28, label: '11/28', weight: 85, variety: 'ミニトマト' },
    { day: 30, label: '11/30', weight: 55, variety: 'サニーレタス' },
  ],
  '2025/12': [
    { day: 5, label: '12/5', weight: 80, variety: 'ミニトマト' },
    { day: 12, label: '12/12', weight: 100, variety: 'ほうれん草' },
    { day: 18, label: '12/18', weight: 85, variety: 'ミニトマト' },
    { day: 25, label: '12/25', weight: 110, variety: 'ほうれん草' },
    { day: 30, label: '12/30', weight: 0, variety: 'ほうれん草' },
  ],
};

export const harvestRecords: HarvestRecord[] = [
  { id: '1', variety: 'サニーレタス', days: 38, weight: 180, harvestedAt: '2026/02/15' },
  { id: '2', variety: 'サニーレタス', days: 41, weight: 195, harvestedAt: '2026/01/28' },
  { id: '3', variety: 'サニーレタス', days: 35, weight: 165, harvestedAt: '2025/12/10' },
  { id: '4', variety: 'サニーレタス', days: 44, weight: 210, harvestedAt: '2025/11/22' },
  { id: '5', variety: 'サニーレタス', days: 37, weight: 178, harvestedAt: '2025/10/30' },
  { id: '6', variety: 'サニーレタス', days: 42, weight: 188, harvestedAt: '2026/03/05' },
  { id: '7', variety: 'ミニトマト', days: 55, weight: 142, harvestedAt: '2026/02/20' },
  { id: '8', variety: 'ミニトマト', days: 62, weight: 175, harvestedAt: '2026/01/15' },
  { id: '9', variety: 'ミニトマト', days: 58, weight: 155, harvestedAt: '2025/11/30' },
  { id: '10', variety: 'ミニトマト', days: 65, weight: 188, harvestedAt: '2025/10/18' },
  { id: '11', variety: 'ミニトマト', days: 50, weight: 128, harvestedAt: '2026/03/18' },
  { id: '12', variety: 'バジル', days: 28, weight: 95, harvestedAt: '2026/02/28' },
  { id: '13', variety: 'バジル', days: 32, weight: 112, harvestedAt: '2025/11/25' },
  { id: '14', variety: 'バジル', days: 25, weight: 82, harvestedAt: '2025/10/12' },
  { id: '15', variety: 'バジル', days: 35, weight: 125, harvestedAt: '2026/01/20' },
  { id: '16', variety: 'ほうれん草', days: 40, weight: 210, harvestedAt: '2026/01/28' },
  { id: '17', variety: 'ほうれん草', days: 36, weight: 188, harvestedAt: '2025/12/15' },
  { id: '18', variety: 'ほうれん草', days: 43, weight: 225, harvestedAt: '2026/02/10' },
  { id: '19', variety: 'ラディッシュ', days: 22, weight: 165, harvestedAt: '2026/01/12' },
  { id: '20', variety: 'ラディッシュ', days: 18, weight: 140, harvestedAt: '2025/12/28' },
  { id: '21', variety: 'ラディッシュ', days: 25, weight: 182, harvestedAt: '2026/03/02' },
];
