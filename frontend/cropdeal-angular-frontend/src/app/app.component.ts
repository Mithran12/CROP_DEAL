import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgIf, NgFor, NgClass, DatePipe, CommonModule } from '@angular/common';
import { AuthService } from './core/auth.service';
import { ApiService } from './core/api.service';
import { ToastService } from './core/toast.service';
import { Notification } from './core/models';
import { NotificationSocketService } from './core/notification-socket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, NgIf, NgFor, NgClass, DatePipe],
  template: `
    <!-- Global Toast Alert -->
    <div class="toast" *ngIf="toast.message" [ngClass]="toast.type">{{ toast.message }}</div>

    <!-- Navigation Header -->
    <header class="topbar" *ngIf="auth.isLoggedIn() && !isAuthPage()">
      <a routerLink="/dashboard" class="brand">
        <span class="brand-mark">🌾</span>
        <span>Crop<span>Deal</span></span>
      </a>

      <nav>
        <a routerLink="/dashboard" routerLinkActive="active">Overview</a>
        <a routerLink="/marketplace" routerLinkActive="active">Marketplace</a>
        <a routerLink="/market-prices" routerLinkActive="active">Market Rates</a>
        <a routerLink="/negotiations" routerLinkActive="active">Negotiations</a>
        <a routerLink="/auctions" routerLinkActive="active">Auctions</a>
        <a routerLink="/orders" routerLinkActive="active">Orders</a>
        <a *ngIf="auth.role() === 'FARMER'" routerLink="/crops" routerLinkActive="active">My Crops</a>
        <a *ngIf="auth.role() === 'DEALER'" routerLink="/subscriptions" routerLinkActive="active">Subscriptions</a>
        <a *ngIf="auth.role() !== 'ADMIN'" routerLink="/wallet" routerLinkActive="active">Wallet</a>
        <a routerLink="/reviews" routerLinkActive="active">Reviews</a>
        <a *ngIf="auth.role() === 'ADMIN'" routerLink="/admin" routerLinkActive="active">Admin</a>
      </nav>

      <div class="top-actions">
        <!-- Notification Bell Dropdown (Hidden for ADMIN) -->
        <div *ngIf="auth.role() !== 'ADMIN'" style="position: relative;">
          <button class="icon-btn" (click)="toggleNotifications()" title="Notifications">
            🔔
            <span *ngIf="unreadCount > 0" style="position: absolute; top: -4px; right: -4px; background: #e74c3c; color: #fff; font-size: 10px; font-weight: 800; padding: 2px 5px; border-radius: 10px;">
              {{ unreadCount }}
            </span>
          </button>

          <!-- Notifications Popup Modal -->
          <div *ngIf="showNotifs" class="card" style="position: absolute; right: 0; top: 48px; width: 340px; max-height: 420px; overflow-y: auto; z-index: 1000; box-shadow: 0 10px 30px rgba(0,0,0,0.15); border-radius: 14px; padding: 14px;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line); padding-bottom: 8px; margin-bottom: 8px;">
              <b style="font-size: 14px;">Notifications ({{ notifications.length }})</b>
              <div style="display: flex; gap: 6px;">
                <button *ngIf="notifications.length > 0" class="btn btn-light btn-small" (click)="markAllRead()" style="font-size: 10px; padding: 3px 6px;">Mark All Read</button>
                <button class="btn btn-light btn-small" (click)="showNotifs = false">✕</button>
              </div>
            </div>

            <div *ngIf="notifications.length === 0" class="muted" style="text-align: center; padding: 20px 10px; font-size: 13px;">
              No unread notifications right now.
            </div>

            <div *ngFor="let n of notifications" style="padding: 10px; border-bottom: 1px solid var(--line); font-size: 13px;">
              <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                  <b *ngIf="n.title">{{ n.title }}</b>
                  <p style="margin: 3px 0 6px; color: var(--ink);">{{ n.message }}</p>
                  <small class="muted">{{ n.createdAt | date:'shortTime' }}</small>
                </div>
                <button class="btn btn-light btn-small" (click)="markRead(n.id)" style="font-size: 10px; padding: 4px 6px;">
                  Mark Read
                </button>
              </div>
            </div>
          </div>
        </div>

        <span class="role-pill">{{ auth.role() }}</span>
        
        <a routerLink="/profile" class="avatar" title="View Profile">
          {{ (auth.currentUser()?.name || auth.role() || 'U').charAt(0).toUpperCase() }}
        </a>

        <button class="icon-btn" (click)="logout()" title="Logout">↪</button>
      </div>
    </header>

    <main [class.app-main]="auth.isLoggedIn() && !isAuthPage()">
      <router-outlet />
    </main>

    <footer *ngIf="!auth.isLoggedIn() || isAuthPage()" class="footer">
      © 2026 CropDeal Platform · Direct Agricultural Trading with Escrow Protection
    </footer>
  `
})
export class AppComponent implements OnInit {
  auth = inject(AuthService);
  toast = inject(ToastService);
  private api = inject(ApiService);
  private router = inject(Router);
  private notifSocket = inject(NotificationSocketService);

  notifications: Notification[] = [];
  unreadCount = 0;
  showNotifs = false;

  ngOnInit() {
    if (this.auth.isLoggedIn()) {
      this.auth.loadProfile().subscribe(() => {
        this.notifSocket.connect();
      });
      this.pollNotifications();

      this.notifSocket.notification$.subscribe((n) => {
        this.notifications = [n, ...this.notifications];
        this.unreadCount = this.notifications.length;
      });
    }
  }

  toggleNotifications() {
    this.showNotifs = !this.showNotifs;
    if (this.showNotifs) {
      this.pollNotifications();
    }
  }

  pollNotifications() {
    const uid = this.auth.userId();
    if (!uid) return;

    this.api.get<Notification[]>('/notifications/user/' + uid + '/unread').subscribe({
      next: list => {
        this.notifications = list || [];
        this.unreadCount = this.notifications.length;
      },
      error: () => {
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  markRead(id: number) {
    this.api.patch('/notifications/' + id + '/read', {}).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== id);
        this.unreadCount = this.notifications.length;
      }
    });
  }

  markAllRead() {
    const uid = this.auth.userId();
    if (!uid) return;
    this.api.patch('/notifications/user/' + uid + '/read-all', {}).subscribe({
      next: () => {
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  isAuthPage(): boolean {
    const url = this.router.url;
    return url.includes('/login') || url.includes('/register') || url.includes('/forgot-password');
  }

  logout() {
    this.notifSocket.disconnect();
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
