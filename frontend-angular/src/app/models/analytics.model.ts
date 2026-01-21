export interface QRAnalytics {
  id: number;
  qrId: string;
  timestamp: string;
  deviceInfo: string;
  ipAddress: string;
  region: string;
}

export interface AnalyticsResponse {
  scanCount: number;
  uniqueIps: number;
  scans: QRAnalytics[];
  deviceStats: { [key: string]: number };
  dailyScans: { [key: string]: number };
}
