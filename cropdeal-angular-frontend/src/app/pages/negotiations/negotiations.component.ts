import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe, DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Negotiation, Offer, Crop } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, CurrencyPipe, DatePipe, FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">PRICE BARGAINING & AGREEMENT</div>
          <h1 class="title">Active Negotiations</h1>
          <p class="sub">
            {{ auth.role() === 'FARMER' 
               ? 'Review offers from dealers on your harvest. Accept, counter, or decline in real time.' 
               : 'Track your offers to farmers. Respond to counter-offers and generate orders once agreed.' }}
          </p>
        </div>
        <div>
          <a *ngIf="auth.role() === 'DEALER'" routerLink="/marketplace" class="btn btn-primary">+ Explore Crops</a>
          <a *ngIf="auth.role() === 'FARMER'" routerLink="/crops" class="btn btn-primary">+ Manage Crops</a>
        </div>
      </div>

      <!-- Tab Bar -->
      <div style="display: flex; gap: 12px; margin-top: 20px; border-bottom: 1px solid var(--line); padding-bottom: 12px;">
        <button class="btn btn-small"
                [style.background]="selectedTab === 'active' ? 'var(--green)' : '#f0f2ef'"
                [style.color]="selectedTab === 'active' ? '#fff' : 'var(--ink)'"
                (click)="selectedTab = 'active'">
          💬 Active Negotiations ({{ activeNegotiations.length }})
        </button>
        <button class="btn btn-small"
                [style.background]="selectedTab === 'history' ? 'var(--green)' : '#f0f2ef'"
                [style.color]="selectedTab === 'history' ? '#fff' : 'var(--ink)'"
                (click)="selectedTab = 'history'">
          📜 Negotiation History ({{ historyNegotiations.length }})
        </button>
      </div>

      <div *ngIf="loading" class="card empty" style="padding: 40px; margin-top: 20px;">
        <div>Loading negotiations…</div>
      </div>

      <!-- Empty State -->
      <div *ngIf="!loading && displayedNegotiations.length === 0" class="card empty" style="padding: 60px 20px; margin-top: 20px;">
        <div style="font-size: 40px; margin-bottom: 10px;">🤝</div>
        <h3>No {{ selectedTab === 'active' ? 'active' : 'historical' }} negotiations found</h3>
        <p class="muted">
          {{ selectedTab === 'active' 
             ? (auth.role() === 'DEALER' 
                ? 'Go to the marketplace and click "Start Negotiation" on any available crop listing.' 
                : 'When dealers make offers on your listed crops, they will appear here.')
             : 'Completed, accepted and closed negotiations will be recorded here.' }}
        </p>
        <a *ngIf="selectedTab === 'active'" [routerLink]="auth.role() === 'DEALER' ? '/marketplace' : '/crops'" class="btn btn-primary" style="margin-top: 10px;">
          {{ auth.role() === 'DEALER' ? 'Browse Marketplace' : 'View My Crops' }}
        </a>
      </div>

      <div class="grid" style="gap: 20px; margin-top: 24px;" *ngIf="!loading && displayedNegotiations.length > 0">
        <div class="card" *ngFor="let n of displayedNegotiations" style="padding: 24px; border-left: 5px solid;"
             [style.border-left-color]="n.status === 'COMPLETED' || n.status === 'ACCEPTED' ? 'var(--green)' : n.status === 'REJECTED' || n.status === 'CANCELLED' ? 'var(--red)' : '#f39c12'">
          
          <div class="split" style="align-items: flex-start;">
            <div>
              <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
                <span style="font-weight: 800; font-size: 13px;">#{{ n.id }}</span>
                <span class="badge" [style.background]="statusBg(n.status)" [style.color]="statusColor(n.status)">
                  {{ n.status }}
                </span>
                <span class="muted" style="font-size: 13px;">Harvest: <b>{{ n.cropName || cropsMap[n.cropId] || 'Crop' }}</b></span>
                <span class="muted" style="font-size: 12px;" *ngIf="n.farmerName && auth.role() !== 'FARMER'">
                  Farmer: <b style="color: var(--ink);">{{ n.farmerName }}</b>
                </span>
                <span class="muted" style="font-size: 12px;" *ngIf="n.dealerName && auth.role() !== 'DEALER'">
                  Dealer: <b style="color: var(--ink);">{{ n.dealerName }}</b>
                </span>
              </div>
              <h2 style="margin: 10px 0 4px; font-size: 22px;">
                Quantity: <b>{{ n.quantity }} quintals</b>
              </h2>
              <div class="muted" style="font-size: 13px;">
                Farmer Asking Price: <b>{{ n.askingPrice | currency:'INR':'symbol':'1.0-0' }} / qtl</b> · 
                {{ n.status === 'ACCEPTED' || n.status === 'COMPLETED' ? 'Final Agreed Price' : 'Current Offer' }}: <b style="color: var(--green); font-size: 16px;">{{ n.currentOffer | currency:'INR':'symbol':'1.0-0' }} / qtl</b>
                <span *ngIf="n.createdAt" style="margin-left: 10px; font-size: 11px;">(Started: {{ n.createdAt | date:'short' }})</span>
              </div>
            </div>

            <!-- Action Buttons based on Role & State -->
            <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px;">
              <!-- When Accepted: Dealer can create Order or View Order; Farmer can view status or View Order -->
              <div *ngIf="n.status === 'ACCEPTED'" style="display: flex; gap: 8px; align-items: center;">
                <ng-container *ngIf="auth.role() === 'DEALER'">
                  <button *ngIf="!n.orderId" class="btn btn-primary" (click)="createOrder(n)">
                    📦 ORDER
                  </button>
                  <a *ngIf="n.orderId" routerLink="/orders" class="btn btn-light">
                    👁 View Order #{{ n.orderNumber || n.orderId }}
                  </a>
                </ng-container>

                <ng-container *ngIf="auth.role() === 'FARMER'">
                  <span *ngIf="!n.orderId" class="badge" style="background: #eef5ee; color: var(--green); padding: 8px 14px; font-size: 13px;">
                    ✓ Price Agreed! Awaiting Dealer Order
                  </span>
                  <a *ngIf="n.orderId" routerLink="/orders" class="btn btn-light">
                    👁 View Order #{{ n.orderNumber || n.orderId }}
                  </a>
                </ng-container>

                <ng-container *ngIf="auth.role() === 'ADMIN'">
                  <a *ngIf="n.orderId" routerLink="/orders" class="btn btn-light">
                    👁 View Order #{{ n.orderNumber || n.orderId }}
                  </a>
                  <span *ngIf="!n.orderId" class="badge" style="background: #eef5ee; color: var(--green); padding: 8px 14px; font-size: 13px;">
                    ✓ Price Agreed
                  </span>
                </ng-container>
              </div>

              <!-- When Completed: Show View Order link if order exists -->
              <div *ngIf="n.status === 'COMPLETED'" style="display: flex; gap: 8px; align-items: center;">
                <span class="badge" style="background: #eef5ee; color: var(--green); padding: 6px 12px; font-size: 12px;">
                  ✓ Completed
                </span>
                <a *ngIf="n.orderId" routerLink="/orders" class="btn btn-light btn-small">
                  👁 View Order #{{ n.orderNumber || n.orderId }}
                </a>
              </div>

              <!-- When Open / Countered: Actions to Accept, Counter, Reject -->
              <div *ngIf="n.status === 'OPEN' || n.status === 'COUNTERED'" style="display: flex; gap: 8px;">
                <!-- Accept button: allowed for either party -->
                <button class="btn btn-primary btn-small" (click)="accept(n.id)">
                  ✓ Accept ({{ n.currentOffer | currency:'INR':'symbol':'1.0-0' }})
                </button>
                <button class="btn btn-light btn-small" (click)="toggleCounter(n.id)">
                  💬 Counter Offer
                </button>
                <button class="btn btn-danger btn-small" (click)="reject(n.id)">
                  ✕ Reject
                </button>
              </div>
            </div>
          </div>

          <!-- Counter Offer Input Form -->
          <div *ngIf="activeCounterId === n.id" class="card form-card" style="margin-top: 18px; background: #fdfdfd; border: 1px dashed var(--green);">
            <div class="field" style="max-width: 320px;">
              <label>Your Proposed Counter Price (₹ / quintal)</label>
              <input type="number" [(ngModel)]="counterAmount" placeholder="e.g. {{ n.currentOffer + 50 }}">
            </div>
            <div style="display: flex; gap: 8px; margin-top: 12px;">
              <button class="btn btn-primary btn-small" (click)="submitCounter(n)">Send Counter Offer</button>
              <button class="btn btn-light btn-small" (click)="activeCounterId = undefined">Cancel</button>
            </div>
          </div>

          <!-- Offer History Timeline -->
          <div style="margin-top: 20px; border-top: 1px solid var(--line); padding-top: 14px;">
            <div class="eyebrow" style="font-size: 11px; margin-bottom: 10px;">COMPLETE CHRONOLOGICAL OFFER TRAIL</div>
            <div style="display: flex; flex-direction: column; gap: 8px;">
              <div *ngFor="let o of n.offers" style="display: flex; justify-content: space-between; align-items: center; padding: 6px 12px; background: #f7f9f7; border-radius: 8px; font-size: 13px;">
                <div>
                  <span class="badge" [style.background]="o.actorRole === 'DEALER' ? '#e8f0fe' : '#eef5ee'" [style.color]="o.actorRole === 'DEALER' ? '#1a73e8' : 'var(--green)'">
                    {{ o.actorRole }}
                  </span>
                  <span style="margin-left: 10px; font-weight: 600;">{{ o.amount | currency:'INR':'symbol':'1.0-0' }} / qtl</span>
                  <span class="muted" style="margin-left: 8px; font-size: 11px;">({{ o.status }})</span>
                </div>
                <span class="muted" style="font-size: 11px;">{{ o.createdAt | date:'short' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class NegotiationsComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);
  router = inject(Router);

  negotiations: Negotiation[] = [];
  selectedTab: 'active' | 'history' = 'active';
  loading = true;
  activeCounterId?: number;
  counterAmount = 0;

  get activeNegotiations(): Negotiation[] {
    return this.negotiations.filter(n => n.status === 'OPEN' || n.status === 'COUNTERED' || n.status === 'ACCEPTED');
  }

  get historyNegotiations(): Negotiation[] {
    return this.negotiations.filter(n => n.status === 'COMPLETED' || n.status === 'REJECTED' || n.status === 'CANCELLED' || n.status === 'EXPIRED');
  }

  get displayedNegotiations(): Negotiation[] {
    return this.selectedTab === 'active' ? this.activeNegotiations : this.historyNegotiations;
  }

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

  cropsMap: Record<number, string> = {};

  load() {
    this.loading = true;
    const uid = this.auth.userId();
    const role = this.auth.role();

    let path = '/negotiations';
    if (role === 'FARMER' && uid) {
      path = '/negotiations/farmer/' + uid;
    } else if (role === 'DEALER' && uid) {
      path = '/negotiations/dealer/' + uid;
    }

    this.api.get<Negotiation[]>(path).subscribe({
      next: (list: Negotiation[]) => {
        this.negotiations = list || [];
        this.loading = false;
        this.fetchCropNames(this.negotiations);
      },
      error: () => {
        this.negotiations = [];
        this.loading = false;
      }
    });
  }

  fetchCropNames(negotiations: Negotiation[]) {
    const uniqueCropIds = [...new Set(negotiations.map(n => n.cropId).filter(Boolean))];
    uniqueCropIds.forEach(id => {
      if (!this.cropsMap[id]) {
        this.api.get<Crop>('/crops/' + id).subscribe({
          next: (c) => {
            this.cropsMap[id] = c.cropName || 'Crop';
          },
          error: () => {
            this.cropsMap[id] = 'Deleted Crop';
          }
        });
      }
    });
  }

  statusBg(status: string): string {
    switch (status) {
      case 'COMPLETED':
      case 'ACCEPTED': return '#eef5ee';
      case 'CANCELLED':
      case 'REJECTED': return '#ffebee';
      case 'COUNTERED': return '#fff8e1';
      default: return '#e8f0fe';
    }
  }

  statusColor(status: string): string {
    switch (status) {
      case 'COMPLETED':
      case 'ACCEPTED': return 'var(--green)';
      case 'CANCELLED':
      case 'REJECTED': return 'var(--red)';
      case 'COUNTERED': return '#b78103';
      default: return '#1a73e8';
    }
  }

  toggleCounter(id: number) {
    if (this.activeCounterId === id) {
      this.activeCounterId = undefined;
    } else {
      this.activeCounterId = id;
      const n = this.negotiations.find(x => x.id === id);
      this.counterAmount = n ? n.currentOffer : 0;
    }
  }

  submitCounter(n: Negotiation) {
    if (!this.counterAmount || this.counterAmount <= 0) {
      this.toast.show('Please enter a valid counter amount', 'error');
      return;
    }

    const role = this.auth.role();
    const isFarmer = role === 'FARMER';
    const path = isFarmer ? `/negotiations/${n.id}/counter` : `/negotiations/${n.id}/offers`;

    this.api.post<Negotiation>(path, { amount: Number(this.counterAmount) }).subscribe({
      next: () => {
        this.toast.show('Counter offer sent successfully');
        this.activeCounterId = undefined;
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not send counter offer', 'error');
      }
    });
  }

  accept(id: number) {
    this.api.post<Negotiation>(`/negotiations/${id}/accept`, {}).subscribe({
      next: () => {
        this.toast.show('Negotiation accepted! Final agreed price locked.');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Accept failed', 'error');
      }
    });
  }

  reject(id: number) {
    if (!confirm('Are you sure you want to reject this negotiation?')) return;
    this.api.post<Negotiation>(`/negotiations/${id}/reject`, {}).subscribe({
      next: () => {
        this.toast.show('Negotiation rejected');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Reject failed', 'error');
      }
    });
  }

  createOrder(n: Negotiation) {
    this.api.post<any>('/orders/from-negotiation/' + n.id, {}).subscribe({
      next: (order: any) => {
        this.toast.show('Order created from accepted negotiation! Proceed to payment.');
        this.router.navigateByUrl('/orders');
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not create order from negotiation', 'error');
      }
    });
  }
}
