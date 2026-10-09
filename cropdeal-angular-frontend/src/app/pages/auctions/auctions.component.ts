import { Component, inject, OnInit, OnDestroy } from "@angular/core";
import {
  CommonModule,
  NgFor,
  NgIf,
  CurrencyPipe,
  DatePipe,
} from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { Subscription } from "rxjs";
import { ApiService } from "../../core/api.service";
import { AuthService } from "../../core/auth.service";
import { ToastService } from "../../core/toast.service";
import {
  AuctionSocketService,
  AuctionLiveBid,
} from "../../core/auction-socket.service";
import { Auction, Bid, Crop, User } from "../../core/models";

@Component({
  selector: "app-auctions",
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
          <div class="eyebrow">COMPETITIVE BIDDING & REAL-TIME DISCOVERY</div>
          <h1 class="title">Live Harvest Auctions</h1>
          <p class="sub">
            {{
              auth.role() === "FARMER"
                ? "List your harvest for timed competitive bidding to maximize your realized market price."
                : "Bid directly on certified farm harvests. Highest bidder wins direct escrow fulfillment."
            }}
          </p>
        </div>
        <div style="display: flex; gap: 10px;">
          <button
            *ngIf="auth.role() === 'FARMER'"
            class="btn btn-primary"
            (click)="openCreateModal()"
          >
            + Start New Auction
          </button>
          <button class="btn btn-light" (click)="load()">↻ Refresh</button>
        </div>
      </div>

      <!-- Quick Metrics Grid -->
      <div class="grid grid-3" style="margin-top: 24px;">
        <div class="card stat">
          <div class="icon">🔨</div>
          <div class="number" style="color: var(--green);">
            {{ auctions.length }}
          </div>
          <div class="label">Live Open Auctions</div>
        </div>
        <div class="card stat">
          <div class="icon">💰</div>
          <div class="number">
            {{
              (auth.role() === "DEALER"
                ? (dealerWallet?.availableBalance ??
                  dealerWallet?.available_balance ??
                  dealerWallet?.balance)
                : null
              ) | currency: "INR" : "symbol" : "1.0-0"
            }}
          </div>
          <div class="label">
            {{
              auth.role() === "DEALER"
                ? "Your Bidding Balance"
                : "Automated Escrow Protection"
            }}
          </div>
        </div>
        <div class="card stat">
          <div class="icon">⏱️</div>
          <div
            class="number"
            style="font-size: 20px; font-weight: 700; margin-top: 14px;"
          >
            Instant Settlement
          </div>
          <div class="label">Winner Automatically Awarded</div>
        </div>
      </div>

      <!-- Active Auctions Table -->
      <div class="card table-wrap" style="margin-top: 24px;">
        <div
          style="padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line);"
        >
          <h3 style="margin: 0;">
            Active Bidding Lots ({{ auctions.length }})
          </h3>
          <span class="badge" style="background: #eef5ee; color: var(--green);"
            >● Live Bidding Active</span
          >
        </div>

        <table class="table" *ngIf="auctions.length; else emptyAuctions">
          <thead>
            <tr>
              <th>CROP</th>
              <th>FARMER / LOCATION</th>
              <th>BASE FLOOR RATE</th>
              <th>CURRENT TOP BID</th>
              <th>BIDS</th>
              <th>TIME WINDOW</th>
              <th>STATUS</th>
              <th>ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let a of auctions">
              <td>
                <b>{{ a.cropName || a.crop?.cropName || "Deleted Crop" }}</b
                ><br />
                <small class="muted"
                  >Grade {{ a.crop?.grade || "A" }} ·
                  {{ a.quantity || a.crop?.quantity || 10 }} quintals</small
                >
              </td>
              <td>
                <b>{{ a.location || a.crop?.location || "Mandi Yard" }}</b
                ><br />
                <small class="muted">{{ a.farmerName || "Farmer" }}</small>
              </td>
              <td>
                <b>{{ a.basePrice | currency: "INR" : "symbol" : "1.0-0" }}</b>
                / quintal
              </td>
              <td>
                <b
                  *ngIf="a.highestBid"
                  style="color: var(--green); font-size: 15px;"
                >
                  {{ a.highestBid | currency: "INR" : "symbol" : "1.0-0" }} /
                  quintal
                </b>
                <span *ngIf="!a.highestBid" class="muted">No bids yet</span>
                <div
                  *ngIf="a.winnerName && a.winnerName !== 'No bids'"
                  style="font-size: 11px; color: var(--muted);"
                >
                  Top Bidder: {{ a.winnerName }}
                </div>
              </td>
              <td>
                <span
                  class="badge"
                  style="background: #eef5ee; color: var(--green); font-weight: 600;"
                >
                  🔨 {{ bidCount(a) }} bids
                </span>
              </td>
              <td>
                <span class="muted" style="font-size: 12px;"
                  >Started: {{ a.startTime | date: "shortTime" }}</span
                ><br />
                <span style="font-weight: 600; color: #d35400;"
                  >Ends: {{ a.endTime | date: "shortTime" }}</span
                >
              </td>
              <td>
                <span
                  class="badge"
                  [style.background]="
                    a.status === 'OPEN' || a.status === 'ACTIVE'
                      ? '#eef5ee'
                      : a.status === 'SCHEDULED'
                        ? '#e8f0fe'
                        : '#fff8e1'
                  "
                  [style.color]="
                    a.status === 'OPEN' || a.status === 'ACTIVE'
                      ? 'var(--green)'
                      : a.status === 'SCHEDULED'
                        ? '#1a73e8'
                        : '#b78103'
                  "
                >
                  {{
                    a.status === "SCHEDULED"
                      ? "UPCOMING"
                      : a.status === "OPEN" || a.status === "ACTIVE"
                        ? "LIVE"
                        : a.status === "ENDED"
                          ? "ENDED"
                          : a.status
                  }}
                </span>
              </td>
              <td>
                <div
                  style="display: flex; gap: 6px; flex-wrap: wrap; align-items: center;"
                >
                  <!-- DEALER: Place Bid if LIVE -->
                  <button
                    *ngIf="
                      auth.role() === 'DEALER' &&
                      (a.status === 'OPEN' || a.status === 'ACTIVE')
                    "
                    class="btn btn-primary btn-small"
                    (click)="openBidModal(a)"
                  >
                    Place Bid
                  </button>

                  <!-- ENDED AUCTION ACTIONS -->
                  <ng-container *ngIf="a.status === 'ENDED'">
                    <!-- Winner (Dealer) Actions -->
                    <ng-container *ngIf="isUserWinner(a)">
                      <button
                        *ngIf="!a.orderId"
                        class="btn btn-primary btn-small"
                        (click)="createAuctionOrder(a)"
                        [disabled]="creatingOrderAuctionId === a.id"
                        style="background: #176b45; font-weight: 700;"
                      >
                        {{
                          creatingOrderAuctionId === a.id
                            ? "Creating..."
                            : "📦 Create Order"
                        }}
                      </button>
                      <ng-container *ngIf="a.orderId">
                        <span
                          class="badge"
                          style="background: #e8f5e9; color: #1b5e20; font-size: 11px; font-weight: 700;"
                        >
                          ✓ Order Created
                        </span>
                        <a
                          *ngIf="a.paymentStatus !== 'PAID'"
                          [routerLink]="['/orders']"
                          [queryParams]="{ payOrder: a.orderId }"
                          class="btn btn-primary btn-small"
                          style="background: #176b45; font-weight: 700;"
                        >
                          💳 Pay Order
                        </a>
                        <a routerLink="/orders" class="btn btn-light btn-small">
                          👁 View Order
                        </a>
                      </ng-container>
                    </ng-container>

                    <!-- Non-Winner Dealer -->
                    <span
                      *ngIf="auth.role() === 'DEALER' && !isUserWinner(a)"
                      class="badge"
                      style="background: #f0f2ef; color: #53615a; font-size: 11px;"
                    >
                      Ended ·
                      {{ a.winnerName ? "Won by " + a.winnerName : "No bids" }}
                    </span>

                    <!-- Farmer -->
                    <ng-container *ngIf="auth.role() === 'FARMER'">
                      <a
                        *ngIf="a.orderId"
                        routerLink="/orders"
                        class="btn btn-light btn-small"
                      >
                        👁 View Order
                      </a>
                      <span
                        *ngIf="!a.orderId"
                        class="badge"
                        style="background: #fff8e1; color: #b78103; font-size: 11px;"
                      >
                        Ended · Awaiting Order
                      </span>
                    </ng-container>

                    <!-- Admin -->
                    <ng-container *ngIf="auth.role() === 'ADMIN'">
                      <a
                        *ngIf="a.orderId"
                        routerLink="/orders"
                        class="btn btn-light btn-small"
                      >
                        👁 View Order
                      </a>
                      <span
                        *ngIf="!a.orderId"
                        class="badge"
                        style="background: #fff8e1; color: #b78103; font-size: 11px;"
                      >
                        Ended
                      </span>
                    </ng-container>
                  </ng-container>

                  <!-- ANYONE: View Bid History -->
                  <button class="btn btn-light btn-small" (click)="viewBids(a)">
                    Bids
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #emptyAuctions>
          <div class="empty" style="padding: 60px 20px;">
            <div style="font-size: 40px; margin-bottom: 10px;">🔨</div>
            <h3>No active auctions open right now</h3>
            <p class="muted">
              {{
                auth.role() === "FARMER"
                  ? 'Click "+ Start New Auction" to put one of your listed harvests up for live bidding.'
                  : "Check back soon! Farmers frequently list fresh harvests for competitive bidding."
              }}
            </p>
            <button
              *ngIf="auth.role() === 'FARMER'"
              class="btn btn-primary"
              (click)="openCreateModal()"
              style="margin-top: 10px;"
            >
              + Start Auction
            </button>
          </div>
        </ng-template>
      </div>

      <!-- Completed Auction History Table -->
      <div class="card table-wrap" style="margin-top: 32px;">
        <div
          style="padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--line);"
        >
          <h3 style="margin: 0;">
            Completed Auction History ({{ historyAuctions.length }})
          </h3>
          <span class="badge" style="background: #f0f2ef; color: #53615a;"
            >Archived Results</span
          >
        </div>

        <table class="table" *ngIf="historyAuctions.length; else emptyHistory">
          <thead>
            <tr>
              <th>CROP</th>
              <th>FARMER / LOCATION</th>
              <th>BASE FLOOR</th>
              <th>FINAL BID</th>
              <th>ORDER & ESCROW</th>
              <th>STATUS</th>
              <th>WINNER</th>
              <th>TIME WINDOW</th>
              <th>ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let h of historyAuctions">
              <td>
                <b>{{ h.cropName || h.crop?.cropName || "Deleted Crop" }}</b
                ><br />
                <small class="muted"
                  >{{
                    h.quantity && h.quantity > 0
                      ? h.quantity
                      : h.crop?.quantity || 5
                  }}
                  quintals</small
                >
              </td>
              <td>
                <b>{{ h.location || h.crop?.location || "Mandi Yard" }}</b
                ><br />
                <small class="muted">{{ h.farmerName || "Farmer" }}</small>
              </td>
              <td>
                <b>{{ h.basePrice | currency: "INR" : "symbol" : "1.0-0" }}</b>
                / qtl
              </td>
              <td>
                <b
                  *ngIf="h.highestBid"
                  style="color: var(--green); font-size: 15px;"
                >
                  {{ h.highestBid | currency: "INR" : "symbol" : "1.0-0" }} /
                  qtl
                </b>
                <span *ngIf="!h.highestBid" class="muted">No bids placed</span>
                <div *ngIf="h.totalBids" class="muted" style="font-size: 11px;">
                  {{ h.totalBids }} bids placed
                </div>
              </td>
              <td>
                <div *ngIf="h.orderNumber || h.orderId">
                  <span style="font-weight: 700; font-size: 12px;"
                    >#{{ h.orderNumber || h.orderId }}</span
                  ><br />
                  <span
                    class="badge"
                    [style.background]="
                      h.paymentStatus === 'PAID' ? '#eef5ee' : '#fff8e1'
                    "
                    [style.color]="
                      h.paymentStatus === 'PAID' ? 'var(--green)' : '#b78103'
                    "
                    style="font-size: 10px; margin-top: 2px;"
                  >
                    💳 {{ h.paymentStatus || "PENDING" }}
                  </span>
                  <span
                    class="badge"
                    [style.background]="
                      h.deliveryStatus === 'DELIVERED' ? '#eef5ee' : '#f0f2ef'
                    "
                    [style.color]="
                      h.deliveryStatus === 'DELIVERED'
                        ? 'var(--green)'
                        : '#53615a'
                    "
                    style="font-size: 10px; margin-left: 4px;"
                  >
                    🚚 {{ h.deliveryStatus || "PENDING" }}
                  </span>
                </div>
                <div
                  *ngIf="!h.orderNumber && !h.orderId"
                  class="muted"
                  style="font-size: 12px;"
                >
                  No Order Generated
                </div>
              </td>
              <td>
                <span
                  class="badge"
                  [style.background]="
                    h.status === 'COMPLETED'
                      ? '#eef5ee'
                      : h.status === 'EXPIRED'
                        ? '#fde8e8'
                        : '#eef5ee'
                  "
                  [style.color]="
                    h.status === 'COMPLETED'
                      ? 'var(--green)'
                      : h.status === 'EXPIRED'
                        ? '#c81e1e'
                        : 'var(--green)'
                  "
                >
                  {{ h.status }}
                </span>
                <div *ngIf="isUserWinner(h)" style="margin-top: 4px;">
                  <span
                    class="badge"
                    style="background: #eef7e6; color: #176b45; font-weight: 800; border: 1px solid #cfe96b;"
                  >
                    🏆 WON LOT
                  </span>
                </div>
              </td>
              <td>
                <span
                  *ngIf="
                    h.winnerName &&
                    h.winnerName !== 'No bids' &&
                    h.winnerName !== 'None'
                  "
                  style="font-weight: 700; color: var(--green);"
                >
                  {{ h.winnerName }}
                </span>
                <span
                  *ngIf="
                    !h.winnerName ||
                    h.winnerName === 'No bids' ||
                    h.winnerName === 'None'
                  "
                  class="muted"
                >
                  No Winner
                </span>
              </td>
              <td>
                <span class="muted" style="font-size: 11px;"
                  >Started: {{ h.startTime | date: "shortTime" }}</span
                ><br />
                <span style="font-weight: 600; font-size: 11px;"
                  >Ended: {{ h.endTime | date: "shortTime" }}</span
                >
              </td>
              <td>
                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                  <button
                    class="btn btn-light btn-small"
                    (click)="viewHistoryDetails(h)"
                  >
                    Details
                  </button>
                  <!-- WINNING DEALER: Prominent PAY NOW Button if Payment Pending -->
                  <a
                    *ngIf="isUserWinner(h) && h.paymentStatus !== 'PAID'"
                    [routerLink]="['/orders']"
                    [queryParams]="{ payOrder: h.orderId || h.id }"
                    class="btn btn-primary btn-small"
                    style="background: #176b45; border: 2px solid #cfe96b; box-shadow: 0 3px 10px rgba(23, 107, 69, 0.3); font-weight: 800;"
                  >
                    💳 PAY NOW
                  </a>
                  <!-- WINNING DEALER: Show PAID Badge if Payment Done -->
                  <span
                    *ngIf="isUserWinner(h) && h.paymentStatus === 'PAID'"
                    class="badge"
                    style="background: #eef5ee; color: var(--green); font-weight: 800; align-self: center;"
                  >
                    ✓ PAID
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #emptyHistory>
          <div class="empty" style="padding: 40px 20px;">
            <p class="muted" style="margin: 0;">
              No completed auctions recorded yet.
            </p>
          </div>
        </ng-template>
      </div>

      <!-- START NEW AUCTION DEDICATED VIEW / PAGE (Farmer) -->
      <div
        *ngIf="activeView === 'create'"
        class="card form-card"
        style="margin-top: 24px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;"
      >
        <div class="split" style="margin-bottom: 14px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">
              NEW HARVEST AUCTION
            </div>
            <h2 style="margin: 4px 0;">Start Timed Auction Lot</h2>
            <p class="sub" style="margin: 0;">
              Configure your listed harvest for live timed competitive bidding
              by verified dealers.
            </p>
          </div>
          <button
            class="btn btn-light btn-small"
            (click)="activeView = 'list'; showCreateModal = false"
          >
            ← Back to Auctions
          </button>
        </div>

        <form (ngSubmit)="submitCreateAuction()">
          <div class="form-grid">
            <div class="field">
              <label>Select Crop Lot *</label>
              <select
                [(ngModel)]="createForm.cropId"
                name="cropId"
                (change)="onCropSelected()"
                required
              >
                <option [ngValue]="null">
                  -- Choose from your listed crops --
                </option>
                <option *ngFor="let c of farmerCrops" [ngValue]="c.id">
                  {{ c.cropName }} ({{ c.quantity }} qtl · Grade {{ c.grade }} ·
                  ₹{{ c.expectedPrice }})
                </option>
              </select>
            </div>

            <div class="field">
              <label>Quality Grade *</label>
              <input
                [(ngModel)]="createForm.grade"
                name="grade"
                readonly
                style="background: #f0f2ef; color: #53615a;"
              />
            </div>

            <div class="field">
              <label>Auction Duration (Minutes) *</label>
              <select
                [(ngModel)]="createForm.durationMinutes"
                name="durationMinutes"
                required
              >
                <option [ngValue]="3">3 Minutes (Quick Auction)</option>
                <option [ngValue]="30">30 Minutes (Flash Auction)</option>
                <option [ngValue]="60">1 Hour (Standard)</option>
                <option [ngValue]="120">2 Hours</option>
                <option [ngValue]="360">6 Hours</option>
                <option [ngValue]="1440">24 Hours (Full Day)</option>
              </select>
            </div>

            <div class="field">
              <label>Base Price Guarantee</label>
              <input
                [value]="
                  selectedCrop
                    ? 'Calculated by dynamic pricing (Benchmark: ₹' +
                      selectedCrop.expectedPrice +
                      ')'
                    : 'Select crop first'
                "
                readonly
                style="background: #f0f2ef; color: #53615a;"
              />
            </div>
          </div>

          <div style="display: flex; gap: 12px; margin-top: 20px;">
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="submitting || !createForm.cropId"
            >
              {{ submitting ? "Launching Auction…" : "🚀 Launch Live Auction" }}
            </button>
            <button
              type="button"
              class="btn btn-light"
              (click)="activeView = 'list'; showCreateModal = false"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>

      <!-- PLACE BID MODAL (Dealer) -->
      <div
        *ngIf="showBidModal && selectedAuction"
        class="card form-card"
        style="margin-top: 24px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;"
      >
        <div class="split" style="margin-bottom: 14px;">
          <div>
            <div class="eyebrow" style="color: var(--green);">
              COMPETITIVE BID
            </div>
            <h2 style="margin: 4px 0;">
              Place Bid on
              {{
                selectedAuction.cropName ||
                  selectedAuction.crop?.cropName ||
                  "Harvest Auction"
              }}
            </h2>
          </div>
          <button
            class="btn btn-light btn-small"
            (click)="showBidModal = false"
          >
            ✕ Close
          </button>
        </div>

        <div
          style="display: flex; gap: 20px; background: #f7faf7; padding: 16px; border-radius: 12px; margin-bottom: 18px;"
        >
          <div>
            <span class="muted" style="font-size: 12px;">Base Floor Price:</span
            ><br />
            <b
              >{{
                selectedAuction.basePrice | currency: "INR" : "symbol" : "1.0-0"
              }}
              / qtl</b
            >
          </div>
          <div>
            <span class="muted" style="font-size: 12px;"
              >Current Highest Bid:</span
            ><br />
            <b style="color: var(--green);"
              >{{
                selectedAuction.highestBid || selectedAuction.basePrice
                  | currency: "INR" : "symbol" : "1.0-0"
              }}
              / qtl</b
            >
          </div>
          <div>
            <span class="muted" style="font-size: 12px;">Minimum Next Bid:</span
            ><br />
            <b style="color: #b78103;"
              >{{ minAllowedBid() | currency: "INR" : "symbol" : "1.0-0" }} /
              qtl</b
            >
          </div>
        </div>

        <form (ngSubmit)="submitBid()">
          <div class="field" style="max-width: 380px;">
            <label>Your Bid Amount (₹ per quintal) *</label>
            <input
              type="number"
              [(ngModel)]="bidAmount"
              name="bidAmount"
              required
              [min]="minAllowedBid()"
              step="10"
              placeholder="e.g. 3800"
            />
            <small class="muted"
              >Must be greater than current highest bid.</small
            >
          </div>

          <div style="display: flex; gap: 12px; margin-top: 20px;">
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="
                submitting || !bidAmount || bidAmount < minAllowedBid()
              "
            >
              {{ submitting ? "Submitting Bid…" : "✓ Confirm & Submit Bid" }}
            </button>
            <button
              type="button"
              class="btn btn-light"
              (click)="showBidModal = false"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>

      <!-- BID HISTORY MODAL -->
      <div
        *ngIf="showHistoryModal && selectedAuction"
        class="card form-card"
        style="margin-top: 24px; animation: fadeIn .2s ease-out;"
      >
        <div class="split" style="margin-bottom: 14px;">
          <div>
            <div class="eyebrow">BID LOG</div>
            <h2 style="margin: 4px 0;">
              Bid History for
              {{
                selectedAuction.cropName ||
                  selectedAuction.crop?.cropName ||
                  "Harvest Auction"
              }}
            </h2>
          </div>
          <button
            class="btn btn-light btn-small"
            (click)="showHistoryModal = false"
          >
            ✕ Close
          </button>
        </div>

        <div *ngIf="bidHistory.length; else noBids">
          <table class="table">
            <thead>
              <tr>
                <th>#</th>
                <th>Bidder</th>
                <th>Amount (₹ / qtl)</th>
                <th>Bid Time</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let b of bidHistory; let i = index">
                <td>#{{ i + 1 }}</td>
                <td>
                  <b>{{ b.dealerName || "Dealer" }}</b>
                </td>
                <td>
                  <b style="color: var(--green);">{{
                    b.amount | currency: "INR" : "symbol" : "1.0-0"
                  }}</b>
                </td>
                <td>{{ b.bidTime | date: "medium" }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <ng-template #noBids>
          <div class="empty" style="padding: 30px;">
            No bids have been recorded yet for this auction lot.
          </div>
        </ng-template>
      </div>

      <!-- AUCTION HISTORY DETAILS MODAL -->
      <div
        *ngIf="showHistoryDetailsModal && selectedHistoryAuction"
        class="card form-card"
        style="margin-top: 24px; border: 2px solid var(--green); animation: fadeIn .2s ease-out;"
      >
        <div
          class="split"
          style="margin-bottom: 16px; border-bottom: 1px solid var(--line); padding-bottom: 12px;"
        >
          <div>
            <div class="eyebrow" style="color: var(--green);">
              AUCTION DETAILS
            </div>
            <h2 style="margin: 4px 0;">
              {{
                selectedHistoryAuction.cropName ||
                  selectedHistoryAuction.crop?.cropName ||
                  "Deleted Crop"
              }}
            </h2>
            <div class="muted">Completed Auction Summary</div>
          </div>
          <button
            class="btn btn-light btn-small"
            (click)="showHistoryDetailsModal = false"
          >
            ✕ Close
          </button>
        </div>

        <div
          class="form-grid"
          style="grid-template-columns: repeat(3, 1fr); margin-bottom: 20px;"
        >
          <div>
            <div class="muted" style="font-size: 12px;">Crop</div>
            <b>{{
              selectedHistoryAuction.cropName ||
                selectedHistoryAuction.crop?.cropName ||
                "Deleted Crop"
            }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Farmer</div>
            <b>{{ selectedHistoryAuction.farmerName || "Farmer" }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Location</div>
            <b>{{
              selectedHistoryAuction.location ||
                selectedHistoryAuction.crop?.location ||
                "Mandi Yard"
            }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Quantity</div>
            <b
              >{{
                selectedHistoryAuction.quantity &&
                selectedHistoryAuction.quantity > 0
                  ? selectedHistoryAuction.quantity
                  : selectedHistoryAuction.crop?.quantity || 5
              }}
              quintals</b
            >
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Base Floor Rate</div>
            <b
              >{{
                selectedHistoryAuction.basePrice
                  | currency: "INR" : "symbol" : "1.0-0"
              }}
              / quintal</b
            >
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Final Winning Bid</div>
            <b style="color: var(--green); font-size: 16px;">
              {{
                selectedHistoryAuction.highestBid
                  ? (selectedHistoryAuction.highestBid
                      | currency: "INR" : "symbol" : "1.0-0") + " / quintal"
                  : "No bids placed"
              }}
            </b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Total Bids</div>
            <b>{{ selectedHistoryAuction.totalBids || 0 }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Winner</div>
            <b
              [style.color]="
                selectedHistoryAuction.winnerName &&
                selectedHistoryAuction.winnerName !== 'No bids'
                  ? 'var(--green)'
                  : 'inherit'
              "
            >
              {{
                selectedHistoryAuction.winnerName &&
                selectedHistoryAuction.winnerName !== "No bids"
                  ? selectedHistoryAuction.winnerName
                  : "No Winner"
              }}
            </b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Status</div>
            <span
              class="badge"
              style="background: #eef5ee; color: var(--green);"
              >{{ selectedHistoryAuction.status }}</span
            >
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Auction Started</div>
            <b>{{ selectedHistoryAuction.startTime | date: "medium" }}</b>
          </div>
          <div>
            <div class="muted" style="font-size: 12px;">Auction Ended</div>
            <b>{{ selectedHistoryAuction.endTime | date: "medium" }}</b>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <a
            *ngIf="
              isUserWinner(selectedHistoryAuction) &&
              selectedHistoryAuction.paymentStatus !== 'PAID'
            "
            [routerLink]="['/orders']"
            [queryParams]="{
              payOrder:
                selectedHistoryAuction.orderId || selectedHistoryAuction.id,
            }"
            class="btn btn-primary"
            style="background: #176b45; border: 2px solid #cfe96b; font-weight: 800;"
          >
            💳 PAY NOW
          </a>
          <span
            *ngIf="
              isUserWinner(selectedHistoryAuction) &&
              selectedHistoryAuction.paymentStatus === 'PAID'
            "
            class="badge"
            style="background: #eef5ee; color: var(--green); font-weight: 800; align-self: center;"
          >
            ✓ PAID
          </span>
          <button
            class="btn btn-light"
            (click)="showHistoryDetailsModal = false"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  `,
})
export class AuctionsComponent implements OnInit, OnDestroy {
  api = inject(ApiService);
  auth = inject(AuthService);
  toast = inject(ToastService);
  auctionSocket = inject(AuctionSocketService);
  router = inject(Router);

  private bidSub?: Subscription;
  creatingOrderAuctionId?: number;

  auctions: (Auction & { crop?: Crop })[] = [];
  historyAuctions: (Auction & { crop?: Crop })[] = [];
  farmerCrops: Crop[] = [];
  dealerWallet: any = null;

  activeView: "list" | "create" = "list";
  bidsCountMap: Record<number, number> = {};

  showCreateModal = false;
  showBidModal = false;
  showHistoryModal = false;
  showHistoryDetailsModal = false;
  submitting = false;

  selectedAuction: (Auction & { crop?: Crop }) | null = null;
  selectedHistoryAuction: (Auction & { crop?: Crop }) | null = null;
  selectedCrop: Crop | null = null;
  bidAmount = 0;
  bidHistory: Bid[] = [];

  createForm = {
    cropId: null as number | null,
    grade: "A",
    durationMinutes: 60,
  };

  ngOnInit() {
    this.auctionSocket.connect();
    this.bidSub = this.auctionSocket.bid$.subscribe((liveBid) => {
      this.handleIncomingLiveBid(liveBid);
    });
    this.ensureUserAndLoad();
  }

  ngOnDestroy() {
    if (this.bidSub) {
      this.bidSub.unsubscribe();
    }
  }

  handleIncomingLiveBid(liveBid: AuctionLiveBid) {
    const match = this.auctions.find((a) => a.id === liveBid.auctionId);
    if (match) {
      match.highestBid = liveBid.amount;
      match.winnerName = liveBid.dealerName || "Dealer";
      match.highestBidderId = liveBid.dealerId;
      match.totalBids = (match.totalBids || 0) + 1;
    }

    if (this.selectedAuction && this.selectedAuction.id === liveBid.auctionId) {
      this.selectedAuction.highestBid = liveBid.amount;
      this.selectedAuction.winnerName = liveBid.dealerName || "Dealer";
      this.selectedAuction.highestBidderId = liveBid.dealerId;
      if (this.showHistoryModal) {
        this.bidHistory.unshift(liveBid);
      }
      if (this.showBidModal) {
        this.bidAmount = this.minAllowedBid();
      }
    }
  }

  ensureUserAndLoad() {
    if (!this.auth.userId()) {
      this.auth.loadProfile().subscribe(() => this.load());
    } else {
      this.load();
    }
  }

  load() {
    this.api.get<Auction[]>("/auctions/open").subscribe({
      next: (list) => {
        const rawAuctions = list || [];
        this.auctions = rawAuctions;
        rawAuctions.forEach((a, idx) => {
          this.auctionSocket.subscribeToAuction(a.id);
          this.api.get<Bid[]>("/auctions/" + a.id + "/bids").subscribe({
            next: (bids) => {
              this.bidsCountMap[a.id] = (bids || []).length;
              if (this.auctions[idx]) {
                this.auctions[idx].totalBids = (bids || []).length;
              }
            },
            error: () => {},
          });
          if (a.cropId) {
            this.api.get<Crop>("/crops/" + a.cropId).subscribe({
              next: (c) => {
                this.auctions[idx].crop = c;
                if (!this.auctions[idx].cropName && c.cropName) {
                  this.auctions[idx].cropName = c.cropName;
                }
                if (!this.auctions[idx].location && c.location) {
                  this.auctions[idx].location = c.location;
                }
                if (!this.auctions[idx].quantity && c.quantity) {
                  this.auctions[idx].quantity = c.quantity;
                }
              },
              error: () => {
                if (!this.auctions[idx].cropName) {
                  this.auctions[idx].cropName = "Deleted Crop";
                }
              },
            });
          }
          if (a.farmerId && (!a.farmerName || a.farmerName === "Farmer")) {
            this.api.get<User>("/users/" + a.farmerId).subscribe({
              next: (u) => {
                if (u && u.name) {
                  this.auctions[idx].farmerName = u.name;
                }
              },
              error: () => {},
            });
          }
        });
      },
      error: () => (this.auctions = []),
    });

    this.api.get<Auction[]>("/auctions/history").subscribe({
      next: (list) => {
        const rawHistory = list || [];
        this.historyAuctions = rawHistory;
        rawHistory.forEach((h, idx) => {
          if (h.cropId) {
            this.api.get<Crop>("/crops/" + h.cropId).subscribe({
              next: (c) => {
                this.historyAuctions[idx].crop = c;
                if (!this.historyAuctions[idx].cropName && c.cropName) {
                  this.historyAuctions[idx].cropName = c.cropName;
                }
                if (!this.historyAuctions[idx].location && c.location) {
                  this.historyAuctions[idx].location = c.location;
                }
                if (!this.historyAuctions[idx].quantity && c.quantity) {
                  this.historyAuctions[idx].quantity = c.quantity;
                }
              },
              error: () => {
                if (!this.historyAuctions[idx].cropName) {
                  this.historyAuctions[idx].cropName = "Deleted Crop";
                }
              },
            });
          }
          if (h.farmerId && (!h.farmerName || h.farmerName === "Farmer")) {
            this.api.get<User>("/users/" + h.farmerId).subscribe({
              next: (u) => {
                if (u && u.name) {
                  this.historyAuctions[idx].farmerName = u.name;
                }
              },
              error: () => {},
            });
          }
          if (
            h.highestBidderId &&
            (!h.winnerName ||
              h.winnerName === "Dealer" ||
              h.winnerName === "No bids")
          ) {
            this.api.get<User>("/users/" + h.highestBidderId).subscribe({
              next: (u) => {
                if (u && u.name) {
                  this.historyAuctions[idx].winnerName = u.name;
                }
              },
              error: () => {},
            });
          }
        });
      },
      error: () => (this.historyAuctions = []),
    });

    const uid = this.auth.userId();
    if (this.auth.role() === "FARMER" && uid) {
      this.api.get<Crop[]>("/crops/farmer/" + uid).subscribe({
        next: (crops) =>
          (this.farmerCrops = (crops || []).filter(
            (c) => c.status === "AVAILABLE",
          )),
        error: () => (this.farmerCrops = []),
      });
    }

    if (this.auth.role() === "DEALER" && uid) {
      this.api.get<any>("/wallets/" + uid).subscribe({
        next: (w) => (this.dealerWallet = w),
        error: () => {
          this.api.get<any>("/wallets/dealer/" + uid).subscribe({
            next: (w2) => (this.dealerWallet = w2),
            error: () => (this.dealerWallet = null),
          });
        },
      });
    }
  }

  bidCount(a: Auction): number {
    return this.bidsCountMap[a.id] ?? a.totalBids ?? 0;
  }

  openCreateModal() {
    this.activeView = "create";
    this.showCreateModal = true;
    this.showBidModal = false;
    this.showHistoryModal = false;
    this.showHistoryDetailsModal = false;
    this.createForm = {
      cropId: null,
      grade: "A",
      durationMinutes: 60,
    };
    this.selectedCrop = null;
  }

  onCropSelected() {
    this.selectedCrop =
      this.farmerCrops.find((c) => c.id === this.createForm.cropId) || null;
    if (this.selectedCrop) {
      this.createForm.grade = this.selectedCrop.grade || "A";
    }
  }

  submitCreateAuction() {
    const farmerId = this.auth.userId();
    if (!farmerId || !this.createForm.cropId) {
      this.toast.show("Please select a valid crop lot", "error");
      return;
    }

    this.submitting = true;
    const payload = {
      cropId: this.createForm.cropId,
      farmerId: farmerId,
      grade: this.createForm.grade,
      durationMinutes: Number(this.createForm.durationMinutes),
    };

    this.api.post<Auction>("/auctions", payload).subscribe({
      next: (res) => {
        this.submitting = false;
        this.showCreateModal = false;
        this.activeView = "list";
        this.toast.show(
          "Auction for " +
            (this.selectedCrop?.cropName || "crop") +
            " launched successfully!",
        );
        this.load();
      },
      error: (e: any) => {
        this.submitting = false;
        this.toast.show(
          e.error?.message || "Failed to create auction",
          "error",
        );
      },
    });
  }

  openBidModal(a: Auction & { crop?: Crop }) {
    this.selectedAuction = a;
    this.showBidModal = true;
    this.showCreateModal = false;
    this.showHistoryModal = false;
    this.showHistoryDetailsModal = false;
    this.bidAmount = this.minAllowedBid();
  }

  minAllowedBid(): number {
    if (!this.selectedAuction) return 100;
    const top =
      this.selectedAuction.highestBid || this.selectedAuction.basePrice || 100;
    return top + 50;
  }

  submitBid() {
    if (!this.selectedAuction) return;
    const dealerId = this.auth.userId();
    if (!dealerId) {
      this.toast.show(
        "Please complete your dealer profile before bidding",
        "error",
      );
      return;
    }

    this.submitting = true;
    const payload = {
      dealerId: dealerId,
      amount: Number(this.bidAmount),
    };

    this.api
      .post<Bid>("/auctions/" + this.selectedAuction.id + "/bids", payload)
      .subscribe({
        next: (b) => {
          this.submitting = false;
          this.showBidModal = false;
          this.toast.show("Bid of ₹" + b.amount + " placed successfully!");
          this.load();
        },
        error: (e: any) => {
          this.submitting = false;
          const msg =
            typeof e.error === "string" && e.error.trim()
              ? e.error
              : e.error?.message || "Failed to place bid";
          this.toast.show(msg, "error");
        },
      });
  }

  viewBids(a: Auction & { crop?: Crop }) {
    this.selectedAuction = a;
    this.showHistoryModal = true;
    this.showBidModal = false;
    this.showCreateModal = false;
    this.showHistoryDetailsModal = false;
    this.bidHistory = [];

    this.api.get<Bid[]>("/auctions/" + a.id + "/bids").subscribe({
      next: (list) => {
        const bids = list || [];
        this.bidHistory = bids;
        bids.forEach((b, idx) => {
          if (b.dealerId && (!b.dealerName || b.dealerName === "Dealer")) {
            this.api.get<User>("/users/" + b.dealerId).subscribe({
              next: (u) => {
                if (u && u.name) {
                  this.bidHistory[idx].dealerName = u.name;
                }
              },
              error: () => {},
            });
          }
        });
      },
      error: () => (this.bidHistory = []),
    });
  }

  isUserWinner(h: Auction | null | undefined): boolean {
    if (!h) return false;
    const uid = this.auth.userId();
    if (!uid || this.auth.role() !== "DEALER") return false;
    if (h.highestBidderId && h.highestBidderId === uid) return true;
    const currentName = this.auth.currentUser()?.name;
    if (
      currentName &&
      h.winnerName &&
      h.winnerName.toLowerCase() === currentName.toLowerCase()
    )
      return true;
    return false;
  }

  viewHistoryDetails(h: Auction & { crop?: Crop }) {
    this.selectedHistoryAuction = h;
    this.showHistoryDetailsModal = true;
    this.showHistoryModal = false;
    this.showBidModal = false;
    this.showCreateModal = false;
  }

  createAuctionOrder(a: Auction) {
    this.creatingOrderAuctionId = a.id;
    this.api.post<any>("/orders/from-auction/" + a.id, {}).subscribe({
      next: (order: any) => {
        this.creatingOrderAuctionId = undefined;
        this.toast.show("Order created from won auction! Proceed to payment.");
        this.load();
        this.router.navigate(["/orders"], {
          queryParams: { payOrder: order?.id || a.orderId },
        });
      },
      error: (e: any) => {
        this.creatingOrderAuctionId = undefined;
        const msg =
          typeof e.error === "string" && e.error.trim()
            ? e.error
            : e.error?.message || "Could not create order from auction";
        this.toast.show(msg, "error");
      },
    });
  }
}
