import type { RouteObject } from "react-router-dom";
import NotFound from "../pages/NotFound";
import Home from "../pages/home/page";
import SeedingRegistration from "../pages/seeding/page";
import CultivationDetail from "../pages/detail/page";
import QRScanPage from "../pages/qrscan/page";
import PhotoRecordPage from "../pages/photo-record/page";
import HarvestPage from "../pages/harvest/page";
import LabelPrintPage from "../pages/label-print/page";
import HistoryPage from "../pages/history/page";
import SettingsPage from "../pages/settings/page";
import StatisticsPage from "../pages/statistics/page";

const routes: RouteObject[] = [
  {
    path: "/",
    element: <Home />,
  },
  {
    path: "/seeding",
    element: <SeedingRegistration />,
  },
  {
    path: "/detail/:deviceId",
    element: <CultivationDetail />,
  },
  {
    path: "/qr-scan",
    element: <QRScanPage />,
  },
  {
    path: "/photo-record/:deviceId",
    element: <PhotoRecordPage />,
  },
  {
    path: "/harvest/:deviceId",
    element: <HarvestPage />,
  },
  {
    path: "/label-print/:deviceId",
    element: <LabelPrintPage />,
  },
  {
    path: "/history",
    element: <HistoryPage />,
  },
  {
    path: "/settings",
    element: <SettingsPage />,
  },
  {
    path: "/statistics",
    element: <StatisticsPage />,
  },
  {
    path: "*",
    element: <NotFound />,
  },
];

export default routes;
