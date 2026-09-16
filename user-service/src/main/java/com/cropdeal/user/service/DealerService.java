package com.cropdeal.user.service;

import com.cropdeal.user.dto.request.DealerRequest;
import com.cropdeal.user.dto.response.DealerResponse;

public interface DealerService {

    DealerResponse createDealer(DealerRequest request);

    DealerResponse getDealerById(Long dealerId);

    DealerResponse getDealerByAuthUserId(Long authUserId);

    DealerResponse updateDealer(Long dealerId, DealerRequest request);

    DealerResponse activateDealer(Long dealerId);

    DealerResponse deactivateDealer(Long dealerId);

    void deleteDealer(Long dealerId);
}