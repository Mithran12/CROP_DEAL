import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe, DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Order, Payment, Invoice, Receipt, Review, Crop } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import jsPDF from 'jspdf';

@Component({
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, CurrencyPipe, DatePipe, FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">TRANSACTIONS & FULFILLMENT</div>
          <h1 class="title">Orders & Escrow Settlements</h1>
          <p class="sub">Track order progress from farmer acceptance to secure payment, QR delivery verification and final settlement.</p>
        </div>
        <div style="display: flex; gap: 10px;">
          <a *ngIf="auth.role() === 'DEALER'" routerLink="/marketplace" class="btn btn-primary">+ Buy More Crops</a>
          <a *ngIf="auth.role() === 'FARMER'" routerLink="/crops" class="btn btn-primary">+ Publish Crop</a>
          <button class="btn btn-light" (click)="load()">↻ Refresh</button>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="card table-wrap" style="margin-top: 24px;">
        <table class="table" *ngIf="orders.length; else empty">
          <thead>
            <tr>
              <th>Order ID & Date</th>
              <th>Crop & Quantity</th>
              <th>Agreed Rate</th>
              <th>Total Amount</th>
              <th>Order Status</th>
              <th>Escrow Payment</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let o of orders">
              <td>
                <b>#{{ o.orderNumber || o.id }}</b><br>
                <small class="muted">{{ o.createdAt | date:'mediumDate' }}</small>
              </td>
              <td>
                <b>{{ cropsMap[o.cropId] || 'Crop' }}</b><br>
                <small class="muted">{{ o.quantity }} quintals</small>
              </td>
              <td>{{ o.agreedPrice | currency:'INR':'symbol':'1.0-0' }} / qtl</td>
              <td><b style="color: var(--green); font-size: 15px;">{{ o.totalAmount | currency:'INR':'symbol':'1.0-0' }}</b></td>
              <td>
                <span class="badge" [style.background]="statusBg(o.status)" [style.color]="statusColor(o.status)">
                  {{ o.status }}
                </span>
              </td>
              <td>
                <span class="badge" [style.background]="o.paymentStatus === 'SUCCESS' ? '#eef5ee' : '#fff8e1'"
                      [style.color]="o.paymentStatus === 'SUCCESS' ? 'var(--green)' : '#b78103'">
                  {{ o.paymentStatus || 'PENDING' }}
                </span>
              </td>
              <td>
                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                  <!-- FARMER: Accept or Reject newly CREATED order -->
                  <ng-container *ngIf="auth.role() === 'FARMER' && o.status === 'CREATED'">
                    <button class="btn btn-primary btn-small" (click)="acceptOrder(o.id)">Accept Order</button>
                    <button class="btn btn-danger btn-small" (click)="rejectOrder(o.id)">Reject</button>
                  </ng-container>

                  <!-- DEALER: Awaiting Farmer acceptance -->
                  <span *ngIf="auth.role() === 'DEALER' && o.status === 'CREATED'"
                        class="badge" style="background: #fff8e1; color: #8a6a12; align-self: center; font-size: 11px;">
                    ⏳ Awaiting Farmer Acceptance
                  </span>

                  <!-- DEALER: Pay for ACCEPTED order -->
                  <button *ngIf="auth.role() === 'DEALER' && (o.status === 'ACCEPTED' || o.status === 'PAYMENT_FAILED') && o.paymentStatus !== 'SUCCESS' && o.status !== 'PAID' && o.status !== 'COMPLETED'"
                          class="btn btn-primary btn-small"
                          style="background: #176b45; border: 2px solid #cfe96b; box-shadow: 0 4px 12px rgba(23, 107, 69, 0.35); font-weight: 800;"
                          (click)="openPaymentModal(o)">
                    💳 PAY NOW ({{ o.totalAmount | currency:'INR':'symbol':'1.0-0' }})
                  </button>

                  <!-- FARMER: View Delivery QR Token to show Dealer -->
                  <button *ngIf="auth.role() === 'FARMER' && (o.status === 'PAID' || o.paymentStatus === 'SUCCESS') && !o.deliveryConfirmed"
                          class="btn btn-light btn-small" (click)="showFarmerQr(o)">
                    📱 Show Delivery QR
                  </button>

                  <!-- DEALER: Confirm delivery using QR code from Farmer -->
                  <button *ngIf="auth.role() === 'DEALER' && (o.status === 'PAID' || o.paymentStatus === 'SUCCESS') && !o.deliveryConfirmed"
                          class="btn btn-primary btn-small" (click)="openDeliveryModal(o)">
                    🚚 Confirm Delivery
                  </button>

                  <!-- DEALER: Cancel Order (not allowed for auction-won orders) -->
                  <button *ngIf="auth.role() === 'DEALER' && !o.auctionId && (o.status === 'CREATED' || o.status === 'ACCEPTED' || (o.status === 'PAID' && !o.deliveryConfirmed)) && o.status !== 'CANCELLED' && o.status !== 'COMPLETED'"
                          class="btn btn-danger btn-small" (click)="cancelOrder(o)">
                    ✕ Cancel Order
                  </button>

                  <!-- COMPLETED ORDER: Invoices & Receipts -->
                  <button *ngIf="o.status === 'COMPLETED'" class="btn btn-light btn-small" (click)="viewInvoice(o)">
                    📄 Invoice
                  </button>

                  <!-- COMPLETED ORDER: Write Review -->
                  <button *ngIf="o.status === 'COMPLETED'" class="btn btn-light btn-small" (click)="openReviewModal(o)">
                    ⭐ Review
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #empty>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 40px; margin-bottom: 10px;">📦</div>
            <h3>No orders found</h3>
            <p class="muted">Completed negotiations will create orders that appear here.</p>
          </div>
        </ng-template>
      </div>

      <!-- Payment Modal / Drawer (DEALER ONLY) -->
      <div *ngIf="paymentOrder" class="card form-card" style="margin-top: 24px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 14px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">SECURE DEALER ESCROW PAYMENT</div>
            <h2 style="margin: 6px 0;">Pay Order #{{ paymentOrder.orderNumber || paymentOrder.id }}</h2>
            <p class="sub" style="margin-bottom: 0;">
              Total Amount: <b style="font-size: 18px; color: var(--green);">{{ paymentOrder.totalAmount | currency:'INR':'symbol':'1.0-0' }}</b>
              · Funds will be held safely in Escrow until you confirm physical harvest delivery.
            </p>
          </div>
          <button class="btn btn-light btn-small" (click)="paymentOrder = undefined">✕ Close</button>
        </div>

        <div style="display: flex; gap: 10px; margin-bottom: 14px;">
          <button type="button" class="btn btn-light btn-small" (click)="pay.method = 'WALLET'">Use Escrow Wallet</button>
          <button type="button" class="btn btn-light btn-small" (click)="fillDemoCard()">Fill Demo Card</button>
          <button type="button" class="btn btn-light btn-small" (click)="fillDemoUpi()">Fill Demo UPI</button>
        </div>

        <form (ngSubmit)="submitPayment()">
          <div class="form-grid">
            <div class="field">
              <label>Payment Method</label>
              <select [(ngModel)]="pay.method" name="method">
                <option value="WALLET">CropDeal Escrow Wallet Balance</option>
                <option value="UPI">UPI (Unified Payments Interface)</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="CASH">Cash Settlement</option>
              </select>
            </div>

            <!-- Escrow Wallet Note -->
            <div class="field" *ngIf="pay.method === 'WALLET'">
              <label>Escrow Wallet Debit</label>
              <div style="background: #eef7e6; border: 1px solid #bce19d; border-radius: 10px; padding: 12px; font-size: 13px; color: #1e5927;">
                💰 <b>{{ paymentOrder.totalAmount | currency:'INR':'symbol':'1.0-0' }}</b> will be deducted from your verified CropDeal Escrow Wallet balance and locked safely in Escrow pending physical harvest handoff.
              </div>
            </div>

            <!-- UPI Field -->
            <div class="field" *ngIf="pay.method === 'UPI'">
              <label>Your UPI ID (Virtual Payment Address) *</label>
              <input [(ngModel)]="pay.upi" name="upi" placeholder="dealer@upi">
            </div>

            <!-- Card Fields -->
            <ng-container *ngIf="pay.method === 'CREDIT_CARD' || pay.method === 'DEBIT_CARD'">
              <div class="field">
                <label>Card Number (16 Digits) *</label>
                <input [(ngModel)]="pay.card" name="card" maxlength="16" placeholder="4111222233334444">
              </div>
              <div class="field">
                <label>Name on Card *</label>
                <input [(ngModel)]="pay.holder" name="holder" placeholder="Dealer Buyer Name">
              </div>
              <div class="field">
                <label>Card Expiry (MM/YY) *</label>
                <input [(ngModel)]="pay.expiry" name="expiry" maxlength="5" placeholder="12/28">
              </div>
              <div class="field">
                <label>Security Code (CVV) *</label>
                <input [(ngModel)]="pay.cvv" name="cvv" maxlength="3" type="password" placeholder="123">
              </div>
            </ng-container>
          </div>

          <div style="display: flex; gap: 12px; margin-top: 20px;">
            <button type="submit" class="btn btn-primary" [disabled]="paying">
              {{ paying ? 'Processing Escrow Deposit…' : 'Authorize Escrow Payment' }}
            </button>
            <button type="button" class="btn btn-light" (click)="paymentOrder = undefined">Cancel</button>
          </div>
        </form>
      </div>

      <!-- Farmer QR Display Modal -->
      <div *ngIf="qrFarmerOrder" class="card form-card" style="margin-top: 24px; border: 2px solid #23915e; animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 12px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">DELIVERY CONFIRMATION QR TOKEN</div>
            <h2 style="margin: 6px 0;">Order #{{ qrFarmerOrder.orderNumber || qrFarmerOrder.id }} Delivery Code</h2>
            <p class="sub">Show this QR code or provide the token below to the dealer upon harvest handoff.</p>
          </div>
          <button class="btn btn-light btn-small" (click)="qrFarmerOrder = undefined">✕ Close</button>
        </div>

        <div style="display: flex; gap: 24px; align-items: center; padding: 20px; background: #fbfdfb; border-radius: 14px;">
          <div style="text-align: center;">
            <img [src]="qrImageUrl" alt="Delivery QR Code" style="width: 170px; height: 170px; border-radius: 12px; border: 2px solid var(--line); background: #fff;"
                 (error)="onQrImageError($event)">
            <div class="muted" style="font-size: 11px; margin-top: 6px;">Scan with Camera / App</div>
          </div>
          <div>
            <div class="muted" style="font-size: 12px; text-transform: uppercase; font-weight: 700;">Text Token for Manual Entry:</div>
            <div style="display: flex; gap: 8px; align-items: center; margin: 8px 0;">
              <div style="font-family: monospace; font-size: 18px; font-weight: 800; background: #eef5ee; color: var(--green); padding: 12px 18px; border-radius: 8px; word-break: break-all; flex: 1;">
                {{ farmerQrToken || 'Loading Token…' }}
              </div>
              <button type="button" class="btn btn-light btn-small" (click)="copyToken()">📋 Copy Token</button>
            </div>
            <p class="muted" style="font-size: 13px; margin: 0;">Once the dealer validates this token, the escrow payment will be released immediately to your wallet!</p>
          </div>
        </div>
      </div>

      <!-- Dealer Delivery Confirmation Modal -->
      <div *ngIf="deliveryOrder" class="card form-card" style="margin-top: 24px; border: 2px solid #23915e; animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 12px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">CONFIRM HARVEST RECEIPT</div>
            <h2 style="margin: 6px 0;">Release Escrow for Order #{{ deliveryOrder.orderNumber || deliveryOrder.id }}</h2>
            <p class="sub">Enter the delivery verification token shown by the farmer upon physical receipt of crops.</p>
          </div>
          <button class="btn btn-light btn-small" (click)="deliveryOrder = undefined">✕ Close</button>
        </div>

        <div class="field" style="max-width: 480px;">
          <label>Delivery QR Code / Security Token *</label>
          <input [(ngModel)]="deliveryTokenInput" placeholder="Paste or type delivery token provided by farmer">
        </div>

        <div style="display: flex; gap: 12px; margin-top: 18px;">
          <button class="btn btn-primary" (click)="confirmDelivery()" [disabled]="delivering">
            {{ delivering ? 'Validating Token & Releasing Escrow…' : '✓ Confirm Receipt & Release Escrow' }}
          </button>
          <button class="btn btn-light" (click)="deliveryOrder = undefined">Cancel</button>
        </div>
      </div>

      <!-- Invoice View Modal -->
      <div *ngIf="activeInvoice" class="card form-card" style="margin-top: 24px; border: 2px solid #cfe96b; animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 16px; border-bottom: 1px solid var(--line); padding-bottom: 12px;">
          <div>
            <div class="eyebrow">OFFICIAL INVOICE</div>
            <h2 style="margin: 4px 0;">Invoice for {{ activeInvoice.orderNumber }}</h2>
            <div class="muted">Generated on {{ activeInvoice.generatedAt | date:'medium' }}</div>
          </div>
          <button class="btn btn-light btn-small" (click)="activeInvoice = undefined">✕ Close</button>
        </div>

        <div class="form-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 20px;">
          <div>
            <div class="muted" style="font-size: 12px;">Order Number</div>
            <b>{{ activeInvoice.orderNumber }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Quantity</div>
            <b>{{ activeInvoice.quantity }} quintals</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Unit Rate</div>
            <b>{{ activeInvoice.unitPrice | currency:'INR':'symbol':'1.0-0' }} / qtl</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Crop Name</div>
            <b>{{ activeInvoice.cropName || cropsMap[activeInvoice.cropId || 0] || 'Farm Harvest' }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Buyer</div>
            <b>{{ activeInvoice.dealerName || 'Verified Dealer' }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Total Paid</div>
            <b style="color: var(--green); font-size: 18px;">{{ activeInvoice.totalAmount | currency:'INR':'symbol':'1.0-0' }}</b>
          </div>
        </div>

        <div style="display: flex; gap: 10px;">
          <button class="btn btn-primary btn-small" (click)="downloadInvoicePdf()">  📄  Download Pdf Invoice</button>
          <button class="btn btn-light btn-small" (click)="activeInvoice = undefined">Done</button>
        </div>
      </div>

      <!-- Review Submission Modal -->
      <div *ngIf="reviewOrder" class="card form-card" style="margin-top: 24px; border: 2px solid #f39c12; animation: fadeIn .2s ease-out;">
        <div class="split" style="margin-bottom: 14px;">
          <div>
            <div class="eyebrow" style="color: #d35400;">RATING & REVIEW</div>
            <h2 style="margin: 4px 0;">Review Trade for Order #{{ reviewOrder.orderNumber || reviewOrder.id }}</h2>
            <p class="sub" style="margin-bottom: 0;">Share feedback about your trade partner for this transaction.</p>
          </div>
          <button class="btn btn-light btn-small" (click)="reviewOrder = undefined">✕ Close</button>
        </div>

        <div class="field" style="max-width: 280px; margin-bottom: 14px;">
          <label>Star Rating (1 to 5 Stars) *</label>
          <select [(ngModel)]="reviewRating">
            <option [value]="5">⭐⭐⭐⭐⭐ 5 Stars - Excellent Quality & Fast Trade</option>
            <option [value]="4">⭐⭐⭐⭐ 4 Stars - Very Good</option>
            <option [value]="3">⭐⭐⭐ 3 Stars - Average Experience</option>
            <option [value]="2">⭐⭐ 2 Stars - Below Expectations</option>
            <option [value]="1">⭐ 1 Star - Unsatisfactory</option>
          </select>
        </div>

        <div class="field" style="margin-bottom: 18px;">
          <label>Written Feedback / Comment</label>
          <textarea [(ngModel)]="reviewComment" rows="3" placeholder="Describe harvest quality, packaging, communication, or delivery timeliness…"></textarea>
        </div>

        <div style="display: flex; gap: 10px;">
          <button class="btn btn-primary" (click)="submitReview()" [disabled]="submittingReview">
            {{ submittingReview ? 'Submitting Review…' : 'Submit Review' }}
          </button>
          <button class="btn btn-light" (click)="reviewOrder = undefined">Cancel</button>
        </div>
      </div>
    </div>
  `
})
export class OrdersComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  orders: Order[] = [];
  paymentOrder?: Order;
  paying = false;

  qrFarmerOrder?: Order;
  farmerQrToken = '';
  qrImageUrl = '';

  deliveryOrder?: Order;
  deliveryTokenInput = '';
  delivering = false;

  activeInvoice?: Invoice;

  reviewOrder?: Order;
  reviewRating = 5;
  reviewComment = '';
  submittingReview = false;

  pay: any = {
    method: 'CREDIT_CARD',
    card: '4111222233334444',
    holder: 'Dealer Buyer',
    expiry: '12/28',
    cvv: '123',
    upi: 'dealer@upi'
  };

  cropsMap: Record<number, string> = {};

  private route = inject(ActivatedRoute);
  pendingPayOrderId: string | number | null = null;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const p = params['payOrder'] || params['orderId'];
      if (p) {
        this.pendingPayOrderId = p;
      }
    });
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
    const role = this.auth.role();
    const uid = this.auth.userId();

    let path = '/orders';
    if (role === 'FARMER' && uid) {
      path = '/orders/farmer/' + uid;
    } else if (role === 'DEALER' && uid) {
      path = '/orders/dealer/' + uid;
    }

    this.api.get<Order[]>(path).subscribe({
      next: (list: Order[]) => {
        this.orders = list || [];
        this.fetchCropNames(this.orders);
        if (this.pendingPayOrderId) {
          const match = this.orders.find(o => String(o.id) === String(this.pendingPayOrderId) || o.orderNumber === String(this.pendingPayOrderId));
          if (match) {
            this.openPaymentModal(match);
            this.pendingPayOrderId = null;
          }
        }
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not load orders', 'error');
      }
    });
  }

  fetchCropNames(orders: Order[]) {
    const uniqueCropIds = [...new Set(orders.map(o => o.cropId).filter(Boolean))];
    uniqueCropIds.forEach(id => {
      if (!this.cropsMap[id]) {
        this.api.get<Crop>('/crops/' + id).subscribe({
          next: (c) => {
            this.cropsMap[id] = c.cropName || 'Farm Harvest';
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
      case 'COMPLETED': return '#eef5ee';
      case 'PAID': return '#e8f0fe';
      case 'ACCEPTED': return '#eafaf1';
      case 'REJECTED': case 'CANCELLED': return '#ffebee';
      default: return '#fff8e1';
    }
  }

  statusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': case 'ACCEPTED': return 'var(--green)';
      case 'PAID': return '#1a73e8';
      case 'REJECTED': case 'CANCELLED': return 'var(--red)';
      default: return '#b78103';
    }
  }

  acceptOrder(id: number) {
    this.api.patch('/orders/' + id + '/accept').subscribe({
      next: () => {
        this.toast.show('Order accepted! Dealer can now deposit escrow payment.');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not accept order', 'error');
      }
    });
  }

  rejectOrder(id: number) {
    if (!confirm('Reject this order?')) return;
    this.api.patch('/orders/' + id + '/reject').subscribe({
      next: () => {
        this.toast.show('Order rejected');
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not reject order', 'error');
      }
    });
  }

  cancelOrder(o: Order) {
    const isPaid = (o.status === 'PAID' || o.paymentStatus === 'SUCCESS');
    const msg = `Are you sure you want to cancel Order #${o.orderNumber || o.id}?` +
      (isPaid ? ' Any escrowed payment will be refunded to your wallet.' : '');
    if (!confirm(msg)) return;

    this.api.post<Order>('/orders/' + o.id + '/cancel', {}).subscribe({
      next: () => {
        this.toast.show(`Order #${o.orderNumber || o.id} has been cancelled.` +
          (isPaid ? ' Escrow funds refunded to your wallet.' : ''));
        if (this.deliveryOrder?.id === o.id) this.deliveryOrder = undefined;
        if (this.paymentOrder?.id === o.id) this.paymentOrder = undefined;
        if (this.qrFarmerOrder?.id === o.id) {
          this.qrFarmerOrder = undefined;
          this.farmerQrToken = '';
          this.qrImageUrl = '';
        }
        this.load();
      },
      error: (e: any) => {
        this.toast.show(e.error?.message || 'Could not cancel order', 'error');
      }
    });
  }

  openPaymentModal(o: Order) {
    this.paymentOrder = o;
    this.fillDemoCard();
  }

  fillDemoCard() {
    this.pay = {
      method: 'CREDIT_CARD',
      card: '4111222233334444',
      holder: 'Dealer Buyer',
      expiry: '12/28',
      cvv: '123',
      upi: 'dealer@upi'
    };
  }

  fillDemoUpi() {
    this.pay = {
      method: 'UPI',
      card: '',
      holder: '',
      expiry: '',
      cvv: '',
      upi: 'dealer@upi'
    };
  }

  submitPayment() {
    if (!this.paymentOrder) return;

    const payload: any = {
      orderId: this.paymentOrder.id,
      paymentMethod: this.pay.method
    };

    if (this.pay.method === 'CREDIT_CARD' || this.pay.method === 'DEBIT_CARD') {
      if (!this.pay.card || !/^[0-9]{16}$/.test(this.pay.card)) {
        this.toast.show('Card number must be exactly 16 digits', 'error');
        return;
      }
      if (!this.pay.holder || !this.pay.holder.trim()) {
        this.toast.show('Card holder name is required', 'error');
        return;
      }
      if (!this.pay.expiry || !/^(0[1-9]|1[0-2])\/([0-9]{2})$/.test(this.pay.expiry)) {
        this.toast.show('Expiry must be MM/YY', 'error');
        return;
      }
      if (!this.pay.cvv || !/^[0-9]{3}$/.test(this.pay.cvv)) {
        this.toast.show('CVV must be 3 digits', 'error');
        return;
      }
      payload.dealerCardNumber = this.pay.card;
      payload.dealerCardHolderName = this.pay.holder.trim();
      payload.dealerCardExpiry = this.pay.expiry;
      payload.dealerCvv = this.pay.cvv;
    } else if (this.pay.method === 'UPI') {
      if (!this.pay.upi || !/^[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}$/.test(this.pay.upi)) {
        this.toast.show('Enter a valid UPI ID (e.g. dealer@upi)', 'error');
        return;
      }
      payload.dealerUpiId = this.pay.upi.trim();
    }

    this.paying = true;

    // Direct POST /payments completes the payment in the backend and moves dealer funds into escrow!
    this.api.post<Payment>('/payments', payload).subscribe({
      next: (p: Payment) => {
        this.paying = false;
        this.paymentOrder = undefined;
        this.toast.show('Payment successful! Funds held safely in escrow pending delivery.');
        this.load();
      },
      error: (e: any) => {
        this.paying = false;
        this.toast.show(e.error?.message || 'Payment failed. Verify details and try again.', 'error');
      }
    });
  }

  showFarmerQr(o: Order) {
    this.qrFarmerOrder = o;
    this.farmerQrToken = '';
    this.qrImageUrl = '';

    // Load delivery token as plain text
    this.api.getText('/orders/' + o.id + '/delivery-token').subscribe({
      next: (token: string) => {
        this.farmerQrToken = token;
      },
      error: () => {
        this.farmerQrToken = o.deliveryQrCode || 'Token available';
      }
    });

    // Load QR code image with Bearer authentication as a Blob
    this.api.getBlob('/orders/' + o.id + '/delivery-qr').subscribe({
      next: (blob: Blob) => {
        this.qrImageUrl = URL.createObjectURL(blob);
      },
      error: () => {
        this.qrImageUrl = '';
      }
    });
  }

  copyToken() {
    if (this.farmerQrToken) {
      navigator.clipboard?.writeText(this.farmerQrToken);
      this.toast.show('Delivery token copied to clipboard!');
    }
  }

  onQrImageError(e: any) {
    // If QR image doesn't load or image endpoint not binary, display fallback
  }

  openDeliveryModal(o: Order) {
    this.deliveryOrder = o;
    this.deliveryTokenInput = '';
  }

  confirmDelivery() {
    if (!this.deliveryOrder) return;
    if (!this.deliveryTokenInput || !this.deliveryTokenInput.trim()) {
      this.toast.show('Please enter the delivery QR token provided by the farmer', 'error');
      return;
    }

    this.delivering = true;
    this.api.post<Order>('/orders/' + this.deliveryOrder.id + '/confirm-delivery', {
      qrCode: this.deliveryTokenInput.trim()
    }).subscribe({
      next: () => {
        this.delivering = false;
        this.toast.show('Delivery confirmed! Escrow funds released to farmer. Order completed.');
        this.deliveryOrder = undefined;
        this.load();
      },
      error: (e: any) => {
        this.delivering = false;
        this.toast.show(e.error?.message || 'Delivery confirmation failed. Invalid or expired QR token.', 'error');
      }
    });
  }

  viewInvoice(o: Order) {
  this.api.get<Invoice>('/reports/invoice/' + o.id).subscribe({
    next: (inv: Invoice) => {
      this.activeInvoice = {
        ...inv,
        orderId: o.id,
        orderNumber: inv.orderNumber || o.orderNumber,
        dealerId: o.dealerId,
        farmerId: o.farmerId,
        cropId: o.cropId,
        quantity: inv.quantity ?? o.quantity,
        unitPrice: inv.agreedPrice ?? inv.unitPrice ?? o.agreedPrice,
        totalAmount: inv.totalAmount ?? o.totalAmount,
        orderDate: o.createdAt,
        generatedAt: new Date().toISOString()
      };
    },

    error: () => {
      this.activeInvoice = {
        orderId: o.id,
        orderNumber: o.orderNumber,
        dealerId: o.dealerId,
        farmerId: o.farmerId,
        cropId: o.cropId,
        quantity: o.quantity,
        unitPrice: o.agreedPrice,
        totalAmount: o.totalAmount,
        orderDate: o.createdAt,
        generatedAt: new Date().toISOString()
      };
    }
  });
}

  openReviewModal(o: Order) {
    this.reviewOrder = o;
    this.reviewRating = 5;
    this.reviewComment = '';
  }

  submitReview() {
    if (!this.reviewOrder) return;

    this.submittingReview = true;
    const body: Review = {
      orderId: this.reviewOrder.id,
      rating: Number(this.reviewRating),
      review: this.reviewComment.trim() || 'Good transaction.'
    };

    this.api.post<Review>('/reviews', body).subscribe({
      next: () => {
        this.submittingReview = false;
        this.toast.show('Review submitted successfully! Thank you for rating.');
        this.reviewOrder = undefined;
      },
      error: (e: any) => {
        this.submittingReview = false;
        this.toast.show(e.error?.message || 'Could not submit review', 'error');
      }
    });
  }

  downloadInvoicePdf() {
  if (!this.activeInvoice) {
    return;
  }

  const invoice = this.activeInvoice;
  const doc = new jsPDF();

  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();

  // =========================
  // HEADER
  // =========================
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(22);
  doc.text('CROPDEAL', pageWidth / 2, 20, {
    align: 'center'
  });

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(11);
  doc.text('Farm to Market', pageWidth / 2, 27, {
    align: 'center'
  });

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(16);
  doc.text('INVOICE', pageWidth / 2, 40, {
    align: 'center'
  });

  doc.line(15, 46, pageWidth - 15, 46);

  // =========================
  // INVOICE INFORMATION
  // =========================
  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');

  doc.text(
    `Invoice / Order No: ${invoice.orderNumber}`,
    20,
    58
  );

  doc.text(
    `Order Date: ${
      invoice.orderDate
        ? new Date(invoice.orderDate).toLocaleDateString('en-IN')
        : '-'
    }`,
    20,
    66
  );

  doc.text(
    `Generated: ${
      invoice.generatedAt
        ? new Date(invoice.generatedAt).toLocaleDateString('en-IN')
        : new Date().toLocaleDateString('en-IN')
    }`,
    20,
    74
  );

  // =========================
  // PARTY DETAILS
  // =========================
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(11);
  doc.text('PARTY DETAILS', 20, 91);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);

  doc.text(
    `Farmer: ${invoice.farmerName || `#${invoice.farmerId ?? '-'}`}`,
    20,
    101
  );

  doc.text(
    `Dealer: ${invoice.dealerName || `#${invoice.dealerId ?? '-'}`}`,
    20,
    109
  );

  // =========================
  // CROP / ORDER DETAILS
  // =========================
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(11);
  doc.text('CROP / ORDER DETAILS', 20, 128);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);

  doc.text(
    `Crop: ${invoice.cropName || this.cropsMap[invoice.cropId || 0] || 'Farm Harvest'}`,
    20,
    138
  );

  doc.text(
    `Quantity: ${invoice.quantity} quintals`,
    20,
    146
  );

  doc.text(
    `Unit Price: ${this.formatCurrency(invoice.unitPrice)} / quintal`,
    20,
    154
  );

  // =========================
  // TOTAL
  // =========================
  doc.line(20, 166, pageWidth - 20, 166);

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(12);

  doc.text(
    'TOTAL AMOUNT',
    20,
    181
  );

  doc.text(
    this.formatCurrency(invoice.totalAmount),
    pageWidth - 20,
    181,
    { align: 'right' }
  );

  // =========================
  // STATUS
  // =========================
  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');

  doc.text(
    'Payment Status: SUCCESS',
    20,
    198
  );

  doc.text(
    `Order Status: ${invoice.orderStatus || 'COMPLETED'}`,
    20,
    206
  );

  // =========================
  // FOOTER
  // =========================
  doc.line(15, 220, pageWidth - 15, 220);

  doc.setFontSize(9);

  doc.text(
    'Thank you for using CropDeal.',
    pageWidth / 2,
    232,
    { align: 'center' }
  );

  doc.text(
    'Farm to Market',
    pageWidth / 2,
    239,
    { align: 'center' }
  );

  // =========================
  // DOWNLOAD
  // =========================
  doc.save(
    `Invoice-${invoice.orderNumber}.pdf`
  );
}

private formatCurrency(value: number): string {
  return 'Rs. ' + Number(value || 0).toLocaleString('en-IN');
}
}
