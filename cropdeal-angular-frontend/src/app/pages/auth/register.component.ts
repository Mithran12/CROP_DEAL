import { Component, inject, OnDestroy } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { NgIf, NgFor, CommonModule } from "@angular/common";
import { AuthService } from "../../core/auth.service";
import { ApiService } from "../../core/api.service";
import { ToastService } from "../../core/toast.service";
import { Role } from "../../core/models";
import { AuthVisualComponent } from "./auth-visual.component";

@Component({
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    NgIf,
    NgFor,
    CommonModule,
    AuthVisualComponent,
  ],
  template: `
    <div class="auth-outer-wrapper">
      <div class="auth-modal-card">
        <!-- Left: Registration Form Section -->
        <section class="auth-form-section">
          <!-- Brand Logo Header -->
          <div class="brand-top">
            <div class="brand-badge">
              <svg viewBox="0 0 32 32" width="22" height="22" fill="none">
                <path
                  d="M16 3C16 3 11 9 11 15C11 18.3 13.2 21.1 16 22C18.8 21.1 21 18.3 21 15C21 9 16 3 16 3Z"
                  fill="#8bc34a"
                />
                <path
                  d="M16 7C16 7 13 11 13 15C13 16.8 14.3 18.3 16 19C17.7 18.3 19 16.8 19 15C19 11 16 7 16 7Z"
                  fill="#1b5e20"
                />
                <path
                  d="M16 19V29M16 23C13 22 10 24 9 26M16 26C19 25 22 27 23 29"
                  stroke="#8bc34a"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </div>
            <div>
              <div class="brand-title-text">CROP<span>DEAL</span></div>
              <div class="brand-sub-text">DIRECT AGRICULTURAL MARKETPLACE</div>
            </div>
          </div>

          <h2 class="form-title">Create Account</h2>
          <p class="form-subtitle">
            Join as a verified Farmer or wholesale Dealer.
          </p>

          <!-- Social Quick Logins -->
          <div class="social-btn-stack">
            <button
              type="button"
              class="social-quick-btn"
              (click)="facebook('FB-FARMER-01')"
            >
              <span class="icon">🌿</span>
              <span>Continue with FB – Farmer</span>
            </button>
            <button
              type="button"
              class="social-quick-btn"
              (click)="facebook('FB-DEALER-01')"
            >
              <span class="icon">💼</span>
              <span>Continue with FB – Dealer</span>
            </button>
          </div>

          <div class="auth-divider">
            <span class="divider-line"></span>
            <span class="divider-text">OR REGISTER WITH EMAIL</span>
            <span class="divider-line"></span>
          </div>

          <form (ngSubmit)="submitRegistration()" autocomplete="off">
            <!-- Select Role -->
            <div class="form-group">
              <label class="form-label">Select Role *</label>
              <div class="role-selector-row">
                <button
                  type="button"
                  class="role-pill-btn"
                  [class.active]="role === 'FARMER'"
                  (click)="role = 'FARMER'"
                >
                  <span class="role-icon">👨‍🌾</span> Farmer
                </button>
                <button
                  type="button"
                  class="role-pill-btn"
                  [class.active]="role === 'DEALER'"
                  (click)="role = 'DEALER'"
                >
                  <span class="role-icon">💼</span> Dealer / Buyer
                </button>
              </div>
            </div>

            <!-- Full Name & Mobile Number (2-Column Grid) -->
            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">Full Name *</label>
                <input
                  type="text"
                  name="name"
                  [(ngModel)]="name"
                  required
                  placeholder="Ramesh Patel"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label class="form-label">Mobile Number (10 digits) *</label>
                <input
                  type="tel"
                  name="phone"
                  [(ngModel)]="phone"
                  required
                  maxlength="10"
                  placeholder="9876543210"
                  class="form-input"
                />
              </div>
            </div>

            <!-- State & Pincode (2-Column Grid, Clean Alignment Inside Card) -->
            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">State *</label>
                <select
                  name="state"
                  [(ngModel)]="state"
                  required
                  class="form-select"
                >
                  <option value="" disabled>Select State</option>
                  <option *ngFor="let s of indianStates" [value]="s">
                    {{ s }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Pincode *</label>
                <input
                  type="text"
                  name="pincode"
                  [(ngModel)]="pincode"
                  required
                  maxlength="6"
                  placeholder="638001"
                  class="form-input"
                />
              </div>
            </div>

            <!-- Email Address with OTP Verification -->
            <div class="form-group">
              <div class="label-with-action">
                <label class="form-label">Email Address *</label>
                <span *ngIf="otpVerified" class="verified-pill"
                  >✓ Verified</span
                >
              </div>
              <div class="input-with-button">
                <input
                  type="email"
                  name="email"
                  [(ngModel)]="email"
                  required
                  placeholder="name@example.com"
                  [disabled]="otpVerified"
                  class="form-input"
                />
                <button
                  *ngIf="!otpVerified"
                  type="button"
                  class="btn-send-otp"
                  [disabled]="otpSending || !email"
                  (click)="sendOtp()"
                >
                  {{
                    otpSending
                      ? "Sending…"
                      : otpSent
                        ? "Resend OTP"
                        : "Send OTP"
                  }}
                </button>
              </div>
              <div *ngIf="otpSentMessage" class="otp-hint-text">
                ℹ️ {{ otpSentMessage }}
              </div>
            </div>

            <!-- OTP Input Box (Displayed when OTP is sent & not yet verified) -->
            <div *ngIf="otpSent && !otpVerified" class="otp-box-section">
              <label class="form-label">Enter 6-Digit Verification OTP *</label>
              <div class="input-with-button">
                <input
                  type="text"
                  name="otp"
                  [(ngModel)]="otp"
                  maxlength="6"
                  placeholder="123456"
                  class="form-input"
                  style="letter-spacing: 4px; font-weight: 700; font-size: 16px;"
                />
                <button
                  type="button"
                  class="btn-verify-otp"
                  [disabled]="otpVerifying || !otp || otp.length < 6"
                  (click)="verifyOtp()"
                >
                  {{ otpVerifying ? "Checking…" : "Verify OTP" }}
                </button>
              </div>
              <div class="resend-timer-row" *ngIf="resendCooldown > 0">
                Resend OTP in <strong>{{ resendCooldown }}s</strong>
              </div>
            </div>

            <!-- Password & Confirm Password (2-Column Grid with Eye Toggles) -->
            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">Password (min 6 chars) *</label>
                <div class="password-wrapper">
                  <input
                    [type]="showPassword ? 'text' : 'password'"
                    name="password"
                    [(ngModel)]="password"
                    required
                    minlength="6"
                    placeholder="Create password"
                    class="form-input"
                  />
                  <button
                    type="button"
                    class="eye-btn"
                    (click)="showPassword = !showPassword"
                    [title]="showPassword ? 'Hide password' : 'Show password'"
                  >
                    {{ showPassword ? "🙈" : "👁️" }}
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">Confirm Password *</label>
                <div class="password-wrapper">
                  <input
                    [type]="showConfirmPassword ? 'text' : 'password'"
                    name="confirmPassword"
                    [(ngModel)]="confirmPassword"
                    required
                    minlength="6"
                    placeholder="Confirm password"
                    class="form-input"
                  />
                  <button
                    type="button"
                    class="eye-btn"
                    (click)="showConfirmPassword = !showConfirmPassword"
                    [title]="
                      showConfirmPassword ? 'Hide password' : 'Show password'
                    "
                  >
                    {{ showConfirmPassword ? "🙈" : "👁️" }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Error Banner -->
            <div *ngIf="error" class="error-banner">⚠️ {{ error }}</div>

            <!-- Submit Button -->
            <button type="submit" class="submit-btn" [disabled]="loading">
              {{ loading ? "Creating Account…" : "Create Account" }}
            </button>
          </form>

          <div class="footer-switch">
            Already registered?
            <a routerLink="/login" class="switch-link">Sign in</a>
          </div>
        </section>

        <!-- Right: Visual Illustration Section -->
        <section class="auth-visual-section">
          <app-auth-visual></app-auth-visual>
        </section>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
        background: #0d3822;
      }

      /* Ambient organic green & gold canvas background matching reference photo */
      .auth-outer-wrapper {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 30px 20px;
        box-sizing: border-box;
        background:
          radial-gradient(
            ellipse 65% 55% at 0% 50%,
            #f5a623 0%,
            rgba(245, 166, 35, 0.4) 35%,
            transparent 70%
          ),
          radial-gradient(
            ellipse 65% 55% at 100% 50%,
            #f5a623 0%,
            rgba(245, 166, 35, 0.4) 35%,
            transparent 70%
          ),
          linear-gradient(135deg, #0a331e 0%, #104e2e 50%, #0a331e 100%);
        background-attachment: fixed;
      }

      /* Unified split card container */
      .auth-modal-card {
        width: 100%;
        max-width: 1140px;
        min-height: 720px;
        background: #ffffff;
        border-radius: 28px;
        box-shadow:
          0 25px 65px rgba(0, 0, 0, 0.35),
          0 0 0 1px rgba(255, 255, 255, 0.1);
        display: grid;
        grid-template-columns: 1fr 1.05fr;
        overflow: hidden;
        margin: auto;
      }

      /* Left Form Panel */
      .auth-form-section {
        padding: 38px 42px;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: center;
        background: #ffffff;
        overflow-y: auto;
        max-height: 90vh;
      }

      .brand-top {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
      }

      .brand-badge {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        background: #eef7e6;
        border: 1px solid #d4ebc5;
        display: grid;
        place-items: center;
      }

      .brand-title-text {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-weight: 900;
        font-size: 20px;
        color: #176b45;
        letter-spacing: -0.01em;
        line-height: 1.1;
      }

      .brand-title-text span {
        color: #7ab82d;
      }

      .brand-sub-text {
        font-size: 8.5px;
        font-weight: 800;
        letter-spacing: 0.12em;
        color: #7e8c85;
        margin-top: 1px;
      }

      .form-title {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: 26px;
        font-weight: 800;
        color: #121c17;
        margin: 4px 0 4px;
        letter-spacing: -0.01em;
      }

      .form-subtitle {
        font-size: 13px;
        color: #6d7973;
        margin: 0 0 16px;
      }

      /* Social quick buttons */
      .social-btn-stack {
        display: flex;
        flex-direction: column;
        gap: 9px;
        margin-bottom: 16px;
      }

      .social-quick-btn {
        width: 100%;
        background: #f4f6f4;
        border: 1px solid #dbe2db;
        border-radius: 11px;
        padding: 9px 14px;
        font-size: 13px;
        font-weight: 700;
        color: #1c2e25;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        transition: all 0.15s ease;
        cursor: pointer;
      }

      .social-quick-btn:hover {
        background: #ebf0ea;
        border-color: #c4d1c4;
        transform: translateY(-1px);
      }

      .social-quick-btn .icon {
        font-size: 16px;
      }

      /* Divider */
      .auth-divider {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
      }

      .divider-line {
        flex: 1;
        height: 1px;
        background: #e3e8e2;
      }

      .divider-text {
        font-size: 10.5px;
        font-weight: 800;
        color: #8b9991;
        letter-spacing: 0.08em;
      }

      /* Form Fields */
      .form-group {
        display: flex;
        flex-direction: column;
        gap: 6px;
        margin-bottom: 13px;
      }

      .form-label {
        font-size: 12px;
        font-weight: 800;
        color: #43514a;
      }

      .form-grid-2 {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
      }

      .role-selector-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
      }

      .role-pill-btn {
        border: 1px solid #dce2db;
        background: #f6f8f5;
        color: #27382f;
        border-radius: 11px;
        padding: 10px 12px;
        font-size: 13px;
        font-weight: 700;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        transition: all 0.15s ease;
        cursor: pointer;
      }

      .role-pill-btn.active {
        background: #176b45;
        color: #ffffff;
        border-color: #176b45;
        box-shadow: 0 4px 12px rgba(23, 107, 69, 0.25);
      }

      .role-icon {
        font-size: 16px;
      }

      .form-input,
      .form-select {
        width: 100%;
        box-sizing: border-box;
        border: 1px solid #dbe2db;
        background: #fbfdfa;
        border-radius: 10px;
        padding: 10px 13px;
        font-size: 13.5px;
        color: #1a2922;
        outline: none;
        transition:
          border 0.15s ease,
          box-shadow 0.15s ease;
      }

      .form-input:focus,
      .form-select:focus {
        border-color: #2e8b57;
        background: #ffffff;
        box-shadow: 0 0 0 3px rgba(46, 139, 87, 0.15);
      }

      /* Password Input Wrapper */
      .password-wrapper {
        position: relative;
        display: flex;
        align-items: center;
      }

      .password-wrapper .form-input {
        padding-right: 42px;
      }

      .eye-btn {
        position: absolute;
        right: 8px;
        background: none;
        border: none;
        font-size: 16px;
        cursor: pointer;
        padding: 6px;
        color: #7a8881;
        display: grid;
        place-items: center;
      }

      /* OTP Elements */
      .label-with-action {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .verified-pill {
        font-size: 11px;
        font-weight: 800;
        color: #137333;
        background: #e6f4ea;
        padding: 2px 8px;
        border-radius: 10px;
        border: 1px solid #ceead6;
      }

      .input-with-button {
        display: flex;
        gap: 8px;
        align-items: center;
      }

      .btn-send-otp,
      .btn-verify-otp {
        white-space: nowrap;
        border: 1px solid #176b45;
        background: #f0f7f3;
        color: #176b45;
        font-size: 12.5px;
        font-weight: 800;
        padding: 9px 14px;
        border-radius: 9px;
        cursor: pointer;
        transition: all 0.15s ease;
      }

      .btn-send-otp:hover,
      .btn-verify-otp:hover {
        background: #176b45;
        color: #ffffff;
      }

      .btn-send-otp:disabled,
      .btn-verify-otp:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      .otp-box-section {
        background: #f7faf6;
        border: 1px dashed #b9d8c4;
        border-radius: 12px;
        padding: 12px 14px;
        margin-bottom: 13px;
      }

      .otp-hint-text {
        font-size: 11.5px;
        color: #216f46;
        margin-top: 4px;
        font-weight: 600;
      }

      .resend-timer-row {
        font-size: 11px;
        color: #66756e;
        margin-top: 5px;
        text-align: right;
      }

      /* Error Banner */
      .error-banner {
        background: #fde8e8;
        border: 1px solid #f8b4b4;
        color: #9b1c1c;
        padding: 10px 14px;
        border-radius: 10px;
        font-size: 12.5px;
        margin-bottom: 14px;
        font-weight: 600;
      }

      /* Submit Button */
      .submit-btn {
        width: 100%;
        background: #176b45;
        color: #ffffff;
        border: none;
        border-radius: 12px;
        padding: 13px;
        font-size: 15px;
        font-weight: 800;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 6px 18px rgba(23, 107, 69, 0.25);
        margin-top: 6px;
      }

      .submit-btn:hover:not(:disabled) {
        background: #125537;
        transform: translateY(-1px);
        box-shadow: 0 8px 22px rgba(23, 107, 69, 0.32);
      }

      .submit-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      /* Switch link */
      .footer-switch {
        text-align: center;
        margin-top: 16px;
        font-size: 13px;
        color: #6b7771;
      }

      .switch-link {
        color: #176b45;
        font-weight: 800;
        text-decoration: underline;
        cursor: pointer;
      }

      /* Right Visual Panel */
      .auth-visual-section {
        height: 100%;
        background: #104e30;
      }

      /* Responsive */
      @media (max-width: 960px) {
        .auth-modal-card {
          grid-template-columns: 1fr;
          max-width: 540px;
        }
        .auth-visual-section {
          display: none;
        }
        .auth-form-section {
          padding: 30px 24px;
          max-height: none;
        }
      }
    `,
  ],
})
export class RegisterComponent implements OnDestroy {
  role: Role = "FARMER";
  name = "";
  phone = "";
  state = "";
  pincode = "";
  email = "";
  password = "";
  confirmPassword = "";
  showPassword = false;
  showConfirmPassword = false;

  // OTP Verification state
  otp = "";
  otpSent = false;
  otpSending = false;
  otpVerifying = false;
  otpVerified = false;
  otpSentMessage = "";
  resendCooldown = 0;
  private cooldownTimer: any = null;

  error = "";
  loading = false;

  private auth = inject(AuthService);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private router = inject(Router);

  indianStates: string[] = [
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Delhi",
    "Chandigarh",
    "Puducherry",
  ];

  ngOnDestroy() {
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
    }
  }

  sendOtp() {
    const trimmedEmail = this.email.trim();
    if (!trimmedEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      this.error = "Please enter a valid email address first to receive OTP.";
      return;
    }

    this.error = "";
    this.otpSending = true;
    this.auth.requestRegistrationOtp(trimmedEmail).subscribe({
      next: (res) => {
        this.otpSending = false;
        this.otpSent = true;
        this.otpSentMessage =
          res.message || "OTP sent to your email (Demo OTP: 123456)";
        this.toast.show("OTP sent to " + trimmedEmail);
        this.startCooldown();
      },
      error: (err) => {
        this.otpSending = false;
        this.error =
          err.error?.message ||
          "Failed to send OTP. Please check your email address.";
      },
    });
  }

  verifyOtp() {
    const trimmedEmail = this.email.trim();
    const trimmedOtp = this.otp.trim();
    if (!trimmedOtp || trimmedOtp.length < 6) {
      this.error = "Please enter the 6-digit OTP received on your email.";
      return;
    }

    this.error = "";
    this.otpVerifying = true;
    this.auth.verifyRegistrationOtp(trimmedEmail, trimmedOtp).subscribe({
      next: (res) => {
        this.otpVerifying = false;
        this.otpVerified = true;
        this.otpSentMessage = "Email verified successfully!";
        this.toast.show("Email verified successfully!");
      },
      error: (err) => {
        this.otpVerifying = false;
        this.error =
          err.error?.message ||
          "Invalid or expired OTP. Please enter the correct code.";
      },
    });
  }

  private startCooldown() {
    this.resendCooldown = 60;
    if (this.cooldownTimer) clearInterval(this.cooldownTimer);
    this.cooldownTimer = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) {
        clearInterval(this.cooldownTimer);
      }
    }, 1000);
  }

  facebook(accountId: string) {
    this.loading = true;
    this.error = "";
    this.auth.mockFacebook(accountId).subscribe({
      next: (r) => {
        this.loading = false;
        this.auth.save(r);
        this.toast.show(
          "Signed in via " +
            (accountId === "FB-FARMER-01" ? "Farmer Demo" : "Dealer Demo"),
        );
        this.router.navigateByUrl("/dashboard");
      },
      error: (e) => {
        this.loading = false;
        this.error = e.error?.message || "Demo login failed";
      },
    });
  }

  submitRegistration() {
    this.error = "";

    const trimmedName = this.name.trim();
    if (!trimmedName || trimmedName.length < 3) {
      this.error = "Full Name must be at least 3 characters long.";
      return;
    }

    const trimmedPhone = this.phone.trim();
    if (!/^[6-9][0-9]{9}$/.test(trimmedPhone)) {
      this.error =
        "Please enter a valid 10-digit Indian mobile number (starting with 6, 7, 8, or 9).";
      return;
    }

    if (!this.state) {
      this.error = "Please select your State.";
      return;
    }

    const trimmedPincode = this.pincode.trim();
    if (!/^[1-9][0-9]{5}$/.test(trimmedPincode)) {
      this.error = "Please enter a valid 6-digit Pincode.";
      return;
    }

    const trimmedEmail = this.email.trim();
    if (!trimmedEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      this.error = "Please enter a valid email address.";
      return;
    }

    // Check OTP verification
    if (!this.otpVerified) {
      if (!this.otpSent) {
        this.error = "Please request and verify email OTP before registering.";
        this.sendOtp();
        return;
      }
      if (!this.otp || this.otp.trim().length < 6) {
        this.error =
          "Please enter the 6-digit OTP sent to your email to verify your account.";
        return;
      }
    }

    if (!this.password || this.password.length < 6) {
      this.error = "Password must be at least 6 characters long.";
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.error = "Passwords do not match.";
      return;
    }

    this.loading = true;
    this.auth
      .register(trimmedEmail, this.password, this.role, this.otp.trim())
      .subscribe({
        next: (r) => {
          if (!r.token) {
            this.loading = false;
            this.error =
              r.message || "Registration succeeded but no token was returned.";
            return;
          }
          this.auth.save(r);

          // Create user profile in user-service
          const profilePayload = {
            authUserId: r.userId,
            name: trimmedName,
            email: trimmedEmail,
            phone: trimmedPhone,
            address: `${this.state} - ${trimmedPincode}`,
            city: this.state,
            state: this.state,
            pincode: trimmedPincode,
            role: this.role,
          };

          this.api.post<any>("/users", profilePayload).subscribe({
            next: (user) => {
              this.loading = false;
              if (user && user.id) {
                this.auth.setUserId(user.id);
                this.auth.currentUser.set(user);
              }
              this.toast.show(
                "Welcome to CropDeal! Please complete your profile and settlement details.",
              );
              this.router.navigateByUrl("/profile");
            },
            error: () => {
              this.loading = false;
              this.toast.show(
                "Welcome to CropDeal! Please complete your profile.",
              );
              this.router.navigateByUrl("/profile");
            },
          });
        },
        error: (e) => {
          this.loading = false;
          this.error =
            e.error?.message ||
            "Registration failed. Please check your details.";
        },
      });
  }
}
