import { useNavigate, useParams } from 'react-router-dom';

const FONT = 'Noto Sans JP, sans-serif';

const MOCK_DEVICES: Record<string, {
  id: string;
  cropName: string;
  manufacturer: string;
  seedingDate: string;
  daysElapsed: number;
  imageUrl: string;
  growthLogs: { date: string; imageUrl: string }[];
}> = {
  A: {
    id: 'A',
    cropName: 'ミニトマト',
    manufacturer: 'タキイ種苗',
    seedingDate: '2026/01/04',
    daysElapsed: 32,
    imageUrl:
      'https://readdy.ai/api/search-image?query=Close-up%20photograph%20of%20a%20small%20cherry%20tomato%20plant%20seedling%20with%20green%20leaves%2C%20natural%20daylight%2C%20soft%20focus%20background%2C%20botanical%20photography%20style%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=160&height=160&seq=detail-a-tomato&orientation=squarish',
    growthLogs: [
      {
        date: '01/06',
        imageUrl:
          'https://readdy.ai/api/search-image?query=Tiny%20tomato%20seedling%20sprouting%20from%20soil%20in%20a%20small%20pot%2C%20top-down%20view%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20macro%20photography%2C%20high%20detail&width=128&height=128&seq=log-a-1&orientation=squarish',
      },
      {
        date: '01/12',
        imageUrl:
          'https://readdy.ai/api/search-image?query=Young%20tomato%20plant%20with%20two%20true%20leaves%20growing%20in%20a%20small%20container%2C%20natural%20light%2C%20clean%20background%2C%20botanical%20photography%2C%20high%20detail&width=128&height=128&seq=log-a-2&orientation=squarish',
      },
      {
        date: '01/20',
        imageUrl:
          'https://readdy.ai/api/search-image?query=Tomato%20seedling%20with%20several%20leaves%20growing%20vigorously%20in%20a%20pot%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20high%20detail&width=128&height=128&seq=log-a-3&orientation=squarish',
      },
      {
        date: '02/01',
        imageUrl:
          'https://readdy.ai/api/search-image?query=Healthy%20tomato%20plant%20with%20multiple%20leaves%20and%20a%20sturdy%20stem%20in%20a%20pot%2C%20natural%20daylight%2C%20clean%20white%20background%2C%20botanical%20photography%2C%20high%20detail&width=128&height=128&seq=log-a-4&orientation=squarish',
      },
    ],
  },
  B: {
    id: 'B',
    cropName: 'バジル',
    manufacturer: 'サカタのタネ',
    seedingDate: '2026/02/01',
    daysElapsed: 15,
    imageUrl:
      'https://readdy.ai/api/search-image?query=Close-up%20photograph%20of%20fresh%20basil%20herb%20plant%20with%20vibrant%20green%20leaves%2C%20natural%20daylight%2C%20soft%20focus%20background%2C%20botanical%20photography%20style%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=160&height=160&seq=detail-b-basil&orientation=squarish',
    growthLogs: [],
  },
};

