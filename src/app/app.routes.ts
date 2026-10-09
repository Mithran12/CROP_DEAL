import { Routes } from "@angular/router";
import { authGuard, adminGuard, profileGuard } from "./core/guard";

export const routes: Routes = [
  { path: "", redirectTo: "login", pathMatch: "full" },
  {
    path: "login",
    loadComponent: () =>
      import("./pages/auth/login.component").then((m) => m.LoginComponent),
  },
  {
    path: "register",
    loadComponent: () =>
      import("./pages/auth/register.component").then(
        (m) => m.RegisterComponent,
      ),
  },
  {
    path: "forgot-password",
    loadComponent: () =>
      import("./pages/auth/forgot-password.component").then(
        (m) => m.ForgotPasswordComponent,
      ),
  },
  {
    path: "dashboard",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/dashboard/dashboard.component").then(
        (m) => m.DashboardComponent,
      ),
  },
  {
    path: "marketplace",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/marketplace/marketplace.component").then(
        (m) => m.MarketplaceComponent,
      ),
  },
  {
    path: "market-prices",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/market-prices/market-prices.component").then(
        (m) => m.MarketPricesComponent,
      ),
  },
  {
    path: "negotiations",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/negotiations/negotiations.component").then(
        (m) => m.NegotiationsComponent,
      ),
  },
  {
    path: "crops",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/crops/crops.component").then((m) => m.CropsComponent),
  },
  {
    path: "auctions",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/auctions/auctions.component").then(
        (m) => m.AuctionsComponent,
      ),
  },
  {
    path: "orders",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/orders/orders.component").then((m) => m.OrdersComponent),
  },
  {
    path: "subscriptions",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/subscriptions/subscriptions.component").then(
        (m) => m.SubscriptionsComponent,
      ),
  },
  {
    path: "wallet",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/wallet/wallet.component").then((m) => m.WalletComponent),
  },
  {
    path: "reviews",
    canActivate: [authGuard, profileGuard],
    loadComponent: () =>
      import("./pages/reviews/reviews.component").then(
        (m) => m.ReviewsComponent,
      ),
  },
  {
    path: "profile",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./pages/profile/profile.component").then(
        (m) => m.ProfileComponent,
      ),
  },
  {
    path: "admin",
    canActivate: [adminGuard],
    loadComponent: () =>
      import("./pages/admin/admin.component").then((m) => m.AdminComponent),
  },
  { path: "**", redirectTo: "login" },
];
