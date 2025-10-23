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
  loading = signal(false);
  qrResult = signal<QRGenerateResponse | null>(null);
  downloading = signal(false);

  generateQR() {
    if (!this.data().trim()) {
      this.toastr.error('Please enter a URL or text');
      return;
    }

    this.loading.set(true);
    this.qrService.generateQR({ data: this.data() }).subscribe({
      next: (response) => {
        this.qrResult.set(response.data);
        this.toastr.success('QR Code generated successfully!');
        this.data.set('');
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error('Failed to generate QR code');
        console.error(error);
        this.loading.set(false);
      }
    });
  }

  // ✅ FIXED: Proper download function
  async downloadQR() {
    if (!this.qrResult()) return;
    
    this.downloading.set(true);
    const imageUrl = this.qrResult()!.downloadUrl;
    const fileName = `smartqr-${this.qrResult()!.qrId}.png`;
    
    try {
      // Fetch the image as blob
      const response = await fetch(imageUrl);
      
      if (!response.ok) {
        throw new Error('Failed to fetch image');
      }
      
      const blob = await response.blob();
      
      // Create a temporary URL for the blob
      const blobUrl = window.URL.createObjectURL(blob);
      
      // Create a temporary anchor element
      const link = document.createElement('a');
      link.href = blobUrl;
      link.download = fileName;
      
      // Append to body, click, and remove
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      // Clean up the blob URL
      window.URL.revokeObjectURL(blobUrl);
      
      this.toastr.success('QR Code downloaded successfully!');
      this.downloading.set(false);
      
    } catch (error) {
      console.error('Download failed:', error);
      this.toastr.error('Failed to download QR code. Opening in new tab instead.');
      // Fallback: open in new tab
      window.open(imageUrl, '_blank');
      this.downloading.set(false);
    }
  }

  // Copy QR Image URL
  copyImageUrl() {
    if (this.qrResult()) {
      navigator.clipboard.writeText(this.qrResult()!.downloadUrl);
      this.toastr.success('Image URL copied to clipboard!');
    }
  }

  // Copy Scan URL
  copyScanUrl() {
    if (this.qrResult()) {
      navigator.clipboard.writeText(this.qrResult()!.scanUrl);
      this.toastr.success('Scan URL copied to clipboard!');
    }
  }
}
