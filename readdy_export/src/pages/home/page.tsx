import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

export default function Home() {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState('home');
  const navigate = useNavigate();

  // Mock data for active devices
  const devices = [
    {
      id: 'A',
      cropName: 'ミニトマト',
      seedingDate: '2024-01-15',
      daysElapsed: 32,
      isEmpty: false,
      imageUrl: 'https://readdy.ai/api/search-image?query=Close-up%20photograph%20of%20a%20small%20cherry%20tomato%20plant%20seedling%20with%20green%20leaves%2C%20natural%20daylight%2C%20soft%20focus%20background%2C%20botanical%20photography%20style%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=80&height=80&seq=device-a-tomato&orientation=squarish'
    },
    {
      id: 'B',
      cropName: 'バジル',
      seedingDate: '2024-02-01',
      daysElapsed: 15,
      isEmpty: false,
      imageUrl: 'https://readdy.ai/api/search-image?query=Close-up%20photograph%20of%20fresh%20basil%20herb%20plant%20with%20vibrant%20green%20leaves%2C%20natural%20daylight%2C%20soft%20focus%20background%2C%20botanical%20photography%20style%2C%20clean%20white%20background%2C%20centered%20composition%2C%20high%20detail%2C%20realistic%20texture&width=80&height=80&seq=device-b-basil&orientation=squarish'
    },
    {
      id: 'C',
      isEmpty: true
    },
    {
      id: 'D',
      isEmpty: true
    }
  ];

  const calculateDaysElapsed = (seedingDate: string) => {
    const today = new Date();
    const seeding = new Date(seedingDate);
    const diffTime = Math.abs(today.getTime() - seeding.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const handleAddDevice = () => {
    navigate('/seeding');
  };

  const handleDeviceClick = (deviceId: string) => {
    const device = devices.find((d) => d.id === deviceId);
    if (device?.isEmpty) {
      navigate('/seeding');
    } else {
      navigate(`/detail/${deviceId}`);
    }
  };

  const handleTabClick = (tab: string) => {
    setActiveTab(tab);
    if (tab === 'history') {
      navigate('/history');
    }
    if (tab === 'statistics') {
      navigate('/statistics');
    }
    if (tab === 'settings') {
      navigate('/settings');
    }
  };

  return (
    <div className="flex flex-col h-screen bg-white" style={{ width: '375px', minHeight: '812px' }}>
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 bg-white border-b border-[#D4D4D4] z-10" style={{ width: '375px' }}>
        <div className="flex items-center justify-between px-4 h-14">
          <div className="flex items-center gap-2">
            <img 
              src="https://public.readdy.ai/ai/img_res/87fa4a8b-6c3b-4d72-8d56-7415dba84ecc.png" 
              alt="Sodatter-BT" 
              className="w-8 h-8 object-contain"
            />
            <h1 className="text-base font-normal text-[#1A1A1C]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              Sodatter-BT
            </h1>
          </div>
          <button
            onClick={handleAddDevice}
            className="w-9 h-9 flex items-center justify-center border border-[#5B8BD4] rounded text-[#5B8BD4] hover:bg-[#5B8BD4]/5 transition-colors"
            aria-label="Add device"
          >
            <i className="ri-add-line text-xl"></i>
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 pt-14 pb-16 overflow-y-auto">
        <div className="px-4 py-6">
          {/* Device Grid */}
          <div className="grid grid-cols-2 gap-4">
            {devices.map((device) => (
              <div key={device.id}>
                {device.isEmpty ? (
                  // Empty Device Card
                  <button
                    onClick={() => handleDeviceClick(device.id)}
                    className="w-full aspect-square border border-dashed border-[#D4D4D4] rounded-lg bg-white flex flex-col items-center justify-center gap-2 hover:border-[#6B6B6B] transition-colors"
                  >
                    <i className="ri-add-line text-3xl text-[#6B6B6B]"></i>
                    <span className="text-sm text-[#6B6B6B]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
                      タップして登録
                    </span>
                  </button>
                ) : (
                  // Active Device Card
                  <button
                    onClick={() => handleDeviceClick(device.id)}
                    className="w-full aspect-square border border-[#6DAE72] rounded-lg bg-white p-3 flex flex-col hover:border-[#6DAE72]/80 transition-colors text-left"
                  >
                    <div className="flex items-start justify-between mb-2">
                      <span 
                        className="inline-flex items-center justify-center w-6 h-6 text-xs border border-[#6DAE72] text-[#6DAE72] rounded"
                        style={{ fontFamily: 'Noto Sans JP, sans-serif' }}
                      >
                        {device.id}
                      </span>
                      <div className="w-12 h-12 flex-shrink-0 overflow-hidden rounded">
                        <img 
                          src={device.imageUrl} 
                          alt={device.cropName}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    </div>
                    <div className="flex-1 flex flex-col justify-end">
                      <h3 className="text-base font-medium text-[#1A1A1C] mb-1" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
                        {device.cropName}
                      </h3>
                      <p className="text-xs text-[#6B6B6B] mb-2" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
                        播種日: {device.seedingDate}
                      </p>
                      <span className="text-sm font-medium text-[#5B8BD4]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
                        Day {device.daysElapsed}
                      </span>
                    </div>
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-[#D4D4D4] z-10" style={{ width: '375px' }}>
        <div className="grid grid-cols-4 h-16">
          <button
            onClick={() => handleTabClick('home')}
            className={`flex flex-col items-center justify-center gap-1 transition-colors ${
              activeTab === 'home' ? 'text-[#5B8BD4]' : 'text-[#6B6B6B]'
            }`}
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className={`${activeTab === 'home' ? 'ri-home-5-fill' : 'ri-home-5-line'} text-xl`}></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              ホーム
            </span>
            {activeTab === 'home' && (
              <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#5B8BD4]" style={{ width: '93.75px' }}></div>
            )}
          </button>

          <button
            onClick={() => handleTabClick('history')}
            className={`flex flex-col items-center justify-center gap-1 transition-colors ${
              activeTab === 'history' ? 'text-[#5B8BD4]' : 'text-[#6B6B6B]'
            }`}
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className={`${activeTab === 'history' ? 'ri-history-fill' : 'ri-history-line'} text-xl`}></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              履歴
            </span>
            {activeTab === 'history' && (
              <div className="absolute bottom-0 h-0.5 bg-[#5B8BD4]" style={{ width: '93.75px', left: '93.75px' }}></div>
            )}
          </button>

          <button
            onClick={() => handleTabClick('statistics')}
            className={`flex flex-col items-center justify-center gap-1 transition-colors ${
              activeTab === 'statistics' ? 'text-[#5B8BD4]' : 'text-[#6B6B6B]'
            }`}
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className={`${activeTab === 'statistics' ? 'ri-bar-chart-2-fill' : 'ri-bar-chart-2-line'} text-xl`}></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              統計
            </span>
            {activeTab === 'statistics' && (
              <div className="absolute bottom-0 h-0.5 bg-[#5B8BD4]" style={{ width: '93.75px', left: '187.5px' }}></div>
            )}
          </button>

          <button
            onClick={() => handleTabClick('settings')}
            className={`flex flex-col items-center justify-center gap-1 transition-colors ${
              activeTab === 'settings' ? 'text-[#5B8BD4]' : 'text-[#6B6B6B]'
            }`}
          >
            <div className="w-6 h-6 flex items-center justify-center">
              <i className={`${activeTab === 'settings' ? 'ri-settings-3-fill' : 'ri-settings-3-line'} text-xl`}></i>
            </div>
            <span className="text-[0.625rem]" style={{ fontFamily: 'Noto Sans JP, sans-serif' }}>
              設定
            </span>
            {activeTab === 'settings' && (
              <div className="absolute bottom-0 h-0.5 bg-[#5B8BD4]" style={{ width: '93.75px', left: '281.25px' }}></div>
            )}
          </button>
        </div>
      </nav>
    </div>
  );
}