import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Subscription } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, CurrencyPipe, FormsModule],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">AUTOMATED HARVEST ALERTS</div>
          <h1 class="title">Crop Subscriptions</h1>
          <p class="sub">Get instant RabbitMQ notifications when farmers list crops that match your required category, location, and price limits.</p>
        </div>
        <button class="btn btn-primary" (click)="showForm = !showForm">
          {{ showForm ? 'Close Form' : '+ New Subscription' }}
        </button>
      </div>

      <!-- Add Subscription Form -->
      <div *ngIf="showForm" class="card form-card" style="margin: 24px 0; border: 2px solid var(--green); animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 12px;">
          <h2 style="margin: 0;">Create Harvest Match Alert</h2>
          <span class="badge">RabbitMQ Powered</span>
        </div>

        <form (ngSubmit)="subscribe()">
          <div class="form-grid">
            <div class="field">
              <label>Crop Category *</label>
              <select [(ngModel)]="form.category" name="category" required>
                <option value="GRAIN">Grains (Rice, Wheat, Corn)</option>
                <option value="VEGETABLE">Vegetables (Tomato, Onion, Potato)</option>
                <option value="FRUIT">Fruits (Mango, Banana)</option>
                <option value="PULSES">Pulses & Lentils</option>
                <option value="OILSEEDS">Oilseeds</option>
                <option value="SPICES">Spices</option>
              </select>
            </div>
            <div class="field">
              <label>Target Crop Name *</label>
              <input [(ngModel)]="form.cropName" name="cropName" required placeholder="e.g. Rice, Tomato, Onion">
            </div>
            <div class="field">
              <label>Preferred Farm Location / Mandi *</label>
              <input [(ngModel)]="form.location" name="location" required placeholder="e.g. Coimbatore, Karur, Punjab">
            </div>
            <div class="field">
              <label>Minimum Batch Quantity (Quintals) *</label>
              <input type="number" [(ngModel)]="form.minimumQuantity" name="minimumQuantity" required min="1" placeholder="e.g. 20">
            </div>
            <div class="field">
              <label>Maximum Price Willing to Pay (₹ / qtl) *</label>
              <input type="number" [(ngModel)]="form.maximumPrice" name="maximumPrice" required min="10" placeholder="e.g. 3500">
            </div>
          </div>

          <div style="display: flex; gap: 12px; margin-top: 20px;">
            <button type="submit" class="btn btn-primary" [disabled]="submitting">
              {{ submitting ? 'Subscribing…' : 'Activate Subscription Alert' }}
            </button>
            <button type="button" class="btn btn-light" (click)="showForm = false">Cancel</button>
          </div>
        </form>
      </div>

      <!-- Subscriptions List -->
      <div class="card table-wrap" style="margin-top: 20px;">
        <table class="table" *ngIf="subscriptions.length; else empty">
          <thead>
            <tr>
              <th>Target Crop</th>
              <th>Category</th>
              <th>Target Location</th>
              <th>Min Quantity</th>
              <th>Max Price Limit</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of subscriptions">
              <td><b>{{ s.cropName }}</b></td>
              <td><span class="badge">{{ s.category }}</span></td>
              <td>📍 {{ s.location }}</td>
              <td>{{ s.minimumQuantity }} qtl</td>
              <td><b style="color: var(--green);">{{ s.maximumPrice | currency:'INR':'symbol':'1.0-0' }}</b> / qtl</td>
              <td>
                <span class="badge" [style.background]="s.active ? '#eef5ee' : '#ffebee'" [style.color]="s.active ? 'var(--green)' : 'var(--red)'">
                  {{ s.active ? 'Active Alerts' : 'Paused' }}
                </span>
              </td>
              <td>
                <div style="display: flex; gap: 6px;">
                  <button class="btn btn-light btn-small" (click)="toggleActive(s)">
                    {{ s.active ? 'Pause' : 'Activate' }}
                  </button>
                  <button class="btn btn-danger btn-small" (click)="deleteSub(s.id!)">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #empty>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 40px; margin-bottom: 10px;">🔔</div>
            <h3>No active subscriptions</h3>
            <p class="muted">Subscribe to crops of interest to get alerted the instant a farmer publishes a matching harvest.</p>
            <button class="btn btn-primary" (click)="showForm = true">+ Create Alert</button>
          </div>
        </ng-template>
      </div>
    </div>
  `
})
export class SubscriptionsComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  subscriptions: Subscription[] = [];
  showForm = false;
  submitting = false;

  form: any = {
    category: 'GRAIN',
    cropName: 'Rice',
    location: '',
    minimumQuantity: 10,
    maximumPrice: 3500
  };

  ngOnInit() {
    this.ensureUserAndLoad();
  }

  ensureUserAndLoad() {
    if (!this.auth.userId()) {
      this.auth.loadProfile().subscribe(() => this.load());
    } else {
      this.load();
    }
  }

  load() {
    const dealerId = this.auth.userId();
    if (!dealerId) return;

    this.api.get<Subscription[]>('/subscriptions/dealer/' + dealerId).subscribe({
      next: (list: Subscription[]) => {
        this.subscriptions = list || [];
      },
      error: () => {
        this.subscriptions = [];
      }
    });
  }

  subscribe() {
    const dealerId = this.auth.userId();
    if (!dealerId) {
      this.toast.show('Please complete your dealer profile first', 'error');
      return;
    }

    if (!this.form.cropName?.trim()) {
      this.toast.show('Crop name is required', 'error');
      return;
    }
    if (!this.form.location?.trim()) {
      this.toast.show('Location is required', 'error');
      return;
    }
    if (!this.form.minimumQuantity || this.form.minimumQuantity <= 0) {
      this.toast.show('Minimum quantity must be greater than 0', 'error');
      return;
    }
    if (!this.form.maximumPrice || this.form.maximumPrice <= 0) {
      this.toast.show('Maximum price must be greater than 0', 'error');
      return;
    }

    const payload = {
      dealerId,
      cropName: this.form.cropName.trim(),
      category: this.form.category,
      location: this.form.location.trim(),
      minimumQuantity: Number(this.form.minimumQuantity),
      maximumPrice: Number(this.form.maximumPrice)
    };

    this.submitting = true;
    this.api.post<Subscription>('/subscriptions', payload).subscribe({
      next: () => {
        this.submitting = false;
        this.toast.show('Subscription created! You will be notified on matching harvest listings.');
        this.showForm = false;
        this.load();
      },
      error: (e: any) => {
        this.submitting = false;
        this.toast.show(e.error?.message || 'Could not create subscription', 'error');
      }
    });
  }

  toggleActive(s: Subscription) {
    if (!s.id) return;
    const path = s.active ? `/subscriptions/${s.id}/deactivate` : `/subscriptions/${s.id}/activate`;
    this.api.patch(path, {}).subscribe({
      next: () => {
        this.toast.show(s.active ? 'Subscription paused' : 'Subscription activated');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not update subscription', 'error');
      }
    });
  }

  deleteSub(id: number) {
    if (!confirm('Delete this subscription?')) return;
    this.api.delete('/subscriptions/' + id).subscribe({
      next: () => {
        this.toast.show('Subscription removed');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not delete subscription', 'error');
      }
    });
  }
}
