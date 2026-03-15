import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

const DEVICES = [
  { id: 'A', inUse: true },
  { id: 'B', inUse: true },
  { id: 'C', inUse: false },
  { id: 'D', inUse: false },
];

export default function SeedingRegistration() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [selectedDevice, setSelectedDevice] = useState<string | null>(null);
  const [variety, setVariety] = useState('');
  const [manufacturer, setManufacturer] = useState('');
  const [seedingDate, setSeedingDate] = useState(() => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  });
  const [photoPreview, setPhotoPreview] = useState<string | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitted, setSubmitted] = useState(false);

  const dateInputRef = useRef<HTMLInputElement>(null);

  const handlePhotoClick = () => {
    fileInputRef.current?.click();
  };

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => {
        setPhotoPreview(ev.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!selectedDevice) newErrors.device = 'デバイスを選択してください';
    if (!variety.trim()) newErrors.variety = '品種名を入力してください';
    if (!seedingDate) newErrors.seedingDate = '播種日を入力してください';
    return newErrors;
  };

  const handleSubmit = () => {
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    setErrors({});
    setSubmitted(true);
  };

  const handleBack = () => {
    navigate('/');
  };

  const formatDateDisplay = (dateStr: string) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${y}年${m}月${d}日`;
  };

  if (submitted) {
    return (
      <div className="flex flex-col items-center justify-center bg-white" style={{ width: '375px', minHeight: '812px', fontFamily: FONT }}>
        <div className="flex flex-col items-center gap-6 px-8">
          <div className="w-16 h-16 flex items-center justify-center border border-[#6DAE72] rounded-full">
            <i className="ri-check-line text-3xl text-[#6DAE72]"></i>
          </div>
          <div className="text-center">
            <p className="text-lg text-[#1A1A1C] mb-2">登録が完了しました</p>
            <p className="text-sm text-[#6B6B6B]">デバイス {selectedDevice} に播種情報を登録しました</p>
          </div>
          <button
            onClick={handleBack}
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
    <div className="flex flex-col bg-white" style={{ width: '375px', minHeight: '812px', fontFamily: FONT }}>
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
        <h1 className="text-base font-normal text-[#1A1A1C]" style={{ fontFamily: FONT }}>
          New Seeding
        </h1>
      </header>

      {/* Scrollable Form */}
      <main className="flex-1 pt-14 pb-28 overflow-y-auto">
        <div className="px-4 py-6 flex flex-col gap-6">

          {/* 1. Device Selector */}
          <div className="flex flex-col gap-2">
            <label className="text-sm text-[#1A1A1C]" style={{ fontFamily: FONT }}>
              Device
            </label>
            <div className="flex flex-row gap-2">
              {DEVICES.map((device) => (
                <button
                  key={device.id}
                  disabled={device.inUse}
                  onClick={() => {
                    if (!device.inUse) {
                      setSelectedDevice(device.id);
                      setErrors((prev) => ({ ...prev, device: '' }));
                    }
                  }}
                  className={[
                    'flex flex-col items-center justify-center rounded-lg transition-all',
                    'h-14 flex-1',
                    device.inUse
                      ? 'border border-[#D4D4D4] text-[#B0B0B0] bg-white cursor-not-allowed'
                      : selectedDevice === device.id
                      ? 'border-2 border-[#6DAE72] text-[#6DAE72] bg-white'
                      : 'border border-[#6DAE72] text-[#6DAE72] bg-white hover:bg-[#6DAE72]/5',
                  ].join(' ')}
                  style={{ fontFamily: FONT }}
                >
                  <span className="text-base font-medium">{device.id}</span>
                  {device.inUse && (
                    <span className="text-[0.625rem] text-[#B0B0B0] leading-tight">In use</span>
                  )}
                </button>
              ))}
            </div>
            {errors.device && (
              <p className="text-xs text-[#EC0000]" style={{ fontFamily: FONT }}>{errors.device}</p>
            )}
          </div>

          {/* 2. Variety */}
          <div className="flex flex-col gap-2">
            <label className="text-sm text-[#1A1A1C]" htmlFor="variety" style={{ fontFamily: FONT }}>
              Variety
            </label>
            <input
              id="variety"
              type="text"
              value={variety}
              onChange={(e) => {
                setVariety(e.target.value);
                setErrors((prev) => ({ ...prev, variety: '' }));
              }}
              className={[
                'h-12 px-3 rounded-lg border bg-white text-base text-[#1A1A1C] outline-none transition-colors',
                'focus:border-[#5B8BD4]',
                errors.variety ? 'border-[#EC0000]' : 'border-[#D4D4D4]',
              ].join(' ')}
              style={{ fontFamily: FONT, fontSize: '16px' }}
              placeholder="例：ミニトマト"
            />
            {errors.variety && (
              <p className="text-xs text-[#EC0000]" style={{ fontFamily: FONT }}>{errors.variety}</p>
            )}
          </div>

          {/* 3. Manufacturer */}
          <div className="flex flex-col gap-2">
            <label className="text-sm text-[#1A1A1C]" htmlFor="manufacturer" style={{ fontFamily: FONT }}>
              Manufacturer
            </label>
            <input
              id="manufacturer"
              type="text"
              value={manufacturer}
              onChange={(e) => setManufacturer(e.target.value)}
              className="h-12 px-3 rounded-lg border border-[#D4D4D4] bg-white text-base text-[#1A1A1C] outline-none transition-colors focus:border-[#5B8BD4]"
              style={{ fontFamily: FONT, fontSize: '16px' }}
              placeholder="例：タキイ種苗"
            />
          </div>

          {/* 4. Seeding Date */}
          <div className="flex flex-col gap-2">
            <label className="text-sm text-[#1A1A1C]" htmlFor="seedingDate" style={{ fontFamily: FONT }}>
              Seeding Date
            </label>
            <div
              className={[
                'h-12 px-3 rounded-lg border bg-white flex items-center justify-between cursor-pointer transition-colors',
                errors.seedingDate ? 'border-[#EC0000]' : 'border-[#D4D4D4] hover:border-[#5B8BD4]',
              ].join(' ')}
              onClick={() => dateInputRef.current?.showPicker?.()}
            >
              <span
                className="text-base text-[#1A1A1C]"
                style={{ fontFamily: FONT }}
              >
                {seedingDate ? formatDateDisplay(seedingDate) : '日付を選択'}
              </span>
              <div className="w-6 h-6 flex items-center justify-center">
                <i className="ri-calendar-line text-lg text-[#5B8BD4]"></i>
              </div>
              <input
                ref={dateInputRef}
                id="seedingDate"
                type="date"
                value={seedingDate}
                onChange={(e) => {
                  setSeedingDate(e.target.value);
                  setErrors((prev) => ({ ...prev, seedingDate: '' }));
                }}
                className="absolute opacity-0 w-0 h-0 pointer-events-none"
                tabIndex={-1}
              />
            </div>
            {errors.seedingDate && (
              <p className="text-xs text-[#EC0000]" style={{ fontFamily: FONT }}>{errors.seedingDate}</p>
            )}
          </div>

          {/* 5. Seed Photo */}
          <div className="flex flex-col gap-2">
            <label className="text-sm text-[#1A1A1C]" style={{ fontFamily: FONT }}>
              Seed Photo
            </label>
            <button
              type="button"
              onClick={handlePhotoClick}
              className="w-full border border-dashed border-[#D4D4D4] rounded-lg bg-white flex flex-col items-center justify-center gap-2 hover:border-[#6B6B6B] transition-colors overflow-hidden"
              style={{ aspectRatio: '3/2' }}
            >
              {photoPreview ? (
                <img
                  src={photoPreview}
                  alt="Seed preview"
                  className="w-full h-full object-cover"
                />
              ) : (
                <>
                  <div className="w-10 h-10 flex items-center justify-center">
                    <i className="ri-camera-line text-3xl text-[#6B6B6B]"></i>
                  </div>
                  <span className="text-sm text-[#6B6B6B]" style={{ fontFamily: FONT }}>
                    Take a photo (optional)
                  </span>
                </>
              )}
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              capture="environment"
              className="hidden"
              onChange={handlePhotoChange}
            />
          </div>

        </div>
      </main>

      {/* Fixed Bottom Register Button */}
      <div
        className="fixed bottom-0 bg-white border-t border-[#D4D4D4] px-4 py-4 z-10"
        style={{ width: '375px' }}
      >
        <button
          onClick={handleSubmit}
          className="w-full h-12 border border-[#6DAE72] rounded-lg text-[#6DAE72] text-base bg-white transition-all hover:border-2 active:border-2 font-normal"
          style={{ fontFamily: FONT }}
        >
          Register
        </button>
      </div>
    </div>
  );
}
