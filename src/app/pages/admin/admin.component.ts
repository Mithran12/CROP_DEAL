import { Component, inject, OnInit } from "@angular/core";
import {
  NgFor,
  NgIf,
  DatePipe,
  CurrencyPipe,
  CommonModule,
} from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../core/api.service";
import { User, Review, MarketRate } from "../../core/models";
import { ToastService } from "../../core/toast.service";
import { getCropImageUrl } from "../../core/crop-images";

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, DatePipe, CurrencyPipe, FormsModule],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">ADMIN CONTROL CENTER</div>
          <h1 class="title">Platform Operations & Oversight</h1>
          <p class="sub">
            Manage marketplace users, oversee escrow settlements, audit trades
            and export system compliance reports.
          </p>
        </div>
        <div style="display: flex; gap: 10px;">
          <button class="btn btn-primary" (click)="downloadExcelReport()">
            📥 Download Excel Order Report
          </button>
          <button class="btn btn-light" (click)="loadAll()">↻ Refresh</button>
        </div>
      </div>

      <!-- KPI Statistics Cards -->
      <div class="grid grid-4" style="margin-top: 24px;">
        <div class="card stat">
          <div class="icon">👨‍🌾</div>
          <div class="number">{{ farmers }}</div>
          <div class="label">Registered Farmers</div>
        </div>
        <div class="card stat">
          <div class="icon">🏪</div>
          <div class="number">{{ dealers }}</div>
          <div class="label">Registered Dealers</div>
        </div>
        <div class="card stat">
          <div class="icon">✅</div>
          <div class="number" style="color: var(--green);">
            {{ activeCount }}
          </div>
          <div class="label">Active Verified Users</div>
        </div>
        <div class="card stat">
          <div class="icon">⭐</div>
          <div class="number">{{ allReviews.length }}</div>
          <div class="label">Trade Reviews Logged</div>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div
        style="display: flex; gap: 12px; margin-top: 28px; border-bottom: 2px solid var(--line); padding-bottom: 8px;"
      >
        <button
          class="btn"
          [style.background]="
            activeTab === 'users' ? 'var(--green)' : '#f2f4f1'
          "
          [style.color]="activeTab === 'users' ? '#fff' : 'var(--ink)'"
          (click)="activeTab = 'users'"
        >
          👥 User Management
        </button>
        <button
          class="btn"
          [style.background]="
            activeTab === 'reports' ? 'var(--green)' : '#f2f4f1'
          "
          [style.color]="activeTab === 'reports' ? '#fff' : 'var(--ink)'"
          (click)="activeTab = 'reports'"
        >
          📊 Orders & Compliance Reports
        </button>
        <button
          class="btn"
          [style.background]="
            activeTab === 'reviews' ? 'var(--green)' : '#f2f4f1'
          "
          [style.color]="activeTab === 'reviews' ? '#fff' : 'var(--ink)'"
          (click)="activeTab = 'reviews'"
        >
          ⭐ Reviews Audit
        </button>
        <button
          class="btn"
          [style.background]="
            activeTab === 'pricing' ? 'var(--green)' : '#f2f4f1'
          "
          [style.color]="activeTab === 'pricing' ? '#fff' : 'var(--ink)'"
          (click)="switchTab('pricing')"
        >
          📈 Market Price Management
        </button>
      </div>

      <!-- TAB 1: USER MANAGEMENT -->
      <div
        *ngIf="activeTab === 'users'"
        class="card table-wrap"
        style="margin-top: 20px;"
      >
        <div
          style="padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line);"
        >
          <h3 style="margin: 0;">
            Marketplace Participants ({{ filteredUsers.length }})
          </h3>
          <div style="display: flex; gap: 8px;">
            <select
              [(ngModel)]="roleFilter"
              (change)="applyUserFilter()"
              style="padding: 6px 12px; border-radius: 8px; border: 1px solid var(--line);"
            >
              <option value="">All Roles</option>
              <option value="FARMER">Farmers</option>
              <option value="DEALER">Dealers</option>
            </select>
          </div>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>User Name & Email</th>
              <th>Auth ID / Profile ID</th>
              <th>Role</th>
              <th>Contact Phone</th>
              <th>Location</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let u of filteredUsers">
              <td>
                <b>{{ u.name || "Profile Pending" }}</b
                ><br />
                <small class="muted">{{ u.email }}</small>
              </td>
              <td>
                <span class="muted">Auth #{{ u.authUserId }}</span
                ><br />
                <small>Profile #{{ u.id }}</small>
              </td>
              <td>
                <span
                  class="badge"
                  [style.background]="
                    u.role === 'FARMER' ? '#eef5ee' : '#e8f0fe'
                  "
                  [style.color]="
                    u.role === 'FARMER' ? 'var(--green)' : '#1a73e8'
                  "
                >
                  {{ u.role }}
                </span>
              </td>
              <td>{{ u.phone || "—" }}</td>
              <td>{{ u.city ? u.city + ", " + u.state : "—" }}</td>
              <td>
                <span
                  class="badge"
                  [style.background]="u.active ? '#eef5ee' : '#ffebee'"
                  [style.color]="u.active ? 'var(--green)' : 'var(--red)'"
                >
                  {{ u.active ? "Active" : "Deactivated" }}
                </span>
              </td>
              <td>
                <button
                  class="btn btn-small"
                  [ngClass]="u.active ? 'btn-danger' : 'btn-light'"
                  (click)="toggleUser(u)"
                >
                  {{ u.active ? "Deactivate" : "Activate" }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- TAB 2: REPORTS & COMPLIANCE -->
      <div *ngIf="activeTab === 'reports'" style="margin-top: 20px;">
        <div class="card form-card">
          <div class="split" style="margin-bottom: 14px;">
            <div>
              <div class="eyebrow">ORDER AUDIT SEARCH</div>
              <h2 style="margin: 4px 0;">Search Transactions & Filter</h2>
            </div>
            <div style="display: flex; gap: 8px;">
              <button
                class="btn btn-primary btn-small"
                (click)="searchOrdersReport()"
              >
                Search Orders
              </button>
              <button
                class="btn btn-light btn-small"
                (click)="downloadFilteredExcelReport()"
              >
                📥 Export Filtered Excel
              </button>
            </div>
          </div>

          <div class="form-grid" style="grid-template-columns: repeat(3, 1fr);">
            <div class="field">
              <label>Order Number</label>
              <input
                [(ngModel)]="searchOrderNumber"
                placeholder="ORD-2026-..."
              />
            </div>
            <div class="field">
              <label>Status</label>
              <select [(ngModel)]="searchStatus">
                <option value="">All Statuses</option>
                <option value="CREATED">CREATED</option>
                <option value="ACCEPTED">ACCEPTED</option>
                <option value="PAID">PAID</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </div>
            <div class="field">
              <label>Location Filter</label>
              <input
                [(ngModel)]="searchLocation"
                placeholder="e.g. Coimbatore"
              />
            </div>
          </div>
        </div>

        <div class="card table-wrap" style="margin-top: 20px;">
          <div
            style="padding: 16px 20px; border-bottom: 1px solid var(--line);"
          >
            <h3 style="margin: 0;">Order Audit Records</h3>
          </div>
          <table class="table" *ngIf="auditOrders.length; else noAudit">
            <thead>
              <tr>
                <th>Order #</th>
                <th>Quantity</th>
                <th>Agreed Price</th>
                <th>Total</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let o of auditOrders">
                <td>
                  <b>#{{ o.orderNumber || o.id }}</b>
                </td>
                <td>{{ o.quantity }} qtl</td>
                <td>
                  {{ o.agreedPrice | currency: "INR" : "symbol" : "1.0-0" }}
                </td>
                <td>
                  <b style="color: var(--green);">{{
                    o.totalAmount | currency: "INR" : "symbol" : "1.0-0"
                  }}</b>
                </td>
                <td>
                  <span class="badge">{{ o.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
          <ng-template #noAudit>
            <div class="empty">
              Click "Search Orders" or download the complete Excel export.
            </div>
          </ng-template>
        </div>
      </div>

      <!-- TAB 3: REVIEWS AUDIT -->
      <div
        *ngIf="activeTab === 'reviews'"
        class="card table-wrap"
        style="margin-top: 20px;"
      >
        <div style="padding: 16px 20px; border-bottom: 1px solid var(--line);">
          <h3 style="margin: 0;">System Reviews & Trust Scores</h3>
        </div>
        <table class="table" *ngIf="allReviews.length; else noReviews">
          <thead>
            <tr>
              <th>Order #</th>
              <th>Reviewer #</th>
              <th>Target User #</th>
              <th>Rating</th>
              <th>Review Text</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of allReviews">
              <td>
                <b>#{{ r.orderId }}</b>
              </td>
              <td>User #{{ r.reviewerId }}</td>
              <td>User #{{ r.targetUserId }}</td>
              <td>
                <b>{{ r.rating }} ⭐</b>
              </td>
              <td>"{{ r.review }}"</td>
              <td class="muted">{{ r.createdAt | date: "short" }}</td>
            </tr>
          </tbody>
        </table>
        <ng-template #noReviews>
          <div class="empty">No platform reviews logged yet.</div>
        </ng-template>
      </div>

      <!-- TAB 4: MARKET PRICE MANAGEMENT (Admin Managed) -->
      <div *ngIf="activeTab === 'pricing'" style="margin-top: 20px;">
        <div class="split" style="margin-bottom: 16px;">
          <div>
            <h3 style="margin: 0;">APMC Mandi Benchmark Rates</h3>
            <p class="muted" style="margin: 4px 0 0; font-size: 13px;">
              Official market prices per quintal referenced across farmer
              listings and automated bidding.
            </p>
          </div>
          <button class="btn btn-primary" (click)="openAddPrice()">
            {{ showPriceForm ? "Close Form" : "+ Add Mandi Price" }}
          </button>
        </div>

        <!-- Add / Edit Form Card -->
        <div
          *ngIf="showPriceForm"
          class="card form-card"
          style="margin-bottom: 24px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;"
        >
          <h3 style="margin-top: 0;">
            {{
              editingRateId
                ? "Edit Mandi Market Price #" + editingRateId
                : "Add New Mandi Benchmark Price"
            }}
          </h3>
          <form (ngSubmit)="savePrice()">
            <div class="form-grid">
              <div class="field">
                <label>Crop Name *</label>
                <input
                  [(ngModel)]="priceForm.cropName"
                  name="cropName"
                  required
                  placeholder="e.g. Sona Masoori Rice, Red Onion"
                />
              </div>
              <div class="field">
                <label>Category *</label>
                <select
                  [(ngModel)]="priceForm.category"
                  name="category"
                  required
                >
                  <option value="GRAIN">Grains</option>
                  <option value="VEGETABLE">Vegetables</option>
                  <option value="FRUIT">Fruits</option>
                  <option value="PULSES">Pulses</option>
                  <option value="OILSEEDS">Oilseeds</option>
                  <option value="SPICES">Spices</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div class="field">
                <label>State *</label>
                <input
                  [(ngModel)]="priceForm.state"
                  name="state"
                  required
                  placeholder="e.g. Tamil Nadu, Punjab"
                />
              </div>
              <div class="field">
                <label>District / Mandi Market *</label>
                <input
                  [(ngModel)]="priceForm.district"
                  name="district"
                  required
                  placeholder="e.g. Karnal, Nizamabad"
                />
              </div>
              <div class="field">
                <label>Market Price (₹ / Quintal) *</label>
                <input
                  type="number"
                  [(ngModel)]="priceForm.marketPrice"
                  name="marketPrice"
                  min="0"
                  required
                  placeholder="e.g. 2800"
                />
              </div>
              <div class="field">
                <label>Maximum Price (₹ / Quintal) *</label>
                <input
                  type="number"
                  [(ngModel)]="priceForm.maxPrice"
                  name="maxPrice"
                  min="0"
                  required
                  placeholder="e.g. 3500"
                />
              </div>
              <div class="field" style="grid-column: span 2;">
                <label>Crop Image (Upload or Image URL)</label>
                <div
                  style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;"
                >
                  <input
                    type="file"
                    accept="image/*"
                    (change)="onPriceImageSelected($event)"
                    style="flex: 1; min-width: 200px;"
                  />
                  <input
                    type="url"
                    [(ngModel)]="priceForm.imageUrl"
                    name="imageUrl"
                    placeholder="Or paste image URL (https://...)"
                    style="flex: 1; min-width: 200px;"
                  />
                </div>
                <div
                  *ngIf="priceForm.imageUrl"
                  style="margin-top: 8px; display: flex; align-items: center; gap: 10px;"
                >
                  <img
                    [src]="priceForm.imageUrl"
                    alt="Preview"
                    style="width: 44px; height: 44px; border-radius: 6px; object-fit: cover; border: 1px solid var(--line);"
                  />
                  <span class="muted" style="font-size: 12px;"
                    >Image Preview</span
                  >
                  <button
                    type="button"
                    class="btn btn-light btn-small"
                    (click)="priceForm.imageUrl = ''"
                  >
                    Remove Image
                  </button>
                </div>
              </div>
            </div>

            <div style="display: flex; gap: 12px; margin-top: 20px;">
              <button
                type="submit"
                class="btn btn-primary"
                [disabled]="savingPrice"
              >
                {{
                  savingPrice
                    ? "Saving…"
                    : editingRateId
                      ? "Update Market Price"
                      : "Save Market Price"
                }}
              </button>
              <button
                type="button"
                class="btn btn-light"
                (click)="showPriceForm = false"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>

        <!-- Market Rates Table -->
        <div class="card table-wrap">
          <table class="table" *ngIf="marketRates.length; else noRates">
            <thead>
              <tr>
                <th>Crop & Category</th>
                <th>District / Mandi</th>
                <th>State</th>
                <th>Market Price</th>
                <th>Max Price</th>
                <th>Updated</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of marketRates">
                <td>
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <img
                      [src]="
                        r.imageUrl || getCropImageUrl(r.cropName, r.category)
                      "
                      [alt]="r.cropName"
                      style="width: 40px; height: 40px; border-radius: 6px; object-fit: cover; border: 1px solid var(--line); background: #eee;"
                    />
                    <div>
                      <b>{{ r.cropName }}</b
                      ><br />
                      <span
                        class="badge"
                        style="padding: 2px 6px; font-size: 11px;"
                        >{{ r.category }}</span
                      >
                    </div>
                  </div>
                </td>
                <td>
                  <b>{{ r.district || r.location }}</b>
                </td>
                <td>{{ r.state || "India" }}</td>
                <td>
                  <b style="color: var(--green); font-size: 15px;"
                    >{{
                      r.marketPrice ?? r.pricePerQuintal
                        | currency: "INR" : "symbol" : "1.0-0"
                    }}
                    / Qtl</b
                  >
                </td>
                <td>
                  <b style="color: #b78103; font-size: 14px;"
                    >{{
                      r.maxPrice ?? r.marketPrice * 1.1
                        | currency: "INR" : "symbol" : "1.0-0"
                    }}
                    / Qtl</b
                  >
                </td>
                <td class="muted">{{ r.updatedAt | date: "mediumDate" }}</td>
                <td>
                  <div style="display: flex; gap: 6px;">
                    <button
                      class="btn btn-light btn-small"
                      (click)="editPrice(r)"
                    >
                      Edit
                    </button>
                    <button
                      class="btn btn-danger btn-small"
                      (click)="deletePrice(r.id!)"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <ng-template #noRates>
            <div class="empty" style="padding: 40px 20px;">
              No market benchmark rates added yet. Click "+ Add Mandi Price" to
              create rates.
            </div>
          </ng-template>
        </div>
      </div>
    </div>
  `,
})
export class AdminComponent implements OnInit {
  api = inject(ApiService);
  toast = inject(ToastService);

  activeTab: "users" | "reports" | "reviews" | "pricing" = "users";

  allUsers: User[] = [];
  filteredUsers: User[] = [];
  farmers = 0;
  dealers = 0;
  activeCount = 0;

  getCropImageUrl = getCropImageUrl;
  marketRates: MarketRate[] = [];
  showPriceForm = false;
  editingRateId?: number;
  savingPrice = false;
  priceForm: any = {
    cropName: "",
    category: "GRAIN",
    state: "",
    district: "",
    marketPrice: 2800,
    maxPrice: 3500,
    imageUrl: "",
  };

  roleFilter = "";

  allReviews: Review[] = [];

  searchOrderNumber = "";
  searchStatus = "";
  searchLocation = "";
  auditOrders: any[] = [];

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.loadUsers();
    this.loadReviews();
  }

  loadUsers() {
    this.api.get<User[]>("/users").subscribe({
      next: (list: User[]) => {
        this.allUsers = list || [];
        this.farmers = this.allUsers.filter((u) => u.role === "FARMER").length;
        this.dealers = this.allUsers.filter((u) => u.role === "DEALER").length;
        this.activeCount = this.allUsers.filter((u) => u.active).length;
        this.applyUserFilter();
      },
      error: (e: any) => {
        this.toast.show(
          e.error?.message || "Could not load users list",
          "error",
        );
      },
    });
  }

  applyUserFilter() {
    if (!this.roleFilter) {
      this.filteredUsers = this.allUsers;
    } else {
      this.filteredUsers = this.allUsers.filter(
        (u) => u.role === this.roleFilter,
      );
    }
  }

  toggleUser(u: User) {
    const path = u.active
      ? `/users/${u.id}/deactivate`
      : `/users/${u.id}/activate`;
    this.api.patch(path, {}).subscribe({
      next: () => {
        this.toast.show(
          `User ${u.name || u.email} ${u.active ? "deactivated" : "activated"}`,
        );
        this.loadUsers();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || "Status update failed", "error");
      },
    });
  }

  loadReviews() {
    this.api.get<Review[]>("/reviews/admin").subscribe({
      next: (list: Review[]) => (this.allReviews = list || []),
      error: () => (this.allReviews = []),
    });
  }

  searchOrdersReport() {
    const params: Record<string, string | number | undefined> = {};
    if (this.searchOrderNumber)
      params["orderNumber"] = this.searchOrderNumber.trim();
    if (this.searchStatus) params["status"] = this.searchStatus;
    if (this.searchLocation) params["location"] = this.searchLocation.trim();

    this.api.get<any>("/reports/orders/search", params).subscribe({
      next: (res: any) => {
        this.auditOrders = res?.orders || res || [];
        this.toast.show(
          `Found ${this.auditOrders.length} matching order records`,
        );
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || "Search failed", "error");
      },
    });
  }

  downloadExcelReport() {
    this.toast.show("Generating Excel compliance report…");
    this.api.getBlob("/reports/orders/excel").subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "CropDeal_Master_Orders_Report.xlsx";
        a.click();
        URL.revokeObjectURL(url);
        this.toast.show("Excel report downloaded successfully!");
      },
      error: () => {
        this.toast.show("Failed to download Excel report", "error");
      },
    });
  }

  downloadFilteredExcelReport() {
    this.toast.show("Generating filtered Excel report…");
    const params: Record<string, string | number | undefined> = {};
    if (this.searchOrderNumber)
      params["orderNumber"] = this.searchOrderNumber.trim();
    if (this.searchStatus) params["status"] = this.searchStatus;
    if (this.searchLocation) params["location"] = this.searchLocation.trim();

    const query = Object.entries(params)
      .filter(([_, v]) => v !== undefined && v !== "")
      .map(
        ([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`,
      )
      .join("&");

    const url = "/reports/orders/search/excel" + (query ? "?" + query : "");

    this.api.getBlob(url).subscribe({
      next: (blob: Blob) => {
        const fileUrl = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = fileUrl;
        a.download = "CropDeal_Filtered_Orders_Report.xlsx";
        a.click();
        URL.revokeObjectURL(fileUrl);
        this.toast.show("Filtered report downloaded successfully!");
      },
      error: () => {
        this.toast.show("Failed to download filtered report", "error");
      },
    });
  }

  switchTab(tab: "users" | "reports" | "reviews" | "pricing") {
    this.activeTab = tab;
    if (tab === "pricing") {
      this.loadPricing();
    }
  }

  loadPricing() {
    this.api.get<MarketRate[]>("/pricing/market-rates").subscribe({
      next: (list) => (this.marketRates = list || []),
      error: (e) =>
        this.toast.show(e.error?.message || "Could not load pricing", "error"),
    });
  }

  openAddPrice() {
    this.showPriceForm = !this.showPriceForm;
    this.editingRateId = undefined;
    this.priceForm = {
      cropName: "",
      category: "GRAIN",
      state: "",
      district: "",
      marketPrice: 2800,
      maxPrice: 3500,
      imageUrl: "",
    };
  }

  onPriceImageSelected(event: any) {
    const file = event.target?.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      this.toast.show("Please select a valid image file", "error");
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.toast.show("Image size must be less than 2MB", "error");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      this.priceForm.imageUrl = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  editPrice(r: MarketRate) {
    this.editingRateId = r.id;
    this.priceForm = {
      cropName: r.cropName,
      category: r.category || "GRAIN",
      state: r.state || "",
      district: r.district || r.location || "",
      marketPrice: r.marketPrice ?? r.pricePerQuintal ?? 0,
      maxPrice:
        r.maxPrice ?? (r.marketPrice ? Math.round(r.marketPrice * 1.1) : 0),
      imageUrl: r.imageUrl || "",
    };
    this.showPriceForm = true;
  }

  savePrice() {
    if (!this.priceForm.cropName || !this.priceForm.cropName.trim()) {
      this.toast.show("Crop name is required", "error");
      return;
    }
    if (this.priceForm.marketPrice == null || this.priceForm.marketPrice < 0) {
      this.toast.show("Market price must be a non-negative number", "error");
      return;
    }
    if (this.priceForm.maxPrice == null || this.priceForm.maxPrice < 0) {
      this.toast.show("Maximum price must be a non-negative number", "error");
      return;
    }
    if (Number(this.priceForm.maxPrice) < Number(this.priceForm.marketPrice)) {
      this.toast.show(
        "Maximum price cannot be lower than market price",
        "error",
      );
      return;
    }

    const mPrice = Number(this.priceForm.marketPrice);
    const mxPrice = Number(this.priceForm.maxPrice);

    const payload = {
      cropName: this.priceForm.cropName.trim(),
      category: this.priceForm.category,
      state: this.priceForm.state.trim() || "All States",
      district: this.priceForm.district.trim() || "Central Mandi",
      marketPrice: mPrice,
      maxPrice: mxPrice,
      imageUrl: this.priceForm.imageUrl || "",
      minPrice: mPrice,
      modalPrice: mPrice,
      pricePerQuintal: mPrice,
    };

    this.savingPrice = true;
    const req$ = this.editingRateId
      ? this.api.put<MarketRate>(
          "/pricing/market-rates/" + this.editingRateId,
          payload,
        )
      : this.api.post<MarketRate>("/pricing/market-rates", payload);

    req$.subscribe({
      next: () => {
        this.savingPrice = false;
        this.showPriceForm = false;
        this.toast.show(
          this.editingRateId
            ? "Market rate updated"
            : "Market rate added successfully!",
        );
        this.loadPricing();
      },
      error: (e) => {
        this.savingPrice = false;
        this.toast.show(
          e.error?.message || e.error || "Failed to save market rate",
          "error",
        );
      },
    });
  }

  deletePrice(id: number) {
    if (!confirm("Are you sure you want to delete this market benchmark rate?"))
      return;
    this.api.delete("/pricing/market-rates/" + id).subscribe({
      next: () => {
        this.toast.show("Market rate deleted");
        this.loadPricing();
      },
      error: (e) => {
        this.toast.show(e.error?.message || "Delete failed", "error");
      },
    });
  }
}
