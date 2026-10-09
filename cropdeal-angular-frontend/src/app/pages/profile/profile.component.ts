import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { User } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [FormsModule, NgIf, NgFor],
  template: `
    <div class="page">
      <div class="eyebrow">ACCOUNT PROFILE</div>
      <h1 class="title">Your Profile & Settlement Details</h1>
      <p class="sub">Keep your contact and banking information up to date for smooth marketplace transactions.</p>

      <div *ngIf="isNewProfile" class="card" style="padding: 16px 20px; margin-bottom: 24px; background: #fdfae5; border: 1px solid #f2e29f; display: flex; align-items: center; gap: 14px;">
        <span style="font-size: 24px;">💡</span>
        <div>
          <b style="color: #7d5e12;">Profile Incomplete</b>
          <div style="font-size: 13px; color: #8c6b16;">Welcome to CropDeal! Please fill in your name, contact phone, and location below to activate your marketplace profile.</div>
        </div>
      </div>

      <div class="card form-card">
        <div class="split" style="margin-bottom: 24px;">
          <div>
            <h2 style="margin: 0;">{{ form.name || 'Setup Your Profile' }}</h2>
            <div style="display: flex; gap: 8px; margin-top: 6px; align-items: center;">
              <span class="badge">{{ form.role || auth.role() }}</span>
              <span *ngIf="!isNewProfile" class="badge" [style.background]="form.active ? '#eef5ee' : '#ffebee'" [style.color]="form.active ? 'var(--green)' : 'var(--red)'">
                {{ form.active ? 'Active Profile' : 'Inactive' }}
              </span>
              <span *ngIf="isNewProfile" class="badge" style="background: #fff8e1; color: #b78103;">New Account</span>
            </div>
          </div>
          <div class="avatar" style="width: 56px; height: 56px; font-size: 22px;">
            {{ (form.name || 'U').charAt(0).toUpperCase() }}
          </div>
        </div>

        <form (ngSubmit)="save()">
          <h3 style="margin: 18px 0 12px; font-size: 16px; color: var(--green);">Basic & Contact Information</h3>
          <div class="form-grid">
            <div class="field">
              <label>Full Name *</label>
              <input [(ngModel)]="form.name" name="name" required minlength="3" placeholder="e.g. Ramesh Kumar">
            </div>
            <div class="field">
              <label>Mobile Number (10 digits) *</label>
              <input [(ngModel)]="form.phone" name="phone" required pattern="^[6-9][0-9]{9}$" placeholder="e.g. 9876543210">
            </div>
            <div class="field">
              <label>Email Address</label>
              <input [(ngModel)]="form.email" name="email" readonly style="background: #f0f2ef; color: #53615a;">
            </div>
            <div class="field">
              <label>Role</label>
              <input [value]="form.role || auth.role()" readonly style="background: #f0f2ef; color: #53615a;">
            </div>
            <div class="field" style="grid-column: span 2;">
              <label>Street Address / Farm Address *</label>
              <input [(ngModel)]="form.address" name="address" required minlength="5" placeholder="e.g. 142 Green Fields, North Bypass Road">
            </div>
            <div class="field">
              <label>City / Town *</label>
              <input [(ngModel)]="form.city" name="city" required placeholder="e.g. Coimbatore">
            </div>
            <div class="field">
              <label>State *</label>
              <select [(ngModel)]="form.state" name="state" required>
                <option value="" disabled>Select State / UT</option>
                <option *ngFor="let s of indianStates" [value]="s">{{ s }}</option>
              </select>
            </div>
            <div class="field">
              <label>PIN Code (6 digits) *</label>
              <input [(ngModel)]="form.pincode" name="pincode" required pattern="^[1-9][0-9]{5}$" maxlength="6" placeholder="e.g. 641001">
            </div>
          </div>

          <h3 style="margin: 28px 0 12px; font-size: 16px; color: var(--green);">Settlement & Banking Information</h3>
          <p class="muted" style="font-size: 12px; margin-top: -6px; margin-bottom: 14px;">Used for direct escrow payout upon harvest delivery confirmation and wallet operations.</p>
          <div class="form-grid">
            <div class="field">
              <label>Account Holder Name</label>
              <input [(ngModel)]="form.accountHolderName" name="accountHolderName" placeholder="As per bank passbook">
            </div>
            <div class="field">
              <label>UPI ID (Instant Settlement)</label>
              <input [(ngModel)]="form.upiId" name="upiId" placeholder="name@okaxis or mobile@upi">
            </div>
            <div class="field">
              <label>Bank Name</label>
              <input [(ngModel)]="form.bankName" name="bankName" placeholder="e.g. State Bank of India">
            </div>
            <div class="field">
              <label>Branch Name</label>
              <input [(ngModel)]="form.branch" name="branch" placeholder="e.g. Main Branch, Coimbatore">
            </div>
            <div class="field">
              <label>Bank Account Number (9-18 digits)</label>
              <input [(ngModel)]="form.bankAccountNumber" name="bankAccountNumber" placeholder="Account number">
            </div>
            <div class="field">
              <label>IFSC Code</label>
              <input [(ngModel)]="form.ifscCode" name="ifscCode" placeholder="e.g. SBIN0001234">
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 15px; margin-top: 26px;">
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              {{ saving ? 'Saving Profile…' : (isNewProfile ? 'Create & Activate Profile' : 'Update Profile') }}
            </button>
            <span *ngIf="saved" style="color: var(--green); font-weight: 700;">✓ Profile saved successfully!</span>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);

  form: any = {};
  isNewProfile = false;
  saving = false;
  saved = false;

  indianStates: string[] = [
    'Andhra Pradesh',
    'Arunachal Pradesh',
    'Assam',
    'Bihar',
    'Chhattisgarh',
    'Goa',
    'Gujarat',
    'Haryana',
    'Himachal Pradesh',
    'Jharkhand',
    'Karnataka',
    'Kerala',
    'Madhya Pradesh',
    'Maharashtra',
    'Manipur',
    'Meghalaya',
    'Mizoram',
    'Nagaland',
    'Odisha',
    'Punjab',
    'Rajasthan',
    'Sikkim',
    'Tamil Nadu',
    'Telangana',
    'Tripura',
    'Uttar Pradesh',
    'Uttarakhand',
    'West Bengal',
    'Andaman and Nicobar Islands',
    'Chandigarh',
    'Dadra and Nagar Haveli and Daman and Diu',
    'Delhi',
    'Jammu and Kashmir',
    'Ladakh',
    'Lakshadweep',
    'Puducherry'
  ];

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    const aid = this.auth.authUserId();
    if (!aid) {
      this.toast.show('Please log in first', 'error');
      return;
    }

    this.api.get<User>('/users/auth/' + aid).subscribe({
      next: (user: User) => {
        if (user && user.id) {
          this.auth.setUserId(user.id);
          this.auth.currentUser.set(user);
          this.form = { ...user };
          this.isNewProfile = false;
        } else {
          this.prepareNewProfile(aid);
        }
      },
      error: () => {
        // 404 indicates user has an auth account but no user profile record yet
        this.prepareNewProfile(aid);
      }
    });
  }

  prepareNewProfile(authUserId: number) {
    this.isNewProfile = true;
    this.form = {
      authUserId,
      email: this.auth.email(),
      role: this.auth.role() || 'FARMER',
      name: '',
      phone: '',
      address: '',
      city: '',
      state: '',
      pincode: '',
      bankAccountNumber: '',
      bankName: '',
      ifscCode: '',
      upiId: '',
      active: true
    };
  }

  save() {
    if (!this.form.name || this.form.name.trim().length < 3) {
      this.toast.show('Full Name must be at least 3 characters', 'error');
      return;
    }
    if (!this.form.phone || !/^[6-9][0-9]{9}$/.test(this.form.phone.trim())) {
      this.toast.show('Enter a valid 10-digit Indian mobile number starting with 6-9', 'error');
      return;
    }
    if (!this.form.address || this.form.address.trim().length < 5) {
      this.toast.show('Address must be at least 5 characters', 'error');
      return;
    }
    if (!this.form.city || !this.form.city.trim()) {
      this.toast.show('City is required', 'error');
      return;
    }
    if (!this.form.state || !this.form.state.trim()) {
      this.toast.show('State is required', 'error');
      return;
    }
    if (!this.form.pincode || !/^[1-9][0-9]{5}$/.test(this.form.pincode.trim())) {
      this.toast.show('Enter a valid 6-digit Indian PIN code (numeric)', 'error');
      return;
    }

    // Clean optional bank fields so empty string doesn't fail pattern validation
    const payload: any = {
      authUserId: this.auth.authUserId(),
      name: this.form.name.trim(),
      email: this.form.email || this.auth.email(),
      phone: this.form.phone.trim(),
      address: this.form.address.trim(),
      city: this.form.city.trim(),
      state: this.form.state.trim(),
      pincode: this.form.pincode.trim(),
      role: this.form.role || this.auth.role(),
      accountHolderName: this.form.accountHolderName?.trim() || undefined,
      branch: this.form.branch?.trim() || undefined,
      upiId: this.form.upiId?.trim() || undefined,
      bankName: this.form.bankName?.trim() || undefined
    };

    if (this.form.bankAccountNumber && this.form.bankAccountNumber.trim()) {
      const acct = this.form.bankAccountNumber.trim();
      if (!/^[0-9]{9,18}$/.test(acct)) {
        this.toast.show('Bank account number must be between 9 and 18 digits', 'error');
        return;
      }
      payload.bankAccountNumber = acct;
    }

    if (this.form.ifscCode && this.form.ifscCode.trim()) {
      const ifsc = this.form.ifscCode.trim().toUpperCase();
      if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifsc)) {
        this.toast.show('Enter a valid IFSC code (e.g. SBIN0001234)', 'error');
        return;
      }
      payload.ifscCode = ifsc;
    }

    this.saving = true;
    this.saved = false;

    if (this.isNewProfile || !this.auth.userId()) {
      // POST /users to create new profile
      this.api.post<User>('/users', payload).subscribe({
        next: (created: User) => {
          this.saving = false;
          this.saved = true;
          this.isNewProfile = false;
          this.form = { ...created };
          this.auth.setUserId(created.id);
          this.auth.currentUser.set(created);
          this.toast.show('Profile created successfully! You can now participate in deals.');
        },
        error: (err: any) => {
          this.saving = false;
          this.toast.show(err.error?.message || 'Profile creation failed. Check the fields.', 'error');
        }
      });
    } else {
      // PUT /users/{id} to update existing profile
      this.api.put<User>('/users/' + this.auth.userId(), payload).subscribe({
        next: (updated: User) => {
          this.saving = false;
          this.saved = true;
          this.form = { ...updated };
          this.auth.currentUser.set(updated);
          this.toast.show('Profile updated successfully');
        },
        error: (err: any) => {
          this.saving = false;
          this.toast.show(err.error?.message || 'Profile update failed', 'error');
        }
      });
    }
  }
}
