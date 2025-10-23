import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toastr = inject(ToastrService);

  email = signal('');
  password = signal('');
  loading = signal(false);
  showPassword = signal(false);

  login() {
    if (!this.email().trim() || !this.password().trim()) {
      this.toastr.error('Please fill in all fields');
      return;
    }

    this.loading.set(true);

    this.authService.login({
      email: this.email(),
      password: this.password()
    }).subscribe({
      next: (response) => {
        this.toastr.success('Login successful!');
        
        // Get return URL or default to generate
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/generate';
        this.router.navigate([returnUrl]);
        
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error(error.error?.message || 'Invalid email or password');
        this.loading.set(false);
      }
    });
  }

  togglePasswordVisibility() {
    this.showPassword.update(v => !v);
  }
}
