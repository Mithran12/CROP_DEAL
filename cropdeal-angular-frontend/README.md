# CropDeal Angular Frontend

A production-style Angular frontend wired to the supplied CropDeal microservices through the API Gateway at `http://localhost:8080`.

## What is included
- Responsive real-time marketplace-style UI
- Login / registration with Farmer, Dealer and Admin roles
- Dummy Facebook login using the backend's supplied demo account IDs
- Farmer: profile, publish/edit/delete crops, marketplace, orders, delivery confirmation
- Dealer: marketplace, price negotiation, orders and card/UPI payment flow
- Admin: user management and active/inactive controls
- JWT token interceptor and route guard
- Toast/error feedback for backend failures
- Direct API calls to the existing gateway endpoints; no mock business data is used

## Run
1. Start the backend services and API Gateway first.
2. Make sure API Gateway is running on `http://localhost:8080`.
3. From this folder:
   ```
   npm install
   ng serve
   ```
4. Open `http://localhost:4200`.

## Backend requirement
Use the supplied `CropDeal-Final-Backend-Final-CORS.zip` so the API Gateway accepts browser requests from Angular on port 4200.

## Important test flow
1. Register/login as FARMER.
2. Complete profile.
3. Publish a crop.
4. Logout and login as DEALER.
5. Open Marketplace and start negotiation.
6. Accept/complete the order from the appropriate role.
7. Dealer creates/processes/succeeds payment.
8. Farmer confirms delivery using the generated delivery token.

The frontend deliberately uses the actual REST routes present in the supplied backend rather than inventing a separate API contract.
