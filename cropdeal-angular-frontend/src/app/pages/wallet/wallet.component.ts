import { Component, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, CurrencyPipe, DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Wallet, FarmerWallet, WalletTransaction } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [
    CommonModule,
    NgFor,
    NgIf,
    CurrencyPipe,
    DatePipe,
    FormsModule
  ],
  template: `
    <div class="page">
      <div class="split">
        <div>
          <div class="eyebrow">ESCROW & FINANCIAL BALANCES</div>
          <h1 class="title">Settlement Wallet</h1>
          <p class="sub">
            {{ auth.role() === 'FARMER'
               ? 'View your earned proceeds from delivered crops, credited directly upon dealer QR confirmation.'
               : 'Manage your purchasing funds and monitor trade deposits safely held in escrow.' }}
          </p>
        </div>

        <div style="display: flex; gap: 10px;">
          <button
            *ngIf="auth.role() === 'DEALER'"
            class="btn btn-primary"
            (click)="showCreditModal = true">
            + Add Funds to Wallet
          </button>

          <button class="btn btn-light" (click)="load()">
            ↻ Refresh
          </button>
        </div>
      </div>

      <!-- Financial Overview Cards -->
      <div class="grid grid-3" style="margin-top: 24px;">

        <!-- Available Balance -->
        <div class="card stat">
          <div class="icon">💰</div>

          <div class="number" style="color: var(--green);">
            {{
              (auth.role() === 'FARMER'
                ? farmerWallet?.balance
                : dealerWallet?.balance)
              | currency:'INR':'symbol':'1.0-0'
            }}
          </div>

          <div class="label">
            Available Wallet Balance
          </div>
        </div>

        <!-- Escrow / Earnings -->
        <div class="card stat">
          <div class="icon">
            {{ auth.role() === 'FARMER' ? '📈' : '🔒' }}
          </div>

          <div class="number">
            {{
              (auth.role() === 'FARMER'
                ? farmerWallet?.totalEarnings
                : dealerWallet?.blockedBalance)
              | currency:'INR':'symbol':'1.0-0'
            }}
          </div>

          <div class="label">
            {{
              auth.role() === 'FARMER'
                ? 'Lifetime Total Earnings'
                : 'Funds Currently in Escrow'
            }}
          </div>
        </div>

        <!-- Status -->
        <div class="card stat">
          <div class="icon">🛡️</div>

          <div
            class="number"
            style="font-size: 20px; font-weight: 700; margin-top: 14px;">
            100% Escrow Protected
          </div>

          <div class="label">
            Zero Counterparty Risk
          </div>
        </div>
      </div>


      <!-- ============================= -->
      <!-- ADD FUNDS MODAL / FORM -->
      <!-- ============================= -->

      <div
        *ngIf="showCreditModal"
        class="card form-card"
        style="
          margin-top: 24px;
          border: 2px solid var(--green);
          animation: fadeIn .2s ease-out;
        ">

        <div class="split" style="margin-bottom: 18px;">
          <div>
            <div
              class="eyebrow"
              style="color: var(--green);">
              DEALER WALLET TOP-UP
            </div>

            <h2 style="margin: 4px 0;">
              Add Funds to Your Wallet
            </h2>

            <p class="sub" style="margin-bottom: 0;">
              Enter your bank transfer details to add funds to your dealer wallet.
            </p>
          </div>

          <button
            class="btn btn-light btn-small"
            (click)="closeCreditModal()">
            ✕ Close
          </button>
        </div>


        <!-- Security Notice -->
        <div style="background: #eef7e6; border: 1px solid #bce19d; border-radius: 12px; padding: 12px 16px; margin-bottom: 18px; font-size: 13px; color: #1e5927; display: flex; align-items: center; justify-content: space-between; gap: 12px;">
          <div>
            🛡️ <b>Security Profile Verification:</b> Submitted financial details are checked against your registered profile. Random details will be rejected.
          </div>
          <button type="button" class="btn btn-light btn-small" (click)="fillFromProfile()" style="white-space: nowrap; font-weight: 800;">
            ✓ Use Profile Details
          </button>
        </div>

        <!-- Quick Amount Buttons -->
        <div style="display: flex; gap: 8px; margin-bottom: 18px; flex-wrap: wrap;">
          <button type="button" class="btn btn-light btn-small" (click)="creditAmount = 10000">+ ₹10,000</button>
          <button type="button" class="btn btn-light btn-small" (click)="creditAmount = 50000">+ ₹50,000</button>
          <button type="button" class="btn btn-light btn-small" (click)="creditAmount = 100000">+ ₹1,00,000</button>
        </div>

        <!-- Amount -->
        <div class="field">
          <label>Top-up Amount (₹) *</label>
          <input type="number" [(ngModel)]="creditAmount" min="1" placeholder="e.g. 50000">
        </div>

        <!-- Payment Method -->
        <div class="field" style="margin-top: 12px;">
          <label>Payment Method *</label>
          <select [(ngModel)]="paymentMethod">
            <option value="">Select payment method</option>
            <option value="IMPS">IMPS (Bank Transfer)</option>
            <option value="NEFT">NEFT (Bank Transfer)</option>
            <option value="UPI">UPI (Unified Payments Interface)</option>
          </select>
        </div>

        <!-- UPI specific field -->
        <div class="field" *ngIf="paymentMethod === 'UPI'" style="margin-top: 12px;">
          <label>Verified UPI ID *</label>
          <input type="text" [(ngModel)]="upiId" placeholder="e.g. dealer@upi" autocomplete="off">
          <small class="muted">Must match UPI ID registered in your CropDeal profile.</small>
        </div>

        <!-- Bank Transfer Fields -->
        <ng-container *ngIf="paymentMethod !== 'UPI'">
          <div class="field" style="margin-top: 12px;">
            <label>Account Holder Name *</label>
            <input type="text" [(ngModel)]="accountHolderName" placeholder="Enter account holder name" autocomplete="off">
          </div>

          <div class="field" style="margin-top: 12px;">
            <label>Bank Name *</label>
            <input type="text" [(ngModel)]="bankName" placeholder="e.g. State Bank of India" autocomplete="off">
          </div>

          <div class="field" style="margin-top: 12px;">
            <label>Account Number (9-18 digits) *</label>
            <input type="text" [(ngModel)]="accountNumber" maxlength="18" inputmode="numeric" placeholder="Enter bank account number" autocomplete="off">
          </div>

          <div class="field" style="margin-top: 12px;">
            <label>IFSC Code *</label>
            <input type="text" [(ngModel)]="ifscCode" maxlength="11" placeholder="e.g. SBIN0001234" style="text-transform: uppercase;" autocomplete="off">
          </div>
        </ng-container>

        <!-- MPIN -->
        <div class="field" style="margin-top: 12px;">
          <label>MPIN (4 or 6 digits) *</label>
          <input type="password" [(ngModel)]="mpin" maxlength="6" inputmode="numeric" placeholder="Enter 4 or 6 digit MPIN" autocomplete="off">
        </div>

        <!-- Action Buttons -->
        <div style="display: flex; gap: 12px; margin-top: 22px;">
          <button class="btn btn-primary" (click)="submitCredit()" [disabled]="crediting">
            {{ crediting ? 'Verifying & Depositing…' : 'Deposit to Wallet' }}
          </button>
          <button class="btn btn-light" (click)="closeCreditModal()" [disabled]="crediting">
            Cancel
          </button>
        </div>
      </div>

      <!-- TRANSACTION HISTORY -->
      <div class="card table-wrap" style="margin-top: 24px;" *ngIf="auth.role() === 'DEALER'">
        <div style="padding: 18px 20px; border-bottom: 1px solid var(--line);">
          <h3 style="margin: 0;">Recent Wallet Transactions</h3>
        </div>

        <table class="table" *ngIf="transactions.length; else noTx">
          <thead>
            <tr>
              <th>Date & Time</th>
              <th>TX ID</th>
              <th>Transaction Type</th>
              <th>Method</th>
              <th>Status</th>
              <th>Description / Reference</th>
              <th>Amount</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let t of transactions">
              <td>{{ t.createdAt | date:'medium' }}</td>
              <td><b>#{{ t.id }}</b></td>
              <td><span class="badge">{{ t.transactionType }}</span></td>
              <td><span class="badge" style="background: #f0f2ef; color: #444;">{{ t.paymentMethod || 'BANK' }}</span></td>
              <td><span class="badge" style="background: #eef5ee; color: var(--green);">{{ t.status || 'SUCCESS' }}</span></td>
              <td class="muted" style="font-size: 12px;">{{ t.description || t.reference || 'Wallet Settlement' }}</td>
              <td>
                <b [style.color]="t.transactionType?.includes('CREDIT') ? 'var(--green)' : 'var(--ink)'">
                  {{ t.amount | currency:'INR':'symbol':'1.0-0' }}
                </b>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #noTx>
          <div class="empty">
            No recent transactions on this wallet.
          </div>
        </ng-template>

      </div>
    </div>
  `
})
export class WalletComponent implements OnInit {

  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  dealerWallet?: Wallet;
  farmerWallet?: FarmerWallet;
  transactions: WalletTransaction[] = [];

