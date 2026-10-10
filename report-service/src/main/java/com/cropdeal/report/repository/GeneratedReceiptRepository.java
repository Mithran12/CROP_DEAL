package com.cropdeal.report.repository;
import com.cropdeal.report.entity.GeneratedReceipt; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface GeneratedReceiptRepository extends JpaRepository<GeneratedReceipt,Long>{Optional<GeneratedReceipt> findByOrderId(Long orderId);}
