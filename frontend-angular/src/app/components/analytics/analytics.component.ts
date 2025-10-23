import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { QrCodeService } from '../../services/qr-code.service';
import { AnalyticsResponse } from '../../models/analytics.model';
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
  analytics = signal<AnalyticsResponse | null>(null);
  loading = signal(true);

  lineChartData: ChartConfiguration['data'] = {
    datasets: [],
    labels: []
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true
      }
    }
  };

  ngOnInit() {
    this.qrId = this.route.snapshot.paramMap.get('id') || '';
    this.loadAnalytics();
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
    const labels = Object.keys(data.dailyScans);
    const values = Object.values(data.dailyScans);

    this.lineChartData = {
      datasets: [
        {
          data: values,
          label: 'Scans per Day',
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37, 99, 235, 0.1)',
          fill: true,
          tension: 0.4
        }
      ],
      labels: labels
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

  getDeviceStatsArray() {
    return this.analytics()
      ? Object.entries(this.analytics()!.deviceStats).map(([key, value]) => ({ device: key, count: value }))
      : [];
  }
}