  showCreditModal = false;

  creditAmount = 50000;

  // Bank transfer details
  accountHolderName = '';
  bankName = '';
  accountNumber = '';
  ifscCode = '';
  upiId = '';
  paymentMethod = '';
  mpin = '';

  crediting = false;

  ngOnInit() {
    this.ensureUserAndLoad();
  }

  ensureUserAndLoad() {
    if (!this.auth.userId()) {
      this.auth.loadProfile().subscribe(() => {
        this.load();
      });
    } else {
      this.load();
    }
  }

  fillFromProfile() {
    const u = this.auth.currentUser();
    if (u) {
      this.populateFields(u);
    } else {
      this.auth.loadProfile().subscribe(user => {
        if (user) this.populateFields(user);
      });
    }
  }

  private populateFields(u: any) {
    this.accountHolderName = u.accountHolderName || u.name || '';
    this.bankName = u.bankName || 'State Bank of India';
    this.accountNumber = u.bankAccountNumber || '';
    this.ifscCode = u.ifscCode || '';
    this.upiId = u.upiId || '';
    this.toast.show('Populated financial details from your verified profile');
  }

  load() {
    const role = this.auth.role();
    const uid = this.auth.userId();

    if (!uid) return;

    if (role === 'FARMER') {
      this.api.get<FarmerWallet>('/wallets/farmer/' + uid).subscribe({
        next: (w: FarmerWallet) => {
          this.farmerWallet = w;
        },
        error: () => {
          this.farmerWallet = {
            farmerId: uid,
            balance: 0,
            totalEarnings: 0
          };
        }
      });
    } else if (role === 'DEALER') {
      this.api.get<Wallet>('/wallets/' + uid).subscribe({
        next: (w: Wallet) => {
          this.dealerWallet = w;
        },
        error: () => {
          this.dealerWallet = {
            dealerId: uid,
            balance: 0,
            blockedBalance: 0
          };
        }
      });

      this.api.get<WalletTransaction[]>('/wallets/' + uid + '/transactions').subscribe({
        next: (txs: WalletTransaction[]) => {
          this.transactions = txs || [];
        },
        error: () => {
          this.transactions = [];
        }
      });
    }
  }

