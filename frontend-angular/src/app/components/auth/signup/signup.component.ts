import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastr = inject(ToastrService);

  name = signal('');
  email = signal('');
  password = signal('');
  confirmPassword = signal('');
  loading = signal(false);
  showPassword = signal(false);

  signup() {
    // Validation
    if (!this.name().trim() || !this.email().trim() || !this.password().trim()) {
      this.toastr.error('Please fill in all fields');
      return;
    }

    if (this.password().length < 6) {
      this.toastr.error('Password must be at least 6 characters');
      return;
    }

    if (this.password() !== this.confirmPassword()) {
      this.toastr.error('Passwords do not match');
      return;
    }

    this.loading.set(true);

    this.authService.signup({
      name: this.name(),
      email: this.email(),
      password: this.password()
    }).subscribe({
      next: (response) => {
        this.toastr.success('Account created successfully!');
        this.router.navigate(['/generate']);
        this.loading.set(false);
      },
      error: (error) => {
        this.toastr.error(error.error?.message || 'Signup failed. Please try again.');
        this.loading.set(false);
      }
    });
  }

  togglePasswordVisibility() {
    this.showPassword.update(v => !v);
  }
}
