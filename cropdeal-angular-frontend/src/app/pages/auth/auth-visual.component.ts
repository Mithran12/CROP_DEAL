import { Component } from "@angular/core";

@Component({
  selector: "app-auth-visual",
  standalone: true,
  template: `
    <div class="visual-panel">
      <!-- Top Brand Header -->
      <div class="visual-header">
        <div class="logo-mark">🌾</div>
        <div class="brand-text">
          <span 
          class="brand-title">CROP<span class="brand-highlight">DEAL</span></span>
          <span class="brand-subtitle">DIRECT AGRICULTURAL MARKETPLACE</span>
        </div>
      </div>

      <!-- Headline Block -->
      <div class="headline-block">
        <h1 class="main-headline">Direct Trade.<br />Fair Prices.</h1>
        <p class="main-description">
          Connecting verified farmers directly with wholesale dealers with live
          lot auctions and escrow payment protection.
        </p>
      </div>

      <!-- Replaceable Hero Dummy Image Container (Easy to replace later) -->
      <div class="dummy-image-container">
        <img
          [src]="imageSrc"
          alt="CropDeal Agricultural Marketplace"
          class="dummy-hero-image"
          (error)="onImageError($event)"
        />
        <div class="image-overlay-badge">
          <span>🛡️ Verified Farmers & Wholesale Dealers</span>
        </div>
      </div>

      <!-- Feature Badges -->
      <div class="feature-pills">
        <div class="pill"><span>🔒</span> 100% Escrow Protection</div>
        <div class="pill"><span>⚖️</span> Direct Price Negotiation</div>
        <div class="pill"><span>📦</span> QR Delivery Verification</div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        width: 100%;
        height: 100%;
      }

      .visual-panel {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 100%;
        padding: 36px 32px;
        box-sizing: border-box;
        background: linear-gradient(
          145deg,
          #124d2e 0%,
          #176b45 50%,
          #0d3b22 100%
        );
        color: #ffffff;
        border-radius: 20px;
        position: relative;
        overflow: hidden;
      }

      .visual-header {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .logo-mark {
        font-size: 28px;
        line-height: 1;
      }

      .brand-text {
        display: flex;
        flex-direction: column;
      }

      .brand-title {
        font-size: 20px;
        font-weight: 900;
        letter-spacing: 1.5px;
        color: #ffffff;
      }

      .brand-highlight {
        color: #cfe96b;
      }

      .brand-subtitle {
        font-size: 9px;
        font-weight: 700;
        letter-spacing: 1.8px;
        color: rgba(255, 255, 255, 0.7);
      }

      .headline-block {
        margin-top: 18px;
      }

      .main-headline {
        font-size: 28px;
        font-weight: 800;
        line-height: 1.2;
        color: #ffffff;
        margin: 0 0 10px 0;
      }

      .main-description {
        font-size: 13px;
        line-height: 1.5;
        color: rgba(255, 255, 255, 0.85);
        margin: 0;
        max-width: 420px;
      }

      /* Dummy Image Container - Easy to replace later */
      .dummy-image-container {
        position: relative;
        width: 100%;
        height: 100%;
        margin: 20px 0;
        border-radius: 14px;
        overflow: hidden;
        box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
        border: 2px solid rgba(207, 233, 107, 0.35);
        background: #0f3d24;
      }

      .dummy-hero-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
        transition: transform 0.3s ease;
      }

      .dummy-hero-image:hover {
        transform: scale(1.02);
      }

      .image-overlay-badge {
        position: absolute;
        bottom: 12px;
        left: 12px;
        right: 12px;
        background: rgba(10, 40, 24, 0.82);
        backdrop-filter: blur(6px);
        padding: 8px 12px;
        border-radius: 8px;
        font-size: 11px;
        font-weight: 700;
        color: #cfe96b;
        border: 1px solid rgba(207, 233, 107, 0.3);
        text-align: center;
      }

      .feature-pills {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: auto;
      }

      .pill {
        background: rgba(255, 255, 255, 0.12);
        border: 1px solid rgba(255, 255, 255, 0.18);
        border-radius: 20px;
        padding: 6px 12px;
        font-size: 11px;
        font-weight: 600;
        color: #f1f8ed;
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }

      @media (max-width: 900px) {
        .dummy-image-container {
          height: 180px;
        }
        .main-headline {
          font-size: 22px;
        }
      }
    `,
  ],
})
export class AuthVisualComponent {
  // Replace this path with any custom image file as needed
  imageSrc = "/assets/images/auth-hero.jpg";

  onImageError(event: any) {
    // Fallback if image path is not found
    event.target.src = "/assets/crops/wheat.webp";
  }
}