  submitCredit() {
    const uid = this.auth.userId();
    if (!uid) return;

    if (!this.creditAmount || this.creditAmount <= 0) {
      this.toast.show('Please enter a valid deposit amount', 'error');
      return;
    }

    if (!this.paymentMethod) {
      this.toast.show('Please select a payment method', 'error');
      return;
    }

    if (this.paymentMethod === 'UPI') {
      if (!this.upiId || !this.upiId.trim()) {
        this.toast.show('Please enter your UPI ID', 'error');
        return;
      }
    } else {
      if (!this.accountHolderName.trim()) {
        this.toast.show('Please enter account holder name', 'error');
        return;
      }
      if (!this.accountNumber || !/^[0-9]{9,18}$/.test(this.accountNumber.trim())) {
        this.toast.show('Account number must be 9 to 18 digits', 'error');
        return;
      }
      if (!this.ifscCode || !/^[A-Z]{4}0[A-Z0-9]{6}$/.test(this.ifscCode.trim().toUpperCase())) {
        this.toast.show('Please enter a valid IFSC code (e.g. SBIN0001234)', 'error');
        return;
      }
    }

    if (!/^([0-9]{4}|[0-9]{6})$/.test(this.mpin)) {
      this.toast.show('MPIN must be 4 or 6 digits', 'error');
      return;
    }

    this.crediting = true;

    const request: any = {
      amount: Number(this.creditAmount),
      paymentMethod: this.paymentMethod,
      accountHolderName: this.accountHolderName.trim(),
      bankName: this.bankName.trim(),
      accountNumber: this.accountNumber.trim(),
      ifscCode: this.ifscCode.trim().toUpperCase(),
      upiId: this.upiId.trim(),
      mpin: this.mpin
    };

    this.api.post<Wallet>('/wallets/' + uid + '/credit', request).subscribe({
      next: (w: Wallet) => {
        this.crediting = false;
        this.dealerWallet = w;
        this.showCreditModal = false;
        this.toast.show('₹' + Number(this.creditAmount).toLocaleString('en-IN') + ' added to wallet successfully!');
        this.resetCreditForm();
        this.load();
      },
      error: (e: any) => {
        this.crediting = false;
        this.toast.show(e?.error?.message || 'Could not credit wallet', 'error');
      }
    });
  }

  closeCreditModal() {
    if (this.crediting) return;
    this.showCreditModal = false;
  }

  resetCreditForm() {
    this.creditAmount = 50000;
    this.accountHolderName = '';
    this.bankName = '';
    this.accountNumber = '';
    this.ifscCode = '';
    this.upiId = '';
    this.paymentMethod = '';
    this.mpin = '';
  }
}
