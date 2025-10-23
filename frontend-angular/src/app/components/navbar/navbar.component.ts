import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})

export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  showUserMenu = false;

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  logout() {
    this.showUserMenu = false;
    this.authService.logout();
  }

  getCurrentUserName(): string {
    return this.authService.getCurrentUser()?.name || 'User';
  }

  getCurrentUserEmail(): string {
    return this.authService.getCurrentUser()?.email || '';
  }

  getUserInitials(): string {
    const user = this.authService.getCurrentUser();
    if (!user || !user.name) return 'U';
    
    const names = user.name.split(' ');
    if (names.length >= 2) {
      return (names[0][0] + names[1][0]).toUpperCase();
    }
    return user.name.substring(0, 2).toUpperCase();
  }
}
