import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080';

  get<T>(path: string, params?: Record<string, string | number | undefined>): Observable<T> {
    let p = new HttpParams();
    Object.entries(params || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== '') p = p.set(k, String(v));
    });
    return this.http.get<T>(this.base + path, { params: p });
  }

  getText(path: string): Observable<string> {
    return this.http.get(this.base + path, { responseType: 'text' });
  }

  getBlob(path: string): Observable<Blob> {
    return this.http.get(this.base + path, { responseType: 'blob' });
  }

  post<T>(path: string, b: unknown): Observable<T> {
    return this.http.post<T>(this.base + path, b);
  }

  put<T>(path: string, b: unknown): Observable<T> {
    return this.http.put<T>(this.base + path, b);
  }

  patch<T>(path: string, b?: unknown): Observable<T> {
    return this.http.patch<T>(this.base + path, b ?? {});
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(this.base + path);
  }

  image(path: string): string {
    return this.base + path;
  }
}
