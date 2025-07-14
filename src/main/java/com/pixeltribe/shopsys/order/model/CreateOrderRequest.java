package com.pixeltribe.shopsys.order.model;

import java.util.List;
import com.pixeltribe.shopsys.orderItem.model.CreateOrderItemRequest;

// ***** 建立訂單請求 DTO *****//
public class CreateOrderRequest {
    private String contactEmail;
    private String contactPhone;
    private List<CreateOrderItemRequest> orderItems;

    // 無參構造函數
    public CreateOrderRequest() {}

    // 有參構造函數
    public CreateOrderRequest(String contactEmail, String contactPhone, List<CreateOrderItemRequest> orderItems) {
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.orderItems = orderItems;
    }

    // getters and setters
    public String getContactEmail() { 
        return contactEmail; 
    }

    public void setContactEmail(String contactEmail) { 
        this.contactEmail = contactEmail; 
    }

    public String getContactPhone() { 
        return contactPhone; 
    }

    public void setContactPhone(String contactPhone) { 
        this.contactPhone = contactPhone; 
    }

    public List<CreateOrderItemRequest> getOrderItems() { 
        return orderItems; 
    }

    public void setOrderItems(List<CreateOrderItemRequest> orderItems) { 
        this.orderItems = orderItems; 
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "contactEmail='" + contactEmail + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }
}