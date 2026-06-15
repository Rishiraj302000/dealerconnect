import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ToastHostComponent } from './core/ui/toast-host.component';
import { ConfirmHostComponent } from './core/ui/confirm-host.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastHostComponent, ConfirmHostComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {}
