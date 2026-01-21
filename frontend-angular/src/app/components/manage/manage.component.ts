import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { QrCodeService } from '../../services/qr-code.service';
import { QRCode } from '../../models/qr-code.model';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-manage',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './manage.component.html',
  styleUrl: './manage.component.css'
})
export class ManageComponent implements OnInit {
  private qrService = inject(QrCodeService);
  private toastr = inject(ToastrService);

  qrCodes = signal<QRCode[]>([]);
  loading = signal(true);
  editingId = signal<string | null>(null);
  editData = signal('');
  editName = signal('');
  deletingId = signal<string | null>(null);
  downloadingId = signal<string | null>(null);

  ngOnInit() {
    this.loadQRCodes();
  }

  loadQRCodes() {
    this.loading.set(true);
    this.qrService.getAllQRCodes().subscribe({
      next: (response) => {
        this.qrCodes.set(response.data);
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error('Failed to load QR codes');
        console.error(error);
        this.loading.set(false);
      }
    });
  }

  startEdit(qr: QRCode) {
    this.editingId.set(qr.id);
    this.editData.set(qr.data);
    this.editName.set(qr.name || '');
  }

  saveEdit(id: string) {
    if (!this.editData().trim()) {
      this.toastr.error('Please enter a valid URL or text');
      return;
    }

    this.qrService.updateQR(id, { 
      data: this.editData(),
      name: this.editName().trim() || undefined
    }).subscribe({
      next: () => {
        this.qrCodes.update(codes =>
          codes.map(qr => qr.id === id ? { 
            ...qr, 
            data: this.editData(),
            name: this.editName().trim() || qr.name
          } : qr)
        );
        this.editingId.set(null);
        this.editData.set('');
        this.editName.set('');
        this.toastr.success('QR code updated successfully!');
      },
      error: (error) => {
        this.toastr.error('Failed to update QR code');
        console.error(error);
      }
    });
  }

  cancelEdit() {
    this.editingId.set(null);
    this.editData.set('');
    this.editName.set('');
  }

  deleteQR(id: string) {
    this.deletingId.set(id);

    this.qrService.deleteQRCode(id).subscribe({
      next: () => {
        this.qrCodes.update(codes => codes.filter(qr => qr.id !== id));
        this.toastr.success('QR code deleted successfully!');
        this.deletingId.set(null);
      },
      error: (error) => {
        this.toastr.error('Failed to delete QR code');
        console.error(error);
        this.deletingId.set(null);
      }
    });
  }

  async downloadQR(qr: QRCode) {
    this.downloadingId.set(qr.id);
    
    this.qrService.downloadQRImage(qr.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        
        const filename = qr.name 
          ? `${qr.name.replace(/[^a-z0-9]/gi, '-').toLowerCase()}.png`
          : `smartqr-${qr.id}.png`;
        
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        this.toastr.success('QR Code downloaded!');
        this.downloadingId.set(null);
      },
      error: (error) => {
        console.error('Download failed:', error);
        this.toastr.error('Download failed. Opening in new tab.');
        window.open(qr.imageUrl, '_blank');
        this.downloadingId.set(null);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  truncateUrl(url: string, maxLength: number = 50): string {
    if (url.length <= maxLength) {
      return url;
    }
    return url.substring(0, maxLength) + '...';
  }

  copyUrl(url: string) {
    navigator.clipboard.writeText(url);
    this.toastr.success('URL copied to clipboard!');
  }

  getDisplayName(qr: QRCode): string {
    return qr.name || this.truncateUrl(qr.data, 40);
  }

  isDeleting(id: string): boolean {
    return this.deletingId() === id;
  }

  isDownloading(id: string): boolean {
    return this.downloadingId() === id;
  }

  getScanUrl(qrId: string): string {
    const baseUrl = environment.apiUrl.replace('/api', '');
    return `${baseUrl}/api/qr/scan/${qrId}`;
  }

  copyScanUrl(qrId: string) {
    const scanUrl = this.getScanUrl(qrId);
    navigator.clipboard.writeText(scanUrl);
    this.toastr.success('Scan URL copied to clipboard!');
  }
}