export default function CultivationDetail() {
  const navigate = useNavigate();
  const { deviceId } = useParams<{ deviceId: string }>();
  const device = MOCK_DEVICES[deviceId ?? 'A'] ?? MOCK_DEVICES['A'];

  const handleBack = () => navigate('/');
  const handleAddPhoto = () => navigate('/qr-scan');
  const handleHarvest = () => navigate(`/harvest/${device.id}`);

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
        <h1
          className="text-xl font-normal text-[#1A1A1C] truncate"
          style={{ fontFamily: FONT }}
        >
          {device.cropName}
        </h1>
      </header>

      {/* Scrollable Content */}
      <main className="flex-1 pt-14 pb-24 overflow-y-auto">
        <div className="px-4 py-6 flex flex-col gap-6">

          {/* Info Card */}
          <div className="border border-[#6DAE72] rounded-xl bg-white p-4 flex flex-col gap-4">
            {/* Top row: thumbnail + details */}
            <div className="flex items-start gap-4">
              {/* Thumbnail */}
              <div className="w-20 h-20 flex-shrink-0 rounded-lg overflow-hidden border border-[#D4D4D4]">
                <img
                  src={device.imageUrl}
                  alt={device.cropName}
                  className="w-full h-full object-top object-cover"
                />
              </div>

              {/* Text details */}
              <div className="flex-1 flex flex-col gap-1 pt-0.5">
                <div className="flex items-center gap-2">
                  <span
                    className="inline-flex items-center justify-center w-6 h-6 text-xs border border-[#6DAE72] text-[#6DAE72] rounded flex-shrink-0"
                    style={{ fontFamily: FONT }}
                  >
                    {device.id}
                  </span>
                  <span className="text-base text-[#1A1A1C]" style={{ fontFamily: FONT }}>
                    {device.cropName}
                  </span>
                </div>
                <p className="text-sm text-[#6B6B6B]" style={{ fontFamily: FONT }}>
                  {device.manufacturer}
                </p>
              </div>
            </div>

            {/* Divider + date row */}
            <div className="border-t border-[#D4D4D4]"></div>
            <div className="flex items-center gap-0">
              <span className="text-sm text-[#6B6B6B] flex-1" style={{ fontFamily: FONT }}>
                播種日：{device.seedingDate}
              </span>
              <div className="w-px h-4 bg-[#D4D4D4] mx-3 flex-shrink-0"></div>
              <span className="text-sm font-medium text-[#5B8BD4]" style={{ fontFamily: FONT }}>
                Day {device.daysElapsed}
              </span>
            </div>
          </div>

          {/* Growth Log Section */}
          <div className="flex flex-col gap-3">
            {/* Section header */}
            <div className="flex items-center justify-between">
              <span className="text-base text-[#1A1A1C]" style={{ fontFamily: FONT }}>
                生育ログ
              </span>
              <button
                onClick={handleAddPhoto}
                className="w-8 h-8 flex items-center justify-center border border-[#5B8BD4] rounded-full text-[#5B8BD4] hover:bg-[#5B8BD4]/5 transition-colors"
                aria-label="Add growth photo"
              >
                <i className="ri-camera-line text-base"></i>
              </button>
            </div>

            {/* Log content */}
            {device.growthLogs.length > 0 ? (
              <div className="overflow-x-auto -mx-4 px-4">
                <div className="flex gap-3" style={{ width: 'max-content' }}>
                  {device.growthLogs.map((log, idx) => (
                    <div key={idx} className="flex flex-col items-center gap-1 flex-shrink-0">
                      <div className="w-16 h-16 rounded-lg overflow-hidden border border-[#D4D4D4]">
                        <img
                          src={log.imageUrl}
                          alt={`Growth log ${log.date}`}
                          className="w-full h-full object-cover object-top"
                        />
                      </div>
                      <span className="text-[0.625rem] text-[#6B6B6B]" style={{ fontFamily: FONT }}>
                        {log.date}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div
                className="rounded-lg bg-[#F7F7F7] flex items-center justify-center px-4 py-8"
              >
                <p
                  className="text-sm text-[#6B6B6B] text-center leading-relaxed"
                  style={{ fontFamily: FONT }}
                >
                  まだ写真がありません。<br />
                  デバイスタグのQRをスキャンして追加してください。
                </p>
              </div>
            )}
          </div>

        </div>
      </main>

      {/* Fixed Bottom: Harvest Button */}
      <div
        className="fixed bottom-0 bg-white border-t border-[#D4D4D4] px-4 py-4 z-10"
        style={{ width: '375px' }}
      >
        <button
          onClick={handleHarvest}
          className="w-full h-12 border border-[#6DAE72] rounded-lg text-[#6DAE72] text-base bg-white flex items-center justify-center gap-2 transition-all hover:border-2 active:border-2 font-normal"
          style={{ fontFamily: FONT }}
        >
          <div className="w-5 h-5 flex items-center justify-center">
            <i className="ri-scissors-line text-base text-[#6DAE72]"></i>
          </div>
          収穫
        </button>
      </div>
    </div>
  );
}
