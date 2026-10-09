import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Crop } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { getCropImageUrl } from '../../core/crop-images';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, CurrencyPipe, FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">ACTIVE MARKETPLACE</div>
          <h1 class="title">Fresh Crops, Direct from Farms</h1>
          <p class="sub">Discover verified harvests by crop, category, location, and quality grade with direct farm-to-dealer pricing.</p>
        </div>
        <div style="display: flex; gap: 10px;">
          <a *ngIf="auth.role() === 'FARMER'" routerLink="/crops" class="btn btn-primary">+ Publish Crop</a>
          <a *ngIf="auth.role() === 'DEALER'" routerLink="/subscriptions" class="btn btn-light">🔔 My Subscriptions</a>
          <a routerLink="/negotiations" class="btn btn-light">💬 Active Negotiations</a>
        </div>
      </div>

      <!-- Search & Advanced Filter Card -->
      <div class="card form-card" style="margin: 24px 0;">
        <div class="form-grid" style="grid-template-columns: repeat(4, 1fr);">
          <div class="field">
            <label>Search Crop Name</label>
            <input [(ngModel)]="search" placeholder="e.g. Tomato, Rice, Wheat…">
          </div>
          <div class="field">
            <label>Category</label>
            <select [(ngModel)]="category">
              <option value="">All Categories</option>
              <option value="GRAIN">Grains</option>
              <option value="VEGETABLE">Vegetables</option>
              <option value="FRUIT">Fruits</option>
              <option value="PULSES">Pulses</option>
              <option value="OILSEEDS">Oilseeds</option>
              <option value="SPICES">Spices</option>
            </select>
          </div>
          <div class="field">
            <label>Quality Grade</label>
            <select [(ngModel)]="grade">
              <option value="">All Grades</option>
              <option value="A">Grade A (Premium)</option>
              <option value="B">Grade B (Standard)</option>
              <option value="C">Grade C</option>
            </select>
          </div>
          <div class="field">
            <label>Farm / Mandi Location</label>
            <input [(ngModel)]="location" placeholder="e.g. Coimbatore, Karur, Punjab">
          </div>
        </div>
        <div style="display: flex; gap: 10px; margin-top: 16px;">
          <button class="btn btn-primary" (click)="load()">Search Marketplace</button>
          <button class="btn btn-light" (click)="resetFilters()">Clear Filters</button>
        </div>
      </div>

      <!-- Crops Grid with Local Replaceable Assets -->
      <div class="grid grid-3" *ngIf="crops.length; else noCrops">
        <article class="card crop-card" *ngFor="let c of crops" style="overflow: hidden; display: flex; flex-direction: column;">
          <div class="crop-photo" style="overflow: hidden; height: 160px; background: #f4f7f2; display: flex; align-items: center; justify-content: center; position: relative;">
            <img [src]="getCropImage(c)" [alt]="c.cropName" style="width: 100%; height: 100%; object-fit: cover;">
            <span style="position: absolute; top: 10px; right: 10px; font-size: 18px; background: rgba(255,255,255,0.9); border-radius: 50%; width: 34px; height: 34px; display: grid; place-items: center; box-shadow: 0 2px 6px rgba(0,0,0,0.12);">
              {{ emoji(c.cropName) }}
            </span>
          </div>

          <div class="crop-body" style="display: flex; flex-direction: column; flex: 1;">
            <div class="split">
              <span class="badge">{{ c.category }}</span>
              <span class="badge" style="background: #fdfae5; color: #8a6a12;">Grade {{ c.grade }}</span>
            </div>
            <h3 style="margin: 12px 0 4px;">{{ c.cropName }}</h3>
            <div class="muted" style="font-size: 13px;">📍 {{ c.location }}</div>
            
            <div class="split" style="margin-top: auto; padding-top: 14px; border-top: 1px solid var(--line);">
              <div>
                <div class="price">{{ c.expectedPrice | currency:'INR':'symbol':'1.0-0' }} <span style="font-size: 13px; font-weight: normal; color: var(--muted);">/ qtl</span></div>
                <small class="muted">Available: <b>{{ c.quantity }}</b> qtl</small>
              </div>

              <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                <button
                  *ngIf="auth.role() === 'DEALER'"
                  class="btn btn-primary btn-small"
                  [attr.data-crop-id]="c.id"
                  (click)="placeDirectOrder(c)"
                  [disabled]="placingOrderCropId === c.id || orderedCropIds.has(c.id)">
                  {{ placingOrderCropId === c.id ? 'Placing…' : (orderedCropIds.has(c.id) ? 'Ordered ✓' : 'Order') }}
                </button>

                <button
                  *ngIf="auth.role() === 'DEALER'"
                  class="btn btn-light btn-small"
                  (click)="selectForNegotiation(c)">
                  Start Negotiation
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>

      <ng-template #noCrops>
        <div class="card empty" style="padding: 60px 20px;">
          <div style="font-size: 40px; margin-bottom: 10px;">🔍</div>
          <h3>No crops matched your filters</h3>
          <p class="muted">Try adjusting your crop name, category or location search.</p>
        </div>
      </ng-template>

      <!-- Negotiation Drawer / Modal -->
      <div *ngIf="selected" class="card form-card" style="margin-top: 28px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 12px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">DIRECT DEALER NEGOTIATION</div>
            <h2 style="margin: 6px 0;">Offer for {{ selected.cropName }}</h2>
            <p class="sub" style="margin-bottom: 0;">Asking Price: <b>{{ selected.expectedPrice | currency:'INR':'symbol':'1.0-0' }} / qtl</b> · Available: <b>{{ selected.quantity }} qtl</b></p>
          </div>
          <button class="btn btn-light btn-small" (click)="selected = undefined">✕ Close</button>
        </div>

        <div class="form-grid" style="grid-template-columns: 1fr 1fr; margin-top: 18px;">
          <div class="field">
            <label>Purchase Quantity (Quintals) *</label>
            <input type="number" [(ngModel)]="negotiateQuantity" min="1" [max]="selected.quantity" placeholder="e.g. 10">
            <small class="muted">Max available: {{ selected.quantity }} quintals</small>
          </div>
          <div class="field">
            <label>Your Opening Offer (₹ / qtl) *</label>
            <input type="number" [(ngModel)]="negotiateAmount" [max]="selected.expectedPrice - 1" placeholder="e.g. {{ selected.expectedPrice - 100 }}">
            <small class="muted" style="color: #6a7870;">Must be below farmer's asking price of {{ selected.expectedPrice | currency:'INR':'symbol':'1.0-0' }}</small>
          </div>
        </div>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
          <button class="btn btn-primary" (click)="startNegotiation()" [disabled]="starting">
            {{ starting ? 'Submitting Offer…' : 'Send Opening Offer to Farmer' }}
          </button>
          <button class="btn btn-light" (click)="selected = undefined">Cancel</button>
        </div>
      </div>
    </div>
  `
})
export class MarketplaceComponent implements OnInit {
  placingOrderCropId: number | null = null;
  orderedCropIds: Set<number> = new Set<number>();
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);
  router = inject(Router);

  crops: Crop[] = [];
  search = '';
  category = '';
  grade = '';
  location = '';

  selected?: Crop;
  negotiateQuantity = 10;
  negotiateAmount = 0;
  starting = false;

  ngOnInit() {
    this.ensureUser();
    this.load();
  }

  ensureUser() {
    if (!this.auth.userId()) {
      this.auth.loadProfile().subscribe();
    }
  }

  load() {
    const params: Record<string, string | number | undefined> = {
      status: 'AVAILABLE'
    };
    if (this.search) params['name'] = this.search.trim();
    if (this.category) params['category'] = this.category;
    if (this.grade) params['grade'] = this.grade;
    if (this.location) params['location'] = this.location.trim();

    this.api.get<Crop[]>('/crops/filter/advanced', params).subscribe({
      next: (list: Crop[]) => {
        this.crops = list || [];
      },
      error: () => {
        this.api.get<Crop[]>('/crops').subscribe({
          next: (all: Crop[]) => {
            this.crops = (all || []).filter(c => {
              const matchesStatus = c.status === 'AVAILABLE' || c.status === 'PUBLISHED';
              const matchesName = !this.search || c.cropName.toLowerCase().includes(this.search.toLowerCase());
              const matchesCategory = !this.category || c.category === this.category;
              const matchesGrade = !this.grade || c.grade === this.grade;
              const matchesLoc = !this.location || c.location.toLowerCase().includes(this.location.toLowerCase());
              return matchesStatus && matchesName && matchesCategory && matchesGrade && matchesLoc;
            });
          }
        });
      }
    });
  }

  resetFilters() {
    this.search = '';
    this.category = '';
    this.grade = '';
    this.location = '';
    this.load();
  }

  getCropImage(c: Crop): string {
    if (c.imageUrl && c.imageUrl.trim()) {
      return c.imageUrl;
    }
    return getCropImageUrl(c.cropName, c.category);
  }

  emoji(n: string = ''): string {
    const s = n.toLowerCase();
    if (s.includes('rice') || s.includes('paddy')) return '🌾';
    if (s.includes('wheat')) return '🌾';
    if (s.includes('tomato')) return '🍅';
    if (s.includes('onion')) return '🧅';
    if (s.includes('potato')) return '🥔';
    if (s.includes('mango')) return '🥭';
    if (s.includes('banana')) return '🍌';
    if (s.includes('chilli') || s.includes('pepper')) return '🌶️';
    if (s.includes('corn') || s.includes('maize')) return '🌽';
    return '🥬';
  }

  selectForNegotiation(c: Crop) {
    if (!this.auth.userId()) {
      this.toast.show('Please complete your dealer profile before starting negotiations', 'error');
      this.router.navigateByUrl('/profile');
      return;
    }
    this.selected = c;
    this.negotiateQuantity = Math.min(10, c.quantity || 10);
    this.negotiateAmount = Math.max(1, Math.round(c.expectedPrice * 0.9));
  }

  startNegotiation() {
    if (!this.selected) return;

    const dealerId = this.auth.userId();
    if (!dealerId) {
      this.toast.show('Please complete your profile before starting a negotiation', 'error');
      this.router.navigateByUrl('/profile');
      return;
    }

    if (!this.negotiateQuantity || this.negotiateQuantity <= 0) {
      this.toast.show('Quantity must be greater than 0', 'error');
      return;
    }

    if (this.negotiateQuantity > this.selected.quantity) {
      this.toast.show('Requested quantity exceeds available crop quantity (' + this.selected.quantity + ')', 'error');
      return;
    }

    if (!this.negotiateAmount || this.negotiateAmount <= 0) {
      this.toast.show('Offer amount must be greater than 0', 'error');
      return;
    }

    if (this.negotiateAmount >= this.selected.expectedPrice) {
      this.toast.show('Dealer opening offer must be below farmer asking price of ' + this.selected.expectedPrice, 'error');
      return;
    }

    const payload = {
      cropId: this.selected.id,
      quantity: Number(this.negotiateQuantity),
      dealerId: dealerId,
      amount: Number(this.negotiateAmount)
    };

    this.starting = true;

    this.api.post<any>('/negotiations', payload).subscribe({
      next: () => {
        this.starting = false;
        this.toast.show('Negotiation started! Offer of ₹' + this.negotiateAmount + ' sent to farmer.');
        this.selected = undefined;
        this.router.navigateByUrl('/negotiations');
      },
      error: (e: any) => {
        this.starting = false;
        this.toast.show(e.error?.message || 'Could not start negotiation', 'error');
      }
    });
  }

  placeDirectOrder(c: Crop) {
    const dealerId = this.auth.userId();

    if (!dealerId) {
      this.toast.show(
        'Please complete your dealer profile before placing an order',
        'error'
      );
      this.router.navigateByUrl('/profile');
      return;
    }

    if (!c.quantity || c.quantity <= 0) {
      this.toast.show('Crop quantity is not available', 'error');
      return;
    }

    if (!c.expectedPrice || c.expectedPrice <= 0) {
      this.toast.show('Crop price is not available', 'error');
      return;
    }

    const payload = {
      dealerId: dealerId,
      farmerId: c.farmerId,
      cropId: c.id,
      quantity: Number(c.quantity),
      agreedPrice: Number(c.expectedPrice)
    };

    this.placingOrderCropId = c.id;

    this.api.post<any>('/orders', payload).subscribe({
      next: (order: any) => {
        this.placingOrderCropId = null;
        this.orderedCropIds.add(c.id);
        c.status = 'PENDING_ACCEPTANCE';
        this.toast.show(
          `Order placed for ${c.cropName}! Waiting for farmer acceptance.`
        );
      },
      error: (e: any) => {
        this.placingOrderCropId = null;
        this.toast.show(
          e.error?.message || 'Could not place order',
          'error'
        );
      }
    });
  }
}
