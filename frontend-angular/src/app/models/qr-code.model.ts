export interface QRCode {
  id: string;
  data: string;
  imageUrl: string;
  name?: string;
  createdAt: string;
}

export interface QRGenerateRequest {
  data: string;
  name?: string;
}

export interface QRGenerateResponse {
  qrId: string;
  downloadUrl: string;
  scanUrl: string;
}

export interface QRUpdateRequest {
  data: string;
  name?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
