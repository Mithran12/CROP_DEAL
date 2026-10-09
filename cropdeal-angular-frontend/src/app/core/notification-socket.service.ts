import { Injectable, inject } from '@angular/core';
import { Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';
import { Notification } from './models';

@Injectable({ providedIn: 'root' })
export class NotificationSocketService {
  private auth = inject(AuthService);
  private toast = inject(ToastService);

  private ws: WebSocket | null = null;
  private reconnectTimer: any = null;
  private isConnected = false;
  private currentSubId = 0;

  public notification$ = new Subject<Notification>();

  connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    const uid = this.auth.userId();
    if (!uid) return;

    try {
      // Connect to Spring WebSocket STOMP endpoint
      this.ws = new WebSocket('ws://localhost:8080/ws-notifications');

      this.ws.onopen = () => {
        // Send STOMP CONNECT frame
        const connectFrame = 'CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\0';
        this.ws?.send(connectFrame);
      };

      this.ws.onmessage = (event) => {
        const data = typeof event.data === 'string' ? event.data : '';
        
        if (data.startsWith('CONNECTED')) {
          this.isConnected = true;
          this.subscribeToTopic(`/topic/notifications/${uid}`);
        } else if (data.startsWith('MESSAGE')) {
          this.handleStompMessage(data);
        }
      };

      this.ws.onerror = () => {
        // Silent error handling; will reconnect
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

  private subscribeToTopic(topic: string) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    this.currentSubId++;
    const subFrame = `SUBSCRIBE\nid:sub-${this.currentSubId}\ndestination:${topic}\nack:auto\n\n\0`;
    this.ws.send(subFrame);
  }

  private handleStompMessage(frame: string) {
    try {
      // Split headers and body separated by double newline
      const doubleNewlineIndex = frame.indexOf('\n\n');
      if (doubleNewlineIndex === -1) return;

      let body = frame.substring(doubleNewlineIndex + 2);
      // Remove trailing null character
      if (body.endsWith('\0')) {
        body = body.substring(0, body.length - 1);
      }

      const notif: Notification = JSON.parse(body.trim());
      if (notif && notif.message) {
        this.notification$.next(notif);
        this.toast.show(`🔔 ${notif.message}`);
      }
    } catch {
      // Ignore unparseable frames
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => {
      if (this.auth.isLoggedIn()) {
        this.connect();
      }
    }, 5000);
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
  }
}
