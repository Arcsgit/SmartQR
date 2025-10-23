import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { QrCodeService } from '../../services/qr-code.service';
import { QRCode } from '../../models/qr-code.model';
import { ToastrService } from 'ngx-toastr';

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

  ngOnInit() {
    this.loadQRCodes();
  }

  loadQRCodes() {
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
  }

  saveEdit(id: string) {
    if (!this.editData().trim()) {
      this.toastr.error('Please enter a valid URL or text');
      return;
    }

    this.qrService.updateQR(id, { data: this.editData() }).subscribe({
      next: () => {
        this.qrCodes.update(codes =>
          codes.map(qr => qr.id === id ? { ...qr, data: this.editData() } : qr)
        );
        this.editingId.set(null);
        this.editData.set('');
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
  }

  deleteQR(id: string) {
    if (!confirm('Are you sure you want to delete this QR code?')) {
      return;
    }

    this.qrService.deleteQRCode(id).subscribe({
      next: () => {
        this.qrCodes.update(codes => codes.filter(qr => qr.id !== id));
        this.toastr.success('QR code deleted successfully!');
      },
      error: (error) => {
        this.toastr.error('Failed to delete QR code');
        console.error(error);
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
}
