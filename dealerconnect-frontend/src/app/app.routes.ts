import { Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { ShellComponent } from './layout/shell.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { DealersComponent } from './features/dealers/dealers.component';
import { DealerDetailComponent } from './features/dealers/dealer-detail.component';
import { FavoritesComponent } from './features/favorites/favorites.component';
import { AuditComponent } from './features/audit/audit.component';
import { authGuard, adminGuard } from './core/auth/auth.guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'dealers', component: DealersComponent },
      { path: 'dealers/:id', component: DealerDetailComponent },
      { path: 'favorites', component: FavoritesComponent },
      { path: 'audit', component: AuditComponent, canActivate: [adminGuard] },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
