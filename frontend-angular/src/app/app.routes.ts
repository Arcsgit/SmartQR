import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    loadComponent: () => import('./components/auth/signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: 'generate',
    loadComponent: () => import('./components/generate/generate.component').then(m => m.GenerateComponent),
    canActivate: [authGuard]  // Protected route
  },
  {
    path: 'manage',
    loadComponent: () => import('./components/manage/manage.component').then(m => m.ManageComponent),
    canActivate: [authGuard]  // Protected route
  },
  {
    path: 'analytics/:id',
    loadComponent: () => import('./components/analytics/analytics.component').then(m => m.AnalyticsComponent),
    canActivate: [authGuard]  // Protected route
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];
