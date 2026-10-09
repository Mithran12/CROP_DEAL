export type Role = 'FARMER' | 'DEALER' | 'ADMIN';

export interface AuthResponse {
  userId: number;
  email: string;
  role: Role;
  message: string;
  token: string | null;
  redirectPath?: string;
}

export interface User {
  id: number;
  authUserId: number;
  name: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  role: Role;
  bankAccountNumber?: string;
  accountHolderName?: string;
  branch?: string;
  bankName?: string;
  ifscCode?: string;
  upiId?: string;
  active: boolean;
}

export interface Crop {
  id: number;
  farmerId: number;
  category: string;
  cropName: string;
  grade: string;
  quantity: number;
  expectedPrice: number;
  location: string;
  status: string;
  publishedAt?: string;
  imageUrl?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  farmerId: number;
  dealerId: number;
  cropId: number;
  negotiationId?: number;
  auctionId?: number;
  quantity: number;
  agreedPrice: number;
  totalAmount: number;
  status: string;
  paymentStatus?: string;
  pickupStatus?: string;
  deliveryConfirmed?: boolean;
  deliveryQrCode?: string;
  deliveryQrExpiresAt?: string;
  deliveryQrVerifiedAt?: string;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Payment {
  id: number;
  paymentNumber?: string;
  orderId: number;
  dealerId: number;
  farmerId: number;
  amount: number;
  paymentMethod: string;
  status: string;
  transactionReference?: string;
  createdAt?: string;
  completedAt?: string;
}

export interface Offer {
  id: number;
  negotiationId: number;
  actorId: number;
  actorRole: string;
  amount: number;
  status: string;
  createdAt: string;
}

export interface Negotiation {
  id: number;
  cropId: number;
  quantity: number;
  farmerId: number;
  dealerId: number;
  askingPrice: number;
  currentOffer: number;
  status: 'OPEN' | 'COUNTERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'COMPLETED' | 'CANCELLED';
  nextAction?: string;
  orderId?: number;
  orderNumber?: string;
  cropName?: string;
  farmerName?: string;
  dealerName?: string;
  createdAt?: string;
  updatedAt?: string;
  acceptedAt?: string;
  offers: Offer[];
}

export interface Review {
  id?: number;
  orderId: number;
  reviewerId?: number;
  targetUserId?: number;
  reviewType?: string;
  rating: number;
  review: string;
  createdAt?: string;
}

export interface Subscription {
  id?: number;
  dealerId: number;
  cropName: string;
  category: string;
  location: string;
  minimumQuantity: number;
  maximumPrice: number;
  active?: boolean;
  createdAt?: string;
}

export interface Notification {
  id: number;
  userId: number;
  title?: string;
  message: string;
  read?: boolean;
  createdAt?: string;
}

export interface Wallet {
  dealerId: number;
  balance?: number;
  availableBalance?: number;
  available_balance?: number;
  blockedBalance?: number;
  blocked_balance?: number;
  escrowBalance?: number;
  escrow_balance?: number;
  updatedAt?: string;
}

export interface FarmerWallet {
  farmerId: number;
  balance: number;
  totalEarnings: number;
  updatedAt?: string;
}

export interface WalletTransaction {
  id: number;
  dealerId?: number;
  userId?: number;
  transactionType: string;
  amount: number;
  reference?: string;
  createdAt: string;
  paymentMethod?: string;
  status?: string;
  source?: string;
  destination?: string;
  description?: string;
  auctionId?: number;
  orderId?: number;
}

export interface Invoice {
  orderId: number;
  orderNumber: string;

  farmerId?: number;
  dealerId?: number;
  cropId?: number;

  farmerName?: string;
  dealerName?: string;
  cropName?: string;

  quantity: number;
  unitPrice: number;
  agreedPrice?: number;
  totalAmount: number;

  orderStatus?: string;
  orderDate?: string;
  generatedAt?: string;
}

export interface Receipt {
  paymentId: number;
  paymentNumber: string;
  orderId: number;
  dealerId: number;
  farmerId: number;
  amount: number;
  paymentMethod: string;
  transactionReference: string;
  paymentDate?: string;
  generatedAt?: string;
}

export interface Auction {
  id: number;
  cropId: number;
  farmerId: number;
  basePrice: number;
  highestBid?: number;
  highestBidderId?: number;
  startTime: string;
  endTime: string;
  status: 'SCHEDULED' | 'OPEN' | 'CLOSED' | 'CANCELLED' | 'ENDED' | 'COMPLETED' | 'EXPIRED';
  crop?: Crop;
  cropName?: string;
  farmerName?: string;
  winnerName?: string;
  winningDealer?: string;
  location?: string;
  quantity?: number;
  totalBids?: number;
  orderId?: number;
  orderNumber?: string;
  paymentStatus?: string;
  deliveryStatus?: string;
  completedAt?: string;
}

export interface Bid {
  id: number;
  auctionId: number;
  dealerId: number;
  dealerName?: string;
  amount: number;
  bidTime: string;
}

export interface MarketRate {
  id?: number;
  cropName: string;
  category?: string;
  state?: string;
  district?: string;
  location?: string;
  marketPrice: number;
  maxPrice?: number;
  imageUrl?: string;
  minPrice?: number;
  modalPrice?: number;
  pricePerQuintal?: number;
  updatedAt?: string;
}

