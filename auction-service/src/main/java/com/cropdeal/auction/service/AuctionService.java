package com.cropdeal.auction.service;

import java.util.List;

import com.cropdeal.auction.dto.AuctionResponse;
import com.cropdeal.auction.dto.BidResponse;
import com.cropdeal.auction.dto.CreateAuctionRequest;
import com.cropdeal.auction.dto.PlaceBidRequest;

public interface AuctionService {

	AuctionResponse createAuction(CreateAuctionRequest request);

	BidResponse placeBid(Long auctionId, PlaceBidRequest request);

	AuctionResponse getAuctionById(Long auctionId);

	List<AuctionResponse> getOpenAuctions();

	List<AuctionResponse> getAuctionHistory();

	List<AuctionResponse> getAuctionsByFarmer(Long farmerId);

	List<AuctionResponse> getAuctionsByDealer(Long dealerId);

	List<AuctionResponse> getAllAuctions();

	AuctionResponse completeAuction(Long auctionId);

	AuctionResponse linkOrder(Long auctionId, Long orderId, String orderNumber);

	void synchronizeAuctionStates();

	List<BidResponse> getBidsByAuction(Long auctionId);
}