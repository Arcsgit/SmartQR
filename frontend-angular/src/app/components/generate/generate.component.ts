import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QrCodeService } from '../../services/qr-code.service';
import { QRGenerateResponse } from '../../models/qr-code.model';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './generate.component.html',
  styleUrl: './generate.component.css'
})
export class GenerateComponent {
  private qrService = inject(QrCodeService);
  private toastr = inject(ToastrService);

  data = signal('');
  name = signal('');
  loading = signal(false);
  qrResult = signal<QRGenerateResponse | null>(null);
  downloading = signal(false);

  generateQR() {
    if (!this.data().trim()) {
      this.toastr.error('Please enter a URL or text');
      return;
    }

    this.loading.set(true);
    
    this.qrService.generateQR({ 
      data: this.data(),
      name: this.name().trim() || undefined
    }).subscribe({
      next: (response) => {
        this.qrResult.set(response.data);
        this.toastr.success('QR Code generated successfully!');
        this.data.set('');
        this.name.set('');
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error('Failed to generate QR code');
        console.error(error);
        this.loading.set(false);
      }
    });
  }

  async downloadQR() {
    if (!this.qrResult()) return;
    
    this.downloading.set(true);
    const qrId = this.qrResult()!.qrId;
    
    this.qrService.downloadQRImage(qrId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `smartqr-${qrId}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        this.toastr.success('QR Code downloaded!');
        this.downloading.set(false);
      },
      error: (error) => {
        console.error('Download failed:', error);
        this.toastr.error('Download failed. Opening in new tab.');
        window.open(this.qrResult()!.downloadUrl, '_blank');
        this.downloading.set(false);
      }
    });
  }

  copyImageUrl() {
    if (this.qrResult()) {
      navigator.clipboard.writeText(this.qrResult()!.downloadUrl);
      this.toastr.success('Image URL copied to clipboard!');
    }
  }

  copyScanUrl() {
    if (this.qrResult()) {
      navigator.clipboard.writeText(this.qrResult()!.scanUrl);
      this.toastr.success('Scan URL copied to clipboard!');
    }
  }
}
