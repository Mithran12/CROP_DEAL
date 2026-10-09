import { Injectable, inject } from '@angular/core';
import { Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';
import { Bid } from './models';

export interface AuctionLiveBid extends Bid {
  cropName?: string;
}

@Injectable({ providedIn: 'root' })
export class AuctionSocketService {
  private auth = inject(AuthService);
  private toast = inject(ToastService);

  private ws: WebSocket | null = null;
  private reconnectTimer: any = null;
  private isConnected = false;
  private currentSubId = 0;
  private activeSubscriptions = new Set<string>();

  public bid$ = new Subject<AuctionLiveBid>();

  connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    try {
      this.ws = new WebSocket('ws://localhost:8080/ws-auction');

      this.ws.onopen = () => {
        const connectFrame = 'CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\0';
        this.ws?.send(connectFrame);
      };

      this.ws.onmessage = (event) => {
        const data = typeof event.data === 'string' ? event.data : '';

        if (data.startsWith('CONNECTED')) {
          this.isConnected = true;
          // Resubscribe to all active topics
          this.activeSubscriptions.forEach(topic => this.sendSubscribeFrame(topic));
        } else if (data.startsWith('MESSAGE')) {
          this.handleStompMessage(data);
        }
      };

      this.ws.onerror = () => {
        // Will reconnect on close
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        this.ws = null;
        this.scheduleReconnect();
      };
    } catch {
      this.scheduleReconnect();
    }
  }

  subscribeToAuction(auctionId: number) {
    const topic = `/topic/auctions/${auctionId}`;
    this.activeSubscriptions.add(topic);
    if (this.isConnected && this.ws?.readyState === WebSocket.OPEN) {
      this.sendSubscribeFrame(topic);
    } else {
      this.connect();
    }
  }

  unsubscribeFromAuction(auctionId: number) {
    const topic = `/topic/auctions/${auctionId}`;
    this.activeSubscriptions.delete(topic);
  }

  private sendSubscribeFrame(topic: string) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    this.currentSubId++;
    const subFrame = `SUBSCRIBE\nid:sub-auction-${this.currentSubId}\ndestination:${topic}\nack:auto\n\n\0`;
    this.ws.send(subFrame);
  }

  private handleStompMessage(frame: string) {
    try {
      const doubleNewlineIndex = frame.indexOf('\n\n');
      if (doubleNewlineIndex === -1) return;

      let body = frame.substring(doubleNewlineIndex + 2);
      if (body.endsWith('\0')) {
        body = body.substring(0, body.length - 1);
      }

      const bid: AuctionLiveBid = JSON.parse(body.trim());
      if (bid && bid.auctionId) {
        this.bid$.next(bid);
        const bidder = bid.dealerName || 'Dealer';
        this.toast.show(`🔨 Live Bid: ₹${bid.amount} placed by ${bidder} on Auction #${bid.auctionId}!`);
      }
    } catch {
      // Ignore non-JSON STOMP frames
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => {
      if (this.auth.isLoggedIn() && this.activeSubscriptions.size > 0) {
        this.connect();
      }
    }, 4000);
  }

  disconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    if (this.ws) {
      try {
        this.ws.send('DISCONNECT\n\n\0');
        this.ws.close();
      } catch {}
      this.ws = null;
    }
    this.isConnected = false;
    this.activeSubscriptions.clear();
  }
}
