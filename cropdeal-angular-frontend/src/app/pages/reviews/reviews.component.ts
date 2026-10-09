import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Review, Order } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, DatePipe, FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">REPUTATION & FEEDBACK</div>
          <h1 class="title">Reviews & Ratings</h1>
          <p class="sub">Reputation is the currency of CropDeal. Every verified transaction can be rated by both parties.</p>
        </div>
        <div>
          <a routerLink="/orders" class="btn btn-primary">View Completed Orders</a>
        </div>
      </div>

      <!-- Rating Summary Card -->
      <div class="card form-card" style="margin-top: 24px;">
        <div class="split">
          <div style="display: flex; align-items: center; gap: 20px;">
            <div style="font-size: 48px; font-weight: 800; color: var(--green);">
              {{ averageRating() | number:'1.1-1' }}
            </div>
            <div>
              <div style="font-size: 20px;">{{ renderStars(averageRating()) }}</div>
              <div class="muted" style="font-size: 13px; margin-top: 4px;">Based on {{ reviews.length }} verified trade reviews</div>
            </div>
          </div>
          <span class="badge" style="background: #eef5ee; color: var(--green); font-size: 13px; padding: 8px 14px;">
            🛡️ Verified Platform Feedback
          </span>
        </div>
      </div>

      <!-- Reviews Feed -->
      <div class="card table-wrap" style="margin-top: 24px;">
        <div style="padding: 18px 20px; border-bottom: 1px solid var(--line);">
          <h3 style="margin: 0;">Feedback Received</h3>
        </div>

        <div *ngIf="reviews.length; else noReviews" style="display: flex; flex-direction: column; divide-y;">
          <div *ngFor="let r of reviews" style="padding: 20px; border-bottom: 1px solid var(--line);">
            <div class="split" style="margin-bottom: 6px;">
              <div>
                <span style="font-size: 18px;">{{ renderStars(r.rating) }}</span>
                <span class="badge" style="margin-left: 10px;">{{ r.reviewType || 'Trade Partner' }}</span>
                <span class="muted" style="margin-left: 8px; font-size: 12px;">Order #{{ r.orderId }}</span>
              </div>
              <span class="muted" style="font-size: 12px;">{{ r.createdAt | date:'mediumDate' }}</span>
            </div>
            <p style="margin: 8px 0 0; color: #2d3b34; font-size: 15px; line-height: 1.5;">
              "{{ r.review }}"
            </p>
          </div>
        </div>

        <ng-template #noReviews>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 40px; margin-bottom: 10px;">⭐</div>
            <h3>No reviews yet</h3>
            <p class="muted">Once you complete orders and trade with partners, your ratings and written reviews will appear here.</p>
            <a routerLink="/orders" class="btn btn-primary" style="margin-top: 10px;">Go to Orders</a>
          </div>
        </ng-template>
      </div>
    </div>
  `
})
export class ReviewsComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  reviews: Review[] = [];

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
    const uid = this.auth.userId();
    const role = this.auth.role();

    const path = role === 'ADMIN' ? '/reviews/admin' : '/reviews/user/' + uid;

    this.api.get<Review[]>(path).subscribe({
      next: (list: Review[]) => {
        this.reviews = list || [];
      },
      error: () => {
        this.reviews = [];
      }
    });
  }

  averageRating(): number {
    if (!this.reviews.length) return 5.0;
    const sum = this.reviews.reduce((acc, r) => acc + (r.rating || 5), 0);
    return sum / this.reviews.length;
  }

  renderStars(rating: number): string {
    const r = Math.round(rating);
    let s = '';
    for (let i = 0; i < 5; i++) {
      s += i < r ? '★' : '☆';
    }
    return s;
  }
}
