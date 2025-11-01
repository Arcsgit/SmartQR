import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../environments/environment';
import {
  ApiResponse,
  QRCode,
  QRGenerateRequest,
  QRGenerateResponse,
  QRUpdateRequest
} from '../models/qr-code.model';
import { AnalyticsResponse } from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class QrCodeService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private apiUrl = environment.apiUrl;

  /**
   * Generate a new QR code
   */
  generateQR(request: QRGenerateRequest): Observable<ApiResponse<QRGenerateResponse>> {
    return this.http.post<ApiResponse<QRGenerateResponse>>(
      `${this.apiUrl}/qr/generate`,
      request
    );
  }

  /**
   * Get all QR codes for current user
   */
  getAllQRCodes(): Observable<ApiResponse<QRCode[]>> {
    return this.http.get<ApiResponse<QRCode[]>>(`${this.apiUrl}/qr/all`);
  }

  /**
   * Get single QR code by ID
   */
  getQRCode(id: string): Observable<ApiResponse<QRCode>> {
    return this.http.get<ApiResponse<QRCode>>(`${this.apiUrl}/qr/${id}`);
  }

  /**
   * Update QR code
   */
  updateQR(id: string, request: QRUpdateRequest): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(
      `${this.apiUrl}/qr/update/${id}`,
      request
    );
  }

  /**
   * Delete QR code
   */
  deleteQRCode(id: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/qr/${id}`);
  }

  /**
   * Get analytics for a QR code
   */
  getAnalytics(qrId: string): Observable<ApiResponse<AnalyticsResponse>> {
    return this.http.get<ApiResponse<AnalyticsResponse>>(
      `${this.apiUrl}/qr/analytics/${qrId}`
    );
  }

  /**
   * Download QR code image as blob
   */
  downloadQRImage(qrId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/qr/download/${qrId}`, {
      responseType: 'blob'
    });
  }

  // ============= QR Name Management (Frontend-only) =============

  /**
   * Store QR code names in localStorage
   */
  private getQRNames(): Map<string, string> {
    if (!isPlatformBrowser(this.platformId)) {
      return new Map();
    }

    try {
      const names = localStorage.getItem('qr_names');
      if (names) {
        return new Map(JSON.parse(names));
      }
    } catch (error) {
      console.error('Failed to load QR names', error);
    }
    return new Map();
  }

  private saveQRNames(names: Map<string, string>): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    try {
      localStorage.setItem('qr_names', JSON.stringify(Array.from(names.entries())));
    } catch (error) {
      console.error('Failed to save QR names', error);
    }
  }

  /**
   * Get name for a QR code
   */
  getQRName(qrId: string): string | null {
    const names = this.getQRNames();
    return names.get(qrId) || null;
  }

  /**
   * Set name for a QR code
   */
  setQRName(qrId: string, name: string): void {
    const names = this.getQRNames();
    names.set(qrId, name);
    this.saveQRNames(names);
  }

  /**
   * Delete name for a QR code
   */
  deleteQRName(qrId: string): void {
    const names = this.getQRNames();
    names.delete(qrId);
    this.saveQRNames(names);
  }

  /**
   * Generate a smart name from URL
   */
  generateSmartName(url: string): string {
    try {
      const urlObj = new URL(url);
      const domain = urlObj.hostname.replace('www.', '');
      const path = urlObj.pathname.split('/').filter(p => p).join(' ');

      if (path) {
        return `${domain} - ${path.substring(0, 20)}`;
      }
      return domain;
    } catch {
      // Not a URL, use first 30 chars
      return url.substring(0, 30);
    }
  }
}
