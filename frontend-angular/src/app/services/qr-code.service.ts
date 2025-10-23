import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { 
  QRCode, 
  QRGenerateRequest, 
  QRGenerateResponse, 
  QRUpdateRequest,
  ApiResponse 
} from '../models/qr-code.model';
import { AnalyticsResponse } from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class QrCodeService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  generateQR(request: QRGenerateRequest): Observable<ApiResponse<QRGenerateResponse>> {
    return this.http.post<ApiResponse<QRGenerateResponse>>(
      `${this.apiUrl}/qr/generate`, 
      request
    ).pipe(catchError(this.handleError));
  }

  updateQR(id: string, request: QRUpdateRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(
      `${this.apiUrl}/qr/update/${id}`, 
      request
    ).pipe(catchError(this.handleError));
  }

  getAllQRCodes(): Observable<ApiResponse<QRCode[]>> {
    return this.http.get<ApiResponse<QRCode[]>>(
      `${this.apiUrl}/qr/all`
    ).pipe(catchError(this.handleError));
  }

  getQRCode(id: string): Observable<ApiResponse<QRCode>> {
    return this.http.get<ApiResponse<QRCode>>(
      `${this.apiUrl}/qr/${id}`
    ).pipe(catchError(this.handleError));
  }

  getAnalytics(id: string): Observable<ApiResponse<AnalyticsResponse>> {
    return this.http.get<ApiResponse<AnalyticsResponse>>(
      `${this.apiUrl}/qr/analytics/${id}`
    ).pipe(catchError(this.handleError));
  }

  deleteQRCode(id: string): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(
      `${this.apiUrl}/qr/${id}`
    ).pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    return throwError(() => error);
  }
}
