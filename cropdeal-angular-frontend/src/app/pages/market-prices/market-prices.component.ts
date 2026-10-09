import { Component, inject, OnInit } from "@angular/core";
import {
  CommonModule,
  NgFor,
  NgIf,
  CurrencyPipe,
  DatePipe,
} from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ApiService } from "../../core/api.service";
import { AuthService } from "../../core/auth.service";
import { MarketRate } from "../../core/models";
import { ToastService } from "../../core/toast.service";
import { getCropImageUrl } from "../../core/crop-images";

@Component({
  standalone: true,
  imports: [
    CommonModule,
    NgFor,
    NgIf,
    CurrencyPipe,
    DatePipe,
    FormsModule,
    RouterLink,
  ],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">CENTRAL MANDI BENCHMARKS</div>
          <h1 class="title">APMC Mandi Market Prices</h1>
          <p class="sub">
            Admin-managed official agricultural market rates per quintal across
            districts and categories.
          </p>
        </div>
        <div style="display: flex; gap: 10px; align-items: center;">
          <a
            *ngIf="auth.role() === 'ADMIN'"
            routerLink="/admin"
            class="btn btn-primary"
            >⚙️ Manage in Admin</a
          >
          <button class="btn btn-light" (click)="load()">↻ Refresh</button>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="card" style="margin-top: 24px; padding: 18px 20px;">
        <div
          style="display: flex; gap: 16px; flex-wrap: wrap; align-items: center; justify-content: space-between;"
        >
          <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === '' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="selectedCategory === '' ? '#fff' : 'var(--ink)'"
              (click)="setCategory('')"
            >
              All Categories
            </button>
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === 'GRAIN' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="
                selectedCategory === 'GRAIN' ? '#fff' : 'var(--ink)'
              "
              (click)="setCategory('GRAIN')"
            >
              🌾 Grains
            </button>
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === 'VEGETABLE' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="
                selectedCategory === 'VEGETABLE' ? '#fff' : 'var(--ink)'
              "
              (click)="setCategory('VEGETABLE')"
            >
              🍅 Vegetables
            </button>
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === 'FRUIT' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="
                selectedCategory === 'FRUIT' ? '#fff' : 'var(--ink)'
              "
              (click)="setCategory('FRUIT')"
            >
              🍎 Fruits
            </button>
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === 'PULSES' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="
                selectedCategory === 'PULSES' ? '#fff' : 'var(--ink)'
              "
              (click)="setCategory('PULSES')"
            >
              🌱 Pulses
            </button>
            <button
              class="btn btn-small"
              [style.background]="
                selectedCategory === 'OILSEEDS' ? 'var(--green)' : '#f2f4f1'
              "
              [style.color]="
                selectedCategory === 'OILSEEDS' ? '#fff' : 'var(--ink)'
              "
              (click)="setCategory('OILSEEDS')"
            >
              🌻 Oilseeds
            </button>
          </div>

          <div style="min-width: 260px;">
            <input
              type="text"
              [(ngModel)]="searchQuery"
              (input)="applyFilter()"
              placeholder="🔍 Search crop, district or state..."
              style="padding: 8px 14px; border-radius: 8px; border: 1px solid var(--line); width: 100%;"
            />
          </div>
        </div>
      </div>

      <!-- Market Rates Table -->
      <div class="card table-wrap" style="margin-top: 20px;">
        <table class="table" *ngIf="filteredRates.length; else emptyTable">
          <thead>
            <tr>
              <th>CROP & CATEGORY</th>
              <th>MANDI / DISTRICT</th>
              <th>STATE</th>
              <th>MARKET PRICE</th>
              <th>MAXIMUM PRICE</th>
              <th>UPDATED DATE</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of filteredRates">
              <td>
                <div style="display: flex; align-items: center; gap: 12px;">
                  <img
                    [src]="
                      r.imageUrl || getCropImageUrl(r.cropName, r.category)
                    "
                    [alt]="r.cropName"
                    style="width: 44px; height: 44px; border-radius: 8px; object-fit: cover; background: #eee; border: 1px solid var(--line);"
                  />
                  <div>
                    <b>{{ r.cropName }}</b
                    ><br />
                    <small
                      class="badge"
                      style="padding: 2px 6px; font-size: 11px;"
                      >{{ r.category || "Agricultural" }}</small
                    >
                  </div>
                </div>
              </td>
              <td>
                <b>{{ r.district || r.location || "Central Yard" }}</b>
              </td>
              <td>
                <span class="muted">{{ r.state || "India" }}</span>
              </td>
              <td>
                <b style="color: var(--green); font-size: 15px;">
                  {{
                    r.marketPrice ?? r.pricePerQuintal
                      | currency: "INR" : "symbol" : "1.0-0"
                  }}
                  / Qtl
                </b>
              </td>
              <td>
                <b style="color: #b78103; font-size: 14px;">
                  {{
                    r.maxPrice ?? r.marketPrice * 1.1
                      | currency: "INR" : "symbol" : "1.0-0"
                  }}
                  / Qtl
                </b>
              </td>
              <td>
                <small class="muted">{{
                  r.updatedAt || today | date: "mediumDate"
                }}</small>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #emptyTable>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 40px; margin-bottom: 10px;">📈</div>
            <h3>No mandi rates match your filter</h3>
            <p class="muted">
              Check your search query or clear the filter. Rates will appear
              once updated by administration.
            </p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
})
export class MarketPricesComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  rates: MarketRate[] = [];
  filteredRates: MarketRate[] = [];
  selectedCategory = "";
  searchQuery = "";
  today = new Date().toISOString();
  getCropImageUrl = getCropImageUrl;

  ngOnInit() {
    this.load();
  }

  load() {
    let url = "/pricing/market-rates";
    const q: string[] = [];
    if (this.selectedCategory)
      q.push("category=" + encodeURIComponent(this.selectedCategory));
    if (this.searchQuery.trim())
      q.push("search=" + encodeURIComponent(this.searchQuery.trim()));
    if (q.length) url += "?" + q.join("&");

    this.api.get<MarketRate[]>(url).subscribe({
      next: (list) => {
        this.rates = list || [];
        this.applyFilter();
      },
      error: (e) => {
        this.toast.show(
          e.error?.message || "Could not load market rates",
          "error",
        );
      },
    });
  }

  setCategory(cat: string) {
    this.selectedCategory = cat;
    this.load();
  }

  applyFilter() {
    let res = this.rates;
    if (this.selectedCategory) {
      res = res.filter(
        (r) =>
          (r.category || "").toUpperCase() ===
            this.selectedCategory.toUpperCase() ||
          (r.category || "")
            .toUpperCase()
            .includes(this.selectedCategory.toUpperCase()),
      );
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      res = res.filter(
        (r) =>
          (r.cropName || "").toLowerCase().includes(q) ||
          (r.district || "").toLowerCase().includes(q) ||
          (r.state || "").toLowerCase().includes(q) ||
          (r.location || "").toLowerCase().includes(q),
      );
    }
    this.filteredRates = res;
  }
}
