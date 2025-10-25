// import { Component, inject, OnInit, signal } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ActivatedRoute, RouterLink } from '@angular/router';
// import { QrCodeService } from '../../services/qr-code.service';
// import { AnalyticsResponse } from '../../models/analytics.model';
// import { ToastrService } from 'ngx-toastr';
// import { BaseChartDirective } from 'ng2-charts';
// import { ChartConfiguration } from 'chart.js';

// @Component({
//   selector: 'app-analytics',
//   standalone: true,
//   imports: [CommonModule, RouterLink, BaseChartDirective],
//   templateUrl: './analytics.component.html',
//   styleUrl: './analytics.component.css'
// })

// export class AnalyticsComponent implements OnInit {
//   private qrService = inject(QrCodeService);
//   private route = inject(ActivatedRoute);
//   private toastr = inject(ToastrService);

//   qrId = '';
//   analytics = signal<AnalyticsResponse | null>(null);
//   loading = signal(true);

//   lineChartData: ChartConfiguration['data'] = {
//     datasets: [],
//     labels: []
//   };

//   lineChartOptions: ChartConfiguration['options'] = {
//     responsive: true,
//     maintainAspectRatio: false,
//     plugins: {
//       legend: {
//         display: true
//       }
//     }
//   };

//   ngOnInit() {
//     this.qrId = this.route.snapshot.paramMap.get('id') || '';
//     this.loadAnalytics();
//   }

//   loadAnalytics() {
//     this.qrService.getAnalytics(this.qrId).subscribe({
//       next: (response) => {
//         this.analytics.set(response.data);
//         this.prepareChartData(response.data);
//         this.loading.set(false);
//       },
//       error: (error) => {
//         this.toastr.error('Failed to load analytics');
//         console.error(error);
//         this.loading.set(false);
//       }
//     });
//   }

//   prepareChartData(data: AnalyticsResponse) {
//     const labels = Object.keys(data.dailyScans);
//     const values = Object.values(data.dailyScans);

//     this.lineChartData = {
//       datasets: [
//         {
//           data: values,
//           label: 'Scans per Day',
//           borderColor: '#2563eb',
//           backgroundColor: 'rgba(37, 99, 235, 0.1)',
//           fill: true,
//           tension: 0.4
//         }
//       ],
//       labels: labels
//     };
//   }

//   formatDate(dateString: string): string {
//     return new Date(dateString).toLocaleDateString('en-US', {
//       year: 'numeric',
//       month: 'short',
//       day: 'numeric',
//       hour: '2-digit',
//       minute: '2-digit'
//     });
//   }

//   getDeviceStatsArray() {
//     return this.analytics()
//       ? Object.entries(this.analytics()!.deviceStats).map(([key, value]) => ({ device: key, count: value }))
//       : [];
//   }
// }

import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { QrCodeService } from '../../services/qr-code.service';
import { AnalyticsResponse } from '../../models/analytics.model';
import { QRCode } from '../../models/qr-code.model';
import { ToastrService } from 'ngx-toastr';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, RouterLink, BaseChartDirective],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
  private qrService = inject(QrCodeService);
  private route = inject(ActivatedRoute);
  private toastr = inject(ToastrService);

  qrId = '';
  qrCode = signal<QRCode | null>(null);
  analytics = signal<AnalyticsResponse | null>(null);
  loading = signal(true);

  // Chart configuration
  lineChartData: ChartConfiguration['data'] = {
    datasets: [],
    labels: []
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: {
          size: 14
        },
        bodyFont: {
          size: 13
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1
        }
      }
    }
  };

  ngOnInit() {
    this.qrId = this.route.snapshot.paramMap.get('id') || '';
    if (this.qrId) {
      this.loadQRCodeDetails();
      this.loadAnalytics();
    } else {
      this.toastr.error('Invalid QR code ID');
      this.loading.set(false);
    }
  }

  loadQRCodeDetails() {
    this.qrService.getQRCode(this.qrId).subscribe({
      next: (response) => {
        this.qrCode.set(response.data);
      },
      error: (error) => {
        console.error('Failed to load QR code details', error);
        this.toastr.error('Failed to load QR code details');
      }
    });
  }

  loadAnalytics() {
    this.qrService.getAnalytics(this.qrId).subscribe({
      next: (response) => {
        this.analytics.set(response.data);
        this.prepareChartData(response.data);
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error('Failed to load analytics');
        console.error(error);
        this.loading.set(false);
      }
    });
  }

  prepareChartData(data: AnalyticsResponse) {
    const labels = Object.keys(data.dailyScans).sort();
    const values = labels.map(date => data.dailyScans[date]);

    this.lineChartData = {
      datasets: [
        {
          data: values,
          label: 'Scans per Day',
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37, 99, 235, 0.1)',
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: '#2563eb',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2
        }
      ],
      labels: labels.map(date => {
        const d = new Date(date);
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      })
    };
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

  formatDateShort(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getDeviceStatsArray() {
    if (!this.analytics()) return [];
    return Object.entries(this.analytics()!.deviceStats).map(([device, count]) => ({
      device,
      count
    })).sort((a, b) => b.count - a.count);
  }

  truncateUrl(url: string, maxLength: number = 60): string {
    if (url.length <= maxLength) {
      return url;
    }
    return url.substring(0, maxLength) + '...';
  }

  copyUrl(url: string) {
    navigator.clipboard.writeText(url);
    this.toastr.success('URL copied to clipboard!');
  }

  getScanUrl(): string {
    return `${window.location.origin}/api/qr/scan/${this.qrId}`;
  }

  getDisplayName(): string|undefined {
    if (this.qrCode()?.name) {
      return this.qrCode()!.name;
    }
    return this.truncateUrl(this.qrCode()?.data || 'QR Code', 40);
  }

  getDeviceIcon(device: string): string {
    const icons: { [key: string]: string } = {
      'Mobile': '📱',
      'Desktop': '💻',
      'Tablet': '📱',
      'Bot': '🤖',
      'Unknown': '❓'
    };
    return icons[device] || '❓';
  }

  getDevicePercentage(count: number): number {
    if (!this.analytics()) return 0;
    return (count / this.analytics()!.scanCount) * 100;
  }
}
