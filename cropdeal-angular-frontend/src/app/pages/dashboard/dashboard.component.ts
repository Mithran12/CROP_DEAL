import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, CurrencyPipe, CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Crop, Order, User, Notification, Wallet, FarmerWallet } from '../../core/models';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, DatePipe, CurrencyPipe, RouterLink],
  template: `
    <div class="page">
      <!-- Role Hero Banner -->
      <section class="hero">
        <div>
          <div class="eyebrow" style="color: #cfe96b;">{{ role }} WORKSPACE</div>
          <h1>Welcome back, {{ user?.name || (role === 'FARMER' ? 'Farmer' : role === 'DEALER' ? 'Dealer' : 'Administrator') }} 👋</h1>
          <p>
            {{ role === 'FARMER' 
               ? 'Turn your hard-earned harvest into guaranteed fair payouts. Publish crops, respond to dealer offers, and track escrow settlements.' 
               : role === 'DEALER'
               ? 'Source high quality harvests directly from verified farms. Negotiate terms, fund secure escrow, and confirm delivery.'
               : 'Full system oversight across farmers, dealers, crop listings, escrow settlements, and compliance reporting.' }}
          </p>
          <div style="display: flex; gap: 12px; margin-top: 14px; flex-wrap: wrap;">
            <a *ngIf="role === 'DEALER'" routerLink="/marketplace" class="btn" style="background: #cfe96b; color: #173a28; text-decoration: none; font-weight: 800;">
              🔍 Browse Marketplace
            </a>
            <a *ngIf="role === 'FARMER'" routerLink="/crops" class="btn" style="background: #cfe96b; color: #173a28; text-decoration: none; font-weight: 800;">
              🌾 + Publish Crop
            </a>
            <a routerLink="/negotiations" class="btn" style="background: #ffffff1f; color: #fff; border: 1px solid #ffffff40; text-decoration: none;">
              💬 Negotiations
            </a>
            <a routerLink="/orders" class="btn" style="background: #ffffff1f; color: #fff; border: 1px solid #ffffff40; text-decoration: none;">
              📦 Orders
            </a>
            <a routerLink="/wallet" class="btn" style="background: #ffffff1f; color: #fff; border: 1px solid #ffffff40; text-decoration: none;">
              💰 Wallet & Escrow
            </a>
          </div>
        </div>
        <div class="hero-art">{{ role === 'FARMER' ? '🚜' : role === 'DEALER' ? '🏪' : '⚙️' }}</div>
      </section>

      <!-- Profile Completion Banner if incomplete -->
      <div *ngIf="!user" class="card" style="padding: 16px 22px; margin-top: 20px; background: #fffde7; border: 1px solid #ffe082; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <b style="color: #7f5c09;">Profile Incomplete</b>
          <div style="font-size: 13px; color: #8d6b14;">Complete your profile contact and banking details to unlock all marketplace features.</div>
        </div>
        <a routerLink="/profile" class="btn btn-primary btn-small">Set Up Profile</a>
      </div>

      <!-- Quick Metrics Grid -->
      <div class="grid grid-4" style="margin-top: 20px;">
        <div class="card stat">
          <div class="icon">🌱</div>
          <div class="number">{{ cropCount }}</div>
          <div class="label">{{ role === 'FARMER' ? 'My Active Crops' : 'Available Crops' }}</div>
        </div>

        <div class="card stat">
          <div class="icon">📦</div>
          <div class="number">{{ orders.length }}</div>
          <div class="label">Total Orders</div>
        </div>

        <div class="card stat">
          <div class="icon">💰</div>
          <div class="number" style="color: var(--green);">{{ totalValue | currency:'INR':'symbol':'1.0-0' }}</div>
          <div class="label">{{ role === 'FARMER' ? 'Gross Sales' : role === 'DEALER' ? 'Purchase Volume' : 'System Volume' }}</div>
        </div>

        <div class="card stat">
          <div class="icon">🔔</div>
          <div class="number">{{ unread }}</div>
          <div class="label">Unread Notifications</div>
        </div>
      </div>

      <!-- Recent Orders Section -->
      <div class="section-head" style="margin-top: 36px;">
        <div>
          <div class="eyebrow">RECENT TRANSACTIONS</div>
          <h2 class="title" style="font-size: 24px; margin: 4px 0;">Latest Order Activity</h2>
        </div>
        <a routerLink="/orders" class="btn btn-light">View All Orders</a>
      </div>

      <div class="card table-wrap">
        <table class="table" *ngIf="orders.length; else emptyOrders">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Quantity</th>
              <th>Agreed Rate</th>
              <th>Total Amount</th>
              <th>Order Status</th>
              <th>Payment</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let o of orders.slice(0, 5)">
              <td><b>#{{ o.orderNumber || o.id }}</b><br><small class="muted">{{ o.createdAt | date:'mediumDate' }}</small></td>
              <td>{{ o.quantity }} qtl</td>
              <td>{{ o.agreedPrice | currency:'INR':'symbol':'1.0-0' }} / qtl</td>
              <td><b style="color: var(--green);">{{ o.totalAmount | currency:'INR':'symbol':'1.0-0' }}</b></td>
              <td><span class="badge">{{ o.status }}</span></td>
              <td>
                <span class="badge" [style.background]="o.paymentStatus === 'SUCCESS' ? '#eef5ee' : '#fff8e1'"
                      [style.color]="o.paymentStatus === 'SUCCESS' ? 'var(--green)' : '#b78103'">
                  {{ o.paymentStatus || 'PENDING' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <ng-template #emptyOrders>
          <div class="empty">No recent orders yet. Start by browsing or publishing harvests.</div>
        </ng-template>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);

  role = this.auth.role();
  user?: User;
  orders: Order[] = [];
  cropCount = 0;
  unread = 0;
  totalValue = 0;

  ngOnInit() {
    this.auth.loadProfile().subscribe((u: User | null) => {
      if (u) {
        this.user = u;
        this.auth.setUserId(u.id);
      }
      this.loadData();
    });
  }

  loadData() {
    const uid = this.auth.userId();
    const role = this.role;

    if (role === 'FARMER' && uid) {
      this.api.get<Crop[]>('/crops/farmer/' + uid).subscribe({
        next: crops => this.cropCount = crops.length,
        error: () => this.cropCount = 0
      });

      this.api.get<Order[]>('/orders/farmer/' + uid).subscribe({
        next: list => {
          this.orders = list || [];
          this.totalValue = this.orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
        },
        error: () => this.orders = []
      });
    } else if (role === 'DEALER' && uid) {
      this.api.get<Crop[]>('/crops').subscribe({
        next: crops => this.cropCount = (crops || []).filter(c => c.status === 'AVAILABLE').length,
        error: () => this.cropCount = 0
      });

      this.api.get<Order[]>('/orders/dealer/' + uid).subscribe({
        next: list => {
          this.orders = list || [];
          this.totalValue = this.orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
        },
        error: () => this.orders = []
      });
    } else {
      this.api.get<Order[]>('/orders').subscribe({
        next: list => {
          this.orders = list || [];
          this.totalValue = this.orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
        },
        error: () => this.orders = []
      });

      this.api.get<Crop[]>('/crops').subscribe({
        next: crops => this.cropCount = (crops || []).length,
        error: () => this.cropCount = 0
      });
    }

    if (uid) {
      this.api.get<Notification[]>('/notifications/user/' + uid + '/unread').subscribe({
        next: notifs => this.unread = (notifs || []).length,
        error: () => this.unread = 0
      });
    }
  }
}
