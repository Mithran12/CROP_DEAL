import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Crop, User } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { getCropImageUrl } from '../../core/crop-images';

@Component({
  standalone: true,
  imports: [NgFor, NgIf, CurrencyPipe, FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">FARMER CONSOLE</div>
          <h1 class="title">My Crop Listings</h1>
          <p class="sub">Publish your harvest directly to dealers across the network with guaranteed fair prices.</p>
        </div>
        <div style="display: flex; gap: 10px;">
          <a routerLink="/marketplace" class="btn btn-light">View Marketplace</a>
          <button class="btn btn-primary" (click)="toggleForm()">
            {{ showForm ? 'Close Form' : '+ Publish Crop' }}
          </button>
        </div>
      </div>

      <!-- Warning if profile is incomplete -->
      <div *ngIf="!auth.userId()" class="card" style="padding: 16px 20px; margin: 18px 0; background: #fff8e1; border: 1px solid #ffe082; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <b style="color: #b78103;">Profile required to publish crops</b>
          <div style="font-size: 13px; color: #7f5c09;">Please set up your profile name, location and settlement details first.</div>
        </div>
        <a routerLink="/profile" class="btn btn-primary btn-small">Complete Profile</a>
      </div>

      <!-- Crop Form Modal / Card -->
      <div *ngIf="showForm" class="card form-card" style="margin: 24px 0; animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 14px;">
          <h2 style="margin: 0;">{{ editing ? 'Update Crop Listing #' + editId : 'Publish New Crop Harvest' }}</h2>
          <span class="badge">{{ form.grade }} Grade</span>
        </div>
        
        <form (ngSubmit)="save()">
          <div class="form-grid">
            <div class="field">
              <label>Crop Category *</label>
              <select [(ngModel)]="form.category" name="category" required>
                <option value="GRAIN">Grains</option>
                <option value="VEGETABLE">Vegetables</option>
                <option value="FRUIT">Fruits</option>
                <option value="PULSES">Pulses & Lentils</option>
                <option value="OILSEEDS">Oilseeds</option>
                <option value="SPICES">Spices</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div class="field">
              <label>Crop Name *</label>
              <input [(ngModel)]="form.cropName" name="cropName" (blur)="checkMarketRate()" required minlength="2" placeholder="e.g. Sona Masoori Rice, Red Tomato">
            </div>
            <div class="field">
              <label>Quality Grade *</label>
              <select [(ngModel)]="form.grade" name="grade" (change)="checkMarketRate()" required>
                <option value="A">Grade A (Premium Export Quality)</option>
                <option value="B">Grade B (Standard Market Quality)</option>
                <option value="C">Grade C (Processing Quality)</option>
              </select>
            </div>
            <div class="field">
              <label>Available Quantity (Quintals) *</label>
              <input type="number" [(ngModel)]="form.quantity" name="quantity" required min="1" step="0.5" placeholder="e.g. 50">
            </div>
            <div class="field">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <label>Expected Price (₹ per Quintal) *</label>
                <span *ngIf="recommendedRate" class="badge" style="background: #eef5ee; color: var(--green); cursor: pointer;" (click)="applyRecommendedRate()" title="Click to apply benchmark MSP price">
                  💡 Mandi Rate: ₹{{ recommendedRate | currency:'INR':'symbol':'1.0-0' }} (Apply)
                </span>
              </div>
              <input type="number" [(ngModel)]="form.expectedPrice" name="expectedPrice" required min="10" placeholder="e.g. 3500">
            </div>
            <div class="field">
              <label>Farm / Mandi Location *</label>
              <input [(ngModel)]="form.location" name="location" (blur)="checkMarketRate()" required placeholder="e.g. Karur, Tamil Nadu">
            </div>
            <div class="field" style="grid-column: 1 / -1;">
              <label>Crop Image (Optional — JPG, JPEG, PNG, max 2MB)</label>
              <div style="display: flex; gap: 14px; align-items: center; flex-wrap: wrap;">
                <input type="file" accept="image/png, image/jpeg, image/jpg" (change)="onImageSelected($event)" style="padding: 6px;">
                <button *ngIf="imagePreview" type="button" class="btn btn-light btn-small" (click)="clearImage()">Remove Image</button>
              </div>
              <div *ngIf="imagePreview" style="margin-top: 10px; display: flex; align-items: center; gap: 12px;">
                <img [src]="imagePreview" alt="Crop Preview" style="width: 70px; height: 70px; object-fit: cover; border-radius: 8px; border: 2px solid var(--green);">
                <span class="muted" style="font-size: 13px;">Uploaded image preview (Takes priority over default category image)</span>
              </div>
            </div>
          </div>

          <div style="display: flex; gap: 12px; margin-top: 22px;">
            <button type="submit" class="btn btn-primary" [disabled]="submitting">
              {{ submitting ? 'Saving Listing…' : (editing ? 'Update Listing' : 'Publish to Marketplace') }}
            </button>
            <button type="button" class="btn btn-light" (click)="reset()">Cancel</button>
          </div>
        </form>
      </div>

      <!-- Crops Table -->
      <div class="card table-wrap" style="margin-top: 20px;">
        <table class="table" *ngIf="crops.length; else empty">
          <thead>
            <tr>
              <th>Crop Name & Details</th>
              <th>Category</th>
              <th>Grade</th>
              <th>Quantity</th>
              <th>Expected Price</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let c of crops">
              <td>
                <div style="display: flex; align-items: center; gap: 10px;">
                  <img [src]="c.imageUrl || getCropImageUrl(c.cropName, c.category)" [alt]="c.cropName" style="width: 40px; height: 40px; border-radius: 8px; object-fit: cover; background: #eee;">
                  <span *ngIf="!c.imageUrl" style="font-size: 20px;">{{ emoji(c.cropName) }}</span>
                  <div>
                    <b>{{ c.cropName }}</b>
                    <div class="muted" style="font-size: 12px;">📍 {{ c.location }}</div>
                  </div>
                </div>
              </td>
              <td><span class="badge">{{ c.category }}</span></td>
              <td><span class="badge" style="background: #fdfae5; color: #8a6a12;">Grade {{ c.grade }}</span></td>
              <td><b>{{ c.quantity }}</b> qtl</td>
              <td><b style="color: var(--green);">{{ c.expectedPrice | currency:'INR':'symbol':'1.0-0' }}</b> / qtl</td>
              <td>
                <span class="badge" [style.background]="c.status === 'AVAILABLE' ? '#eef5ee' : '#ffebee'" [style.color]="c.status === 'AVAILABLE' ? 'var(--green)' : 'var(--red)'">
                  {{ c.status }}
                </span>
              </td>
              <td>
                <div style="display: flex; gap: 6px;">
                  <button class="btn btn-light btn-small" (click)="edit(c)">Edit</button>
                  <button class="btn btn-danger btn-small" (click)="remove(c.id)">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <ng-template #empty>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 48px; margin-bottom: 12px;">🌾</div>
            <h3 style="margin: 0 0 8px;">No crops listed yet</h3>
            <p class="muted" style="margin: 0 0 16px;">Publish your current harvest to start receiving negotiation offers from verified dealers.</p>
            <button class="btn btn-primary" (click)="showForm = true" [disabled]="!auth.userId()">+ Publish Your First Crop</button>
          </div>
        </ng-template>
      </div>
    </div>
  `
})
export class CropsComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  crops: Crop[] = [];
  showForm = false;
  editing = false;
  editId?: number;
  submitting = false;
  recommendedRate: number | null = null;
  imagePreview = '';

  form: any = {
    category: 'GRAIN',
    cropName: '',
    grade: 'A',
    quantity: 50,
    expectedPrice: 3200,
    location: '',
    imageUrl: ''
  };

  onImageSelected(event: any) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      this.toast.show('Please upload a valid JPG or PNG image', 'error');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.toast.show('Image size must be less than 2MB', 'error');
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result as string;
      this.form.imageUrl = this.imagePreview;
    };
    reader.readAsDataURL(file);
  }

  clearImage() {
    this.imagePreview = '';
    this.form.imageUrl = '';
  }

  ngOnInit() {
    this.ensureUserAndLoad();
  }

  ensureUserAndLoad() {
    if (!this.auth.userId()) {
      this.auth.loadProfile().subscribe((u: User | null) => {
        if (u && u.id) {
          if (!this.form.location && u.city) {
            this.form.location = u.city + (u.state ? ', ' + u.state : '');
          }
          this.load(u.id);
        }
      });
    } else {
      this.load(this.auth.userId());
    }
  }

  load(farmerId: number) {
    if (!farmerId) return;
    this.api.get<Crop[]>('/crops/farmer/' + farmerId).subscribe({
      next: (list: Crop[]) => {
        this.crops = list || [];
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not load your crops', 'error');
      }
    });
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.reset();
    }
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
    if (s.includes('cotton')) return '🌱';
    return '🥬';
  }

  getCropImageUrl = getCropImageUrl;

  save() {
    const farmerId = this.auth.userId();
    if (!farmerId) {
      this.toast.show('Please complete your profile first before publishing crops', 'error');
      return;
    }

    if (!this.form.cropName || this.form.cropName.trim().length < 2) {
      this.toast.show('Crop name must be at least 2 characters', 'error');
      return;
    }
    if (!this.form.quantity || this.form.quantity <= 0) {
      this.toast.show('Quantity must be greater than 0', 'error');
      return;
    }

    // Grain-specific validation
    const catUpper = (this.form.category || '').toUpperCase();
    if (catUpper === 'GRAIN' || catUpper === 'GRAINS') {
      if (this.form.quantity < 1) {
        this.toast.show('Grain crops must have a minimum quantity of 1 quintal', 'error');
        return;
      }
      if (this.form.expectedPrice && this.form.expectedPrice < 100) {
        this.toast.show('Expected price for grain crops must be at least ₹100 per quintal', 'error');
        return;
      }
    }

    if (!this.form.expectedPrice || this.form.expectedPrice <= 0) {
      this.toast.show('Expected price must be greater than 0', 'error');
      return;
    }
    if (!this.form.location || !this.form.location.trim()) {
      this.toast.show('Location is required', 'error');
      return;
    }

    const payload: any = {
      farmerId,
      category: this.form.category,
      cropName: this.form.cropName.trim(),
      grade: this.form.grade,
      quantity: Number(this.form.quantity),
      expectedPrice: Number(this.form.expectedPrice),
      location: this.form.location.trim(),
      imageUrl: this.form.imageUrl || undefined
    };

    this.submitting = true;

    const req$ = this.editing
      ? this.api.put<Crop>('/crops/' + this.editId, payload)
      : this.api.post<Crop>('/crops', payload);

    req$.subscribe({
      next: (saved: Crop) => {
        this.submitting = false;
        this.toast.show(this.editing ? 'Crop listing updated successfully' : 'Crop published to marketplace successfully!');
        this.reset();
        this.load(farmerId);
      },
      error: (e: any) => {
        this.submitting = false;
        this.toast.show(e.error?.message || 'Failed to save crop listing', 'error');
      }
    });
  }

  edit(c: Crop) {
    this.editing = true;
    this.editId = c.id;
    this.imagePreview = c.imageUrl || '';
    this.form = {
      category: c.category,
      cropName: c.cropName,
      grade: c.grade,
      quantity: c.quantity,
      expectedPrice: c.expectedPrice,
      location: c.location,
      imageUrl: c.imageUrl || ''
    };
    this.showForm = true;
  }

  remove(id: number) {
    if (!confirm('Are you sure you want to delete this crop listing?')) return;
    this.api.delete('/crops/' + id).subscribe({
      next: () => {
        this.toast.show('Listing deleted');
        this.load(this.auth.userId());
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Delete failed', 'error');
      }
    });
  }

  reset() {
    this.showForm = false;
    this.editing = false;
    this.editId = undefined;
    this.clearImage();
    this.form = {
      category: 'GRAIN',
      cropName: '',
      grade: 'A',
      quantity: 50,
      expectedPrice: 3200,
      location: '',
      imageUrl: ''
    };
    this.recommendedRate = null;
  }

  checkMarketRate() {
    if (!this.form.cropName || this.form.cropName.trim().length < 2) return;
    const name = this.form.cropName.trim().toLowerCase();
    
    // Extract base crop name (e.g. "Sona Masoori Rice" -> "Rice", "Organic Red Tomato" -> "Tomato")
    let baseCrop = this.form.cropName.trim();
    if (name.includes('rice')) baseCrop = 'Rice';
    else if (name.includes('wheat')) baseCrop = 'Wheat';
    else if (name.includes('tomato')) baseCrop = 'Tomato';
    else if (name.includes('onion')) baseCrop = 'Onion';
    else if (name.includes('potato')) baseCrop = 'Potato';
    else if (name.includes('corn')) baseCrop = 'Corn';

    this.api.get<any[]>('/pricing/market-rates/' + baseCrop).subscribe({
      next: (rates) => {
        if (rates && rates.length > 0) {
          // If location matches, use specific location rate, otherwise use first
          const locMatch = rates.find(r => this.form.location && r.location && 
            this.form.location.toLowerCase().includes(r.location.toLowerCase()));
          this.recommendedRate = locMatch ? locMatch.marketPrice : rates[0].marketPrice;
        } else {
          this.recommendedRate = null;
        }
      },
      error: () => this.recommendedRate = null
    });
  }

  applyRecommendedRate() {
    if (this.recommendedRate) {
      this.form.expectedPrice = this.recommendedRate;
      this.toast.show('Applied Mandi MSP Rate: ₹' + this.recommendedRate + ' / quintal');
    }
  }
}
