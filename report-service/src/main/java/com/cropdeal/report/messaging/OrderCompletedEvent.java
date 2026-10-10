package com.cropdeal.report.messaging;
public class OrderCompletedEvent { private Long orderId,farmerId,dealerId,cropId; private String orderNumber,status,message;
 public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;} public Long getFarmerId(){return farmerId;} public void setFarmerId(Long v){farmerId=v;} public Long getDealerId(){return dealerId;} public void setDealerId(Long v){dealerId=v;} public Long getCropId(){return cropId;} public void setCropId(Long v){cropId=v;} public String getOrderNumber(){return orderNumber;} public void setOrderNumber(String v){orderNumber=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;}
}
