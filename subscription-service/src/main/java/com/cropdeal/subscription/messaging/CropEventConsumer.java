package com.cropdeal.subscription.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.repository.CropSubscriptionRepository;

import java.util.List;

@Service
public class CropEventConsumer {

	private final CropSubscriptionRepository subscriptionRepository;
	private final CropMatchEventProducer cropMatchEventProducer;

	public CropEventConsumer(CropSubscriptionRepository subscriptionRepository,
			CropMatchEventProducer cropMatchEventProducer) {

		this.subscriptionRepository = subscriptionRepository;
		this.cropMatchEventProducer = cropMatchEventProducer;
	}

	@RabbitListener(queues = RabbitConfig.CROP_QUEUE)
	public void receiveCropPublished(CropPublishedEvent event) {

		List<CropSubscription> subscriptions = subscriptionRepository.findByActiveTrue();
		java.util.Set<Long> notifiedDealerIds = new java.util.HashSet<>();

		for (CropSubscription subscription : subscriptions) {

			boolean cropNameMatch = subscription.getCropName() == null || subscription.getCropName().isBlank()
					|| (event.getCropName() != null
							&& subscription.getCropName().trim().equalsIgnoreCase(event.getCropName().trim()));

			boolean categoryMatch = subscription.getCategory() == null || subscription.getCategory().isBlank()
					|| (event.getCategory() != null
							&& subscription.getCategory().trim().equalsIgnoreCase(event.getCategory().trim()));

			boolean locationMatch = subscription.getLocation() == null || subscription.getLocation().isBlank()
					|| (event.getLocation() != null
							&& subscription.getLocation().trim().equalsIgnoreCase(event.getLocation().trim()));

			boolean quantityMatch = subscription.getMinimumQuantity() == null
					|| (event.getQuantity() != null && event.getQuantity() <= subscription.getMinimumQuantity());

			boolean priceMatch = subscription.getMaximumPrice() == null || subscription.getMaximumPrice() <= 0
					|| (event.getExpectedPrice() != null && event.getExpectedPrice() <= subscription.getMaximumPrice());

			if (cropNameMatch && categoryMatch && locationMatch && quantityMatch && priceMatch) {

				if (subscription.getDealerId() != null && notifiedDealerIds.add(subscription.getDealerId())) {
					CropMatchFoundEvent matchEvent = new CropMatchFoundEvent();

					matchEvent.setDealerId(subscription.getDealerId());
					matchEvent.setCropId(event.getCropId());
					matchEvent.setCropName(event.getCropName());
					String loc = event.getLocation() != null && !event.getLocation().isBlank()
							? " in " + event.getLocation()
							: "";
					String qty = event.getQuantity() != null ? " (" + event.getQuantity() + " units)" : "";
					matchEvent.setMessage(
							"New " + event.getCropName() + " crop" + qty + " available from a farmer" + loc + ".");

					cropMatchEventProducer.publishCropMatch(matchEvent);
				}
			}
		}
	}
}