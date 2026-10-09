import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink, ActivatedRoute } from "@angular/router";
import { NgIf, CommonModule } from "@angular/common";
import { AuthService } from "../../core/auth.service";
import { ToastService } from "../../core/toast.service";
import { AuthVisualComponent } from "./auth-visual.component";

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf, CommonModule, AuthVisualComponent],
  template: `
    <div class="auth-outer-wrapper">
      <div class="auth-modal-card">
        <!-- Left: Login Form Panel -->
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

          <h2 class="form-title">Welcome Back</h2>
          <p class="form-subtitle">
            Sign in to your account to manage auctions, crops and orders.
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
            <span class="divider-text">OR SIGN IN WITH EMAIL</span>
            <span class="divider-line"></span>
          </div>

          <form (ngSubmit)="login()" autocomplete="off">
            <!-- Email Address -->
            <div class="form-group">
              <label class="form-label">Email Address *</label>
              <input
                type="email"
                name="email"
                [(ngModel)]="email"
                required
                placeholder="name@example.com"
                autocomplete="username"
                class="form-input"
              />
            </div>

            <!-- Password -->
            <div class="form-group">
              <div class="label-with-action">
                <label class="form-label">Password *</label>
                <a routerLink="/forgot-password" class="forgot-link"
                  >Forgot password?</a
                >
              </div>
              <div class="password-wrapper">
                <input
                  [type]="showPassword ? 'text' : 'password'"
                  name="password"
                  [(ngModel)]="password"
                  required
                  placeholder="Enter password"
                  autocomplete="current-password"
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

            <!-- Error Banner -->
            <div *ngIf="error" class="error-banner">⚠️ {{ error }}</div>

            <!-- Unverified Account OTP Box -->
            <div *ngIf="showUnverifiedBox" class="unverified-box">
              <div class="unverified-title">📧 Email Verification Required</div>
              <p class="unverified-desc">
                Your account is not yet verified. Request an OTP to verify your
                email and activate your account.
              </p>

              <div *ngIf="!unverifiedOtpSent" style="margin-top: 10px;">
                <button
                  type="button"
                  class="btn-action-small"
                  (click)="sendUnverifiedOtp()"
                  [disabled]="otpSending"
                >
                  {{ otpSending ? "Sending OTP…" : "Send Verification OTP" }}
                </button>
              </div>

              <div *ngIf="unverifiedOtpSent" style="margin-top: 10px;">
                <div class="input-with-button">
                  <input
                    type="text"
                    name="unverifiedOtp"
                    [(ngModel)]="unverifiedOtp"
                    maxlength="6"
                    placeholder="123456"
                    class="form-input"
                    style="letter-spacing: 3px; font-weight: 700;"
                  />
                  <button
                    type="button"
                    class="btn-action-small"
                    (click)="verifyUnverifiedOtp()"
                    [disabled]="otpVerifying || !unverifiedOtp"
                  >
                    {{ otpVerifying ? "Verifying…" : "Verify & Login" }}
                  </button>
                </div>
                <div class="otp-hint-text">ℹ️ {{ otpHintMessage }}</div>
              </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="submit-btn" [disabled]="loading">
              {{ loading ? "Signing in…" : "Sign in to CropDeal" }}
            </button>
          </form>

          <div class="footer-switch">
            New to CropDeal?
            <a routerLink="/register" class="switch-link">Create an account</a>
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

      .auth-modal-card {
        width: 100%;
        max-width: 1140px;
        min-height: 680px;
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

      .auth-form-section {
        padding: 44px 44px;
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
        margin-bottom: 16px;
      }

      .brand-badge {
        width: 42px;
        height: 42px;
        border-radius: 12px;
        background: #eef7e6;
        border: 1px solid #d4ebc5;
        display: grid;
        place-items: center;
      }

      .brand-title-text {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-weight: 900;
        font-size: 21px;
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
        margin-top: 2px;
      }

      .form-title {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: 28px;
        font-weight: 800;
        color: #121c17;
        margin: 4px 0 6px;
        letter-spacing: -0.01em;
      }

      .form-subtitle {
        font-size: 13.5px;
        color: #6d7973;
        margin: 0 0 22px;
      }

      .social-btn-stack {
        display: flex;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 20px;
      }

      .social-quick-btn {
        width: 100%;
        background: #f4f6f4;
        border: 1px solid #dbe2db;
        border-radius: 11px;
        padding: 10px 14px;
        font-size: 13.5px;
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
        font-size: 17px;
      }

      .auth-divider {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;
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

      .form-group {
        display: flex;
        flex-direction: column;
        gap: 7px;
        margin-bottom: 16px;
      }

      .form-label {
        font-size: 12.5px;
        font-weight: 800;
        color: #43514a;
      }

      .label-with-action {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .forgot-link {
        font-size: 12px;
        font-weight: 700;
        color: #176b45;
        text-decoration: none;
      }

      .forgot-link:hover {
        text-decoration: underline;
      }

      .form-input {
        width: 100%;
        box-sizing: border-box;
        border: 1px solid #dbe2db;
        background: #fbfdfa;
        border-radius: 11px;
        padding: 11px 14px;
        font-size: 14px;
        color: #1a2922;
        outline: none;
        transition:
          border 0.15s ease,
          box-shadow 0.15s ease;
      }

      .form-input:focus {
        border-color: #2e8b57;
        background: #ffffff;
        box-shadow: 0 0 0 3px rgba(46, 139, 87, 0.15);
      }

      .password-wrapper {
        position: relative;
        display: flex;
        align-items: center;
      }

      .password-wrapper .form-input {
        padding-right: 44px;
      }

      .eye-btn {
        position: absolute;
        right: 8px;
        background: none;
        border: none;
        font-size: 17px;
        cursor: pointer;
        padding: 6px;
        color: #7a8881;
        display: grid;
        place-items: center;
      }

      .error-banner {
        background: #fde8e8;
        border: 1px solid #f8b4b4;
        color: #9b1c1c;
        padding: 11px 14px;
        border-radius: 11px;
        font-size: 13px;
        margin-bottom: 16px;
        font-weight: 600;
      }

      .unverified-box {
        background: #fff8eb;
        border: 1px solid #f6d289;
        border-radius: 12px;
        padding: 14px;
        margin-bottom: 16px;
      }

      .unverified-title {
        font-size: 13px;
        font-weight: 800;
        color: #945700;
        margin-bottom: 4px;
      }

      .unverified-desc {
        font-size: 12px;
        color: #704403;
        margin: 0;
        line-height: 1.45;
      }

      .btn-action-small {
        background: #176b45;
        color: #fff;
        border: none;
        padding: 8px 14px;
        border-radius: 8px;
        font-weight: 700;
        font-size: 12px;
        cursor: pointer;
      }

      .input-with-button {
        display: flex;
        gap: 8px;
        align-items: center;
      }

      .otp-hint-text {
        font-size: 11.5px;
        color: #176b45;
        margin-top: 4px;
        font-weight: 600;
      }

      .submit-btn {
        width: 100%;
        background: #176b45;
        color: #ffffff;
        border: none;
        border-radius: 12px;
        padding: 14px;
        font-size: 15px;
        font-weight: 800;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 6px 18px rgba(23, 107, 69, 0.25);
        margin-top: 8px;
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

      .footer-switch {
        text-align: center;
        margin-top: 20px;
        font-size: 13.5px;
        color: #6b7771;
      }

      .switch-link {
        color: #176b45;
        font-weight: 800;
        text-decoration: underline;
        cursor: pointer;
      }

      .auth-visual-section {
        height: 100%;
        background: #104e30;
      }

      @media (max-width: 960px) {
        .auth-modal-card {
          grid-template-columns: 1fr;
          max-width: 520px;
        }
        .auth-visual-section {
          display: none;
        }
        .auth-form-section {
          padding: 34px 26px;
          max-height: none;
        }
      }
    `,
  ],
})
export class LoginComponent implements OnInit {
  email = "";
  password = "";
  showPassword = false;
  loading = false;
  error = "";

