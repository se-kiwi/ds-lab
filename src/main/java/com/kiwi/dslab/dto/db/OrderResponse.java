package com.kiwi.dslab.dto.db;

public class OrderResponse {
    private String order_id;
    private String user_id;
    private String initiator;
    private boolean success;
    private double paid;

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public void print() {
        System.out.println(order_id + ", " + user_id + ", " + initiator + ", " + success + ", " + paid);
    }
}
