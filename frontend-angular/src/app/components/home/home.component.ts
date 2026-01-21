import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  authService = inject(AuthService);

  features = [
    {
      title: 'Generate QR Codes',
      description: 'Create QR codes for URLs, text, or any data with a single click.',
      link: '/generate'
    },
    {
      title: 'Manage QR Codes',
      description: 'Update destination URLs without regenerating QR codes.',
      link: '/manage'
    },
    {
      title: 'Analytics & Insights',
      description: 'Track scans, devices, and regions with detailed analytics.',
      link: '/manage'
    },
    {
      title: 'Fast & Reliable',
      description: 'Built with modern technology for speed and reliability.',
      link: '/generate'
    }
  ];
}
