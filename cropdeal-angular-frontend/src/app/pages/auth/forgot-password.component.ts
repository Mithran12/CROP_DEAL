import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgIf, CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf, CommonModule],
  template: `
    <div class="auth-page">
      <section class="auth-visual">
        <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 24px;">
          <div style="width: 48px; height: 48px; border-radius: 14px; background: #cfe96b; display: grid; place-items: center; font-size: 26px;">🌾</div>
          <span style="font-family: 'Plus Jakarta Sans', sans-serif; font-weight: 800; font-size: 28px; letter-spacing: -0.02em; color: #fff;">
            CROP<span style="color: #cfe96b;">DEAL</span>
          </span>
        </div>
        <div class="eyebrow" style="color: #cfe96b;">ACCOUNT SECURITY</div>
        <h1>Reset Your Password Securely</h1>
        <p>
          Protecting your CropDeal farmer and dealer accounts with real-time Email OTP verification.
        </p>
      </section>

      <section class="auth-form-wrap">
        <!-- STEP 1: REQUEST OTP -->
        <div class="auth-card card" *ngIf="step === 'email'">
          <div class="brand-row" style="margin-bottom: 20px;">
            <div class="brand-badge" style="font-size: 22px;">🔒</div>
            <div>
              <div style="font-weight: 800; font-size: 16px;">CROPDEAL</div>
              <div style="font-size: 12px; color: var(--muted);">Password Reset Request</div>
            </div>
          </div>

          <h2>Forgot Password?</h2>
          <p class="sub">Enter your registered email address to receive a secure 6-digit OTP verification code.</p>

          <form (ngSubmit)="sendOtp()">
            <div class="field">
              <label>Registered Email Address *</label>
              <input type="email"
                     name="email"
                     [(ngModel)]="email"
                     required
                     placeholder="you@example.com"
                     autocomplete="email"
                     [disabled]="loading">
            </div>

            <div *ngIf="errorMessage" class="error-banner" style="margin-top: 14px; padding: 10px 14px; background: #fee2e2; border: 1px solid #f87171; border-radius: 8px; color: #b91c1c; font-size: 13px;">
              {{ errorMessage }}
            </div>

            <button type="submit"
                    class="btn btn-primary full"
                    style="margin-top: 22px;"
                    [disabled]="loading || !email">
              {{ loading ? 'Sending OTP…' : 'Send OTP' }}
            </button>
          </form>

          <div style="text-align: center; margin-top: 24px; font-size: 13px;">
            <a routerLink="/login" style="color: var(--green); font-weight: 700; text-decoration: none;">
              ← Back to Login
            </a>
          </div>
        </div>

        <!-- STEP 2: ENTER & VERIFY OTP -->
        <div class="auth-card card" *ngIf="step === 'otp'">
          <div class="brand-row" style="margin-bottom: 20px;">
            <div class="brand-badge" style="font-size: 22px;">✉️</div>
            <div>
              <div style="font-weight: 800; font-size: 16px;">CROPDEAL</div>
              <div style="font-size: 12px; color: var(--muted);">Email OTP Verification</div>
            </div>
          </div>

          <h2>Enter OTP</h2>
          <p class="sub">
            We sent a 6-digit verification code to <strong>{{ email }}</strong>. Please check your inbox.
          </p>

          <form (ngSubmit)="verifyOtp()">
            <div class="field">
              <label>6-Digit OTP Code *</label>
              <input type="text"
                     name="otp"
                     [(ngModel)]="otp"
                     required
                     maxlength="6"
                     pattern="^[0-9]{6}$"
                     placeholder="123456"
                     style="font-size: 24px; letter-spacing: 6px; text-align: center; font-weight: 700;"
                     [disabled]="loading">
            </div>

            <!-- OTP TIMER & RESEND -->
            <div style="margin-top: 16px; padding: 12px; background: #f8fafc; border-radius: 8px; text-align: center; border: 1px solid #e2e8f0;">
              <div style="font-size: 13px; color: #475569; font-weight: 600;">
                OTP expires in:
                <span [style.color]="timerSeconds <= 30 ? '#dc2626' : '#166534'" style="font-weight: 800; font-family: monospace; font-size: 15px;">
                  {{ formattedTimer }}
                </span>
              </div>
              <div style="margin-top: 8px;">
                <button type="button"
                        (click)="resendOtp()"
                        [disabled]="resendCooldown > 0 || loading"
                        style="background: none; border: none; font-size: 12px; color: var(--green); font-weight: 700; cursor: pointer; text-decoration: underline;">
                  {{ resendCooldown > 0 ? ('Resend OTP in ' + resendCooldown + 's') : 'Resend OTP' }}
                </button>
              </div>
            </div>

            <div *ngIf="errorMessage" class="error-banner" style="margin-top: 14px; padding: 10px 14px; background: #fee2e2; border: 1px solid #f87171; border-radius: 8px; color: #b91c1c; font-size: 13px;">
              {{ errorMessage }}
            </div>

            <button type="submit"
                    class="btn btn-primary full"
                    style="margin-top: 20px;"
                    [disabled]="loading || !otp || otp.length !== 6">
              {{ loading ? 'Verifying…' : 'Verify OTP' }}
            </button>
          </form>

          <div style="text-align: center; margin-top: 20px; font-size: 13px;">
            <button type="button" (click)="step = 'email'" style="background: none; border: none; color: #64748b; font-size: 12px; cursor: pointer;">
              Change Email Address
            </button>
          </div>
        </div>

        <!-- STEP 3: NEW PASSWORD & CONFIRM PASSWORD -->
        <div class="auth-card card" *ngIf="step === 'password'">
          <div class="brand-row" style="margin-bottom: 20px;">
            <div class="brand-badge" style="font-size: 22px;">🔑</div>
            <div>
              <div style="font-weight: 800; font-size: 16px;">CROPDEAL</div>
              <div style="font-size: 12px; color: var(--muted);">Set New Password</div>
            </div>
          </div>

          <h2>New Password</h2>
          <p class="sub">Choose a strong password with at least 6 characters.</p>

          <form (ngSubmit)="resetPassword()">
            <div class="field">
              <label>New Password *</label>
              <div style="position: relative; display: flex; align-items: center;">
                <input [type]="showNewPass ? 'text' : 'password'"
                       name="newPassword"
                       [(ngModel)]="newPassword"
                       required
                       minlength="6"
                       placeholder="Enter new password"
                       style="width: 100%; padding-right: 44px;"
                       [disabled]="loading">
                <button type="button"
                        (click)="showNewPass = !showNewPass"
                        style="position: absolute; right: 12px; background: none; border: none; cursor: pointer; font-size: 16px;">
                  {{ showNewPass ? '🙈' : '👁️' }}
                </button>
              </div>
            </div>

            <div class="field" style="margin-top: 14px;">
              <label>Confirm Password *</label>
              <div style="position: relative; display: flex; align-items: center;">
                <input [type]="showConfirmPass ? 'text' : 'password'"
                       name="confirmPassword"
                       [(ngModel)]="confirmPassword"
                       required
                       minlength="6"
                       placeholder="Re-enter new password"
                       style="width: 100%; padding-right: 44px;"
                       [disabled]="loading">
                <button type="button"
                        (click)="showConfirmPass = !showConfirmPass"
                        style="position: absolute; right: 12px; background: none; border: none; cursor: pointer; font-size: 16px;">
                  {{ showConfirmPass ? '🙈' : '👁️' }}
                </button>
              </div>
            </div>

            <div *ngIf="errorMessage" class="error-banner" style="margin-top: 14px; padding: 10px 14px; background: #fee2e2; border: 1px solid #f87171; border-radius: 8px; color: #b91c1c; font-size: 13px;">
              {{ errorMessage }}
            </div>

            <button type="submit"
                    class="btn btn-primary full"
                    style="margin-top: 22px;"
                    [disabled]="loading || !newPassword || !confirmPassword">
              {{ loading ? 'Updating Password…' : 'Reset Password' }}
            </button>
          </form>
        </div>

        <!-- STEP 4: SUCCESS CONFIRMATION -->
        <div class="auth-card card" *ngIf="step === 'success'" style="text-align: center;">
          <div style="width: 64px; height: 64px; border-radius: 50%; background: #dcfce7; color: #166534; font-size: 32px; display: grid; place-items: center; margin: 0 auto 18px;">
            ✓
          </div>
          <h2>Password Reset Successful</h2>
          <p class="sub" style="margin-bottom: 24px;">
            Your password has been securely updated. You can now log into your CropDeal account.
          </p>
          <a routerLink="/login" class="btn btn-primary full" style="display: block; text-decoration: none;">
            Back to Login
          </a>
        </div>
      </section>
    </div>
  `
})
export class ForgotPasswordComponent implements OnDestroy {
  auth = inject(AuthService);
  toast = inject(ToastService);
  router = inject(Router);