  // Unverified account OTP verification
  showUnverifiedBox = false;
  unverifiedOtpSent = false;
  unverifiedOtp = "";
  otpSending = false;
  otpVerifying = false;
  otpHintMessage = "";

  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      const token = params["resetToken"] || params["token"];
      if (token) {
        this.router.navigate(["/forgot-password"], { queryParams: { token } });
      }
    });
  }

  login() {
    this.loading = true;
    this.error = "";
    this.showUnverifiedBox = false;

    this.auth.login(this.email.trim(), this.password).subscribe({
      next: (r) => {
        if (!r.token) {
          this.error = r.message || "Login did not return a token";
          this.loading = false;
          return;
        }
        this.auth.save(r);
        this.loading = false;
        this.toast.show("Signed in successfully as " + r.role);
        const targetUrl = r.role === "ADMIN" ? "/admin" : "/dashboard";
        this.router.navigateByUrl(targetUrl);
      },
      error: (e) => {
        const msg =
          e.error?.message || "Login failed. Check your email and password.";
        this.error = msg;
        this.loading = false;

        // If backend returned email unverified message
        if (
          msg.toLowerCase().includes("verify your email") ||
          msg.toLowerCase().includes("otp")
        ) {
          this.showUnverifiedBox = true;
        }
      },
    });
  }

  sendUnverifiedOtp() {
    const trimmed = this.email.trim();
    if (!trimmed) {
      this.error = "Please enter your email above.";
      return;
    }
    this.otpSending = true;
    this.auth.requestRegistrationOtp(trimmed).subscribe({
      next: (r) => {
        this.otpSending = false;
        this.unverifiedOtpSent = true;
        this.otpHintMessage = r.message || "OTP sent (Demo: 123456)";
        this.toast.show("OTP sent to " + trimmed);
      },
      error: (e) => {
        this.otpSending = false;
        this.error = e.error?.message || "Failed to send verification OTP.";
      },
    });
  }

  verifyUnverifiedOtp() {
    const trimmed = this.email.trim();
    const otpVal = this.unverifiedOtp.trim();
    if (!otpVal || otpVal.length < 6) {
      this.error = "Please enter the 6-digit OTP.";
      return;
    }
    this.otpVerifying = true;
    this.auth.verifyRegistrationOtp(trimmed, otpVal).subscribe({
      next: () => {
        this.otpVerifying = false;
        this.showUnverifiedBox = false;
        this.toast.show("Email verified successfully! Logging you in...");
        // Retry login
        this.login();
      },
      error: (e) => {
        this.otpVerifying = false;
        this.error = e.error?.message || "Invalid or expired OTP.";
      },
    });
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
        const targetUrl = r.role === "ADMIN" ? "/admin" : "/dashboard";
        this.router.navigateByUrl(targetUrl);
      },
      error: (e) => {
        this.loading = false;
        this.error = e.error?.message || "Demo login failed";
      },
    });
  }
}