  step: 'email' | 'otp' | 'password' | 'success' = 'email';

  email = '';
  otp = '';
  newPassword = '';
  confirmPassword = '';

  loading = false;
  errorMessage = '';

  showNewPass = false;
  showConfirmPass = false;

  timerSeconds = 300; // 5 minutes
  resendCooldown = 30; // 30 seconds
  private intervalId: any = null;
  private resendIntervalId: any = null;

  get formattedTimer(): string {
    const m = Math.floor(this.timerSeconds / 60);
    const s = this.timerSeconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  sendOtp() {
    this.errorMessage = '';
    const cleanEmail = this.email.trim();
    if (!cleanEmail) {
      this.errorMessage = 'Please enter your email.';
      return;
    }

    this.loading = true;
    this.auth.requestOtp(cleanEmail).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.show(res.message || 'OTP sent to your registered email address.');
        this.step = 'otp';
        this.startTimer();
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Unable to send OTP email. Please try again later.';
        this.errorMessage = msg;
        this.toast.show(msg, 'error');
      }
    });
  }

  verifyOtp() {
    this.errorMessage = '';
    const cleanOtp = this.otp.trim();
    if (!cleanOtp || cleanOtp.length !== 6) {
      this.errorMessage = 'Please enter a valid 6-digit OTP.';
      return;
    }

    this.loading = true;
    this.auth.verifyOtp(this.email.trim(), cleanOtp).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.show(res.message || 'OTP verified successfully.');
        this.step = 'password';
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Invalid OTP.';
        this.errorMessage = msg;
        this.toast.show(msg, 'error');
      }
    });
  }

  resendOtp() {
    if (this.resendCooldown > 0) return;
    this.errorMessage = '';
    this.loading = true;
    this.auth.requestOtp(this.email.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.show(res.message || 'A new OTP has been sent to your email.');
        this.otp = '';
        this.startTimer();
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Unable to send OTP email. Please try again later.';
        this.errorMessage = msg;
        this.toast.show(msg, 'error');
      }
    });
  }

  resetPassword() {
    this.errorMessage = '';
    if (!this.newPassword || this.newPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters.';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.loading = true;
    this.auth.resetPasswordWithOtp(this.email.trim(), this.otp.trim(), this.newPassword, this.confirmPassword).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.show('Password reset successful');
        this.step = 'success';
        this.clearTimers();
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Password reset failed.';
        this.errorMessage = msg;
        this.toast.show(msg, 'error');
      }
    });
  }

  private startTimer() {
    this.clearTimers();
    this.timerSeconds = 300;
    this.resendCooldown = 30;

    this.intervalId = setInterval(() => {
      if (this.timerSeconds > 0) {
        this.timerSeconds--;
      } else {
        clearInterval(this.intervalId);
      }
    }, 1000);

    this.resendIntervalId = setInterval(() => {
      if (this.resendCooldown > 0) {
        this.resendCooldown--;
      } else {
        clearInterval(this.resendIntervalId);
      }
    }, 1000);
  }

  private clearTimers() {
    if (this.intervalId) clearInterval(this.intervalId);
    if (this.resendIntervalId) clearInterval(this.resendIntervalId);
  }

  ngOnDestroy() {
    this.clearTimers();
  }
}
