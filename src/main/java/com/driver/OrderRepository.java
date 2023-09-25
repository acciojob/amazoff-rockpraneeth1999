package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class OrderRepository {

    HashMap<String,Order> orderList = new HashMap<>();

    HashMap<String,Order> assignedOrders = new HashMap<>();

    HashMap<String,Order> unassignedOrders = new HashMap<>();

    HashMap<String,DeliveryPartner> partnerList = new HashMap<>();

    HashMap<String, List<String>> partner_order = new HashMap<>();


    public void addOrder(Order order) {
        unassignedOrders.put(order.getId(),order);
        orderList.put(order.getId(),order);
    }

    public void addPartner(String partnerId) {
        partnerList.put(partnerId,new DeliveryPartner(partnerId));
    }

    public void addOrderPartnerPair(String orderId, String partnerId) {
        List<String> orders = partner_order.get(partnerId);
        int numOfOrdersOfPartner = partnerList.get(partnerId).getNumberOfOrders();

        if(numOfOrdersOfPartner==0){
            orders=new ArrayList<>();
            orders.add(orderId);
            partner_order.put(partnerId,orders);

            DeliveryPartner deliveryPartner = partnerList.get(partnerId);
            deliveryPartner.setNumberOfOrders(1);

            partnerList.put(partnerId,deliveryPartner);
        }

        orders.add(orderId);
        partner_order.put(partnerId,orders);

        DeliveryPartner deliveryPartner = partnerList.get(partnerId);
        deliveryPartner.setNumberOfOrders(numOfOrdersOfPartner+1);
        partnerList.put(partnerId,deliveryPartner);

        Order order = unassignedOrders.get(orderId);
        unassignedOrders.remove(orderId);

        assignedOrders.put(orderId,order);
    }

    public Order getOrderById(String orderId) {
        if(assignedOrders.containsKey(orderId))
            return assignedOrders.get(orderId);
        if (unassignedOrders.containsKey(orderId)){
            return unassignedOrders.get(orderId);
        }
        return null;
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        if (partnerList.containsKey(partnerId))
            return partnerList.get(partnerId);
        return null;
    }

    public Integer getOrderCountByPartnerId(String partnerId) {
        return partnerList.get(partnerId).getNumberOfOrders();
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        return partner_order.get(partnerId);
    }

    public List<String> getAllOrders() {
        List<String> orders = new ArrayList<>();
        for (String order : orderList.keySet()){
            orders.add(order);
        }
        return orders;
    }

    public Integer getCountOfUnassignedOrders() {
        return unassignedOrders.size();
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId) {
        int lastTime = Integer.parseInt(time.substring(0,2)) * 60 + Integer.parseInt(time.substring(3));

        List<String> partnerOrders = partner_order.get(partnerId);
        List<String> remainingOrders = new ArrayList<>();

        for (String orderId : partnerOrders){
            Order order = assignedOrders.get(orderId);
            int deliveryTime = order.getDeliveryTime();

            if(deliveryTime<=lastTime){
                orderList.remove(orderId);
                assignedOrders.remove(orderId);
            }
            else{
                remainingOrders.add(orderId);
            }
        }

        partner_order.put(partnerId,remainingOrders);

        return remainingOrders.size();
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId) {
        List<String> orders = partner_order.get(partnerId);
        int lastTime = Integer.MIN_VALUE;

        for (String orderId : orders){
            Order order = assignedOrders.get(orderId);
            int deliveryTime = order.getDeliveryTime();

            lastTime=Math.max(lastTime,deliveryTime);
        }

        int hours = lastTime/60;
        int minutes= lastTime%60;

        String time = hours+":"+minutes;

        return time;
    }

    public void deletePartnerById(String partnerId) {
        List<String> orders = partner_order.get(partnerId);
        for (String orderId : orders){
            Order order = assignedOrders.get(orderId);
            assignedOrders.remove(orderId);
            unassignedOrders.put(orderId,order);
        }

        partner_order.remove(partnerId);
    }

    public void deleteOrderById(String orderId) {
        orderList.remove(orderId);
        assignedOrders.remove(orderId);

        for (String partnerId : partner_order.keySet()){
            //Iterate all order of partner
            List<String> orders = partner_order.get(partnerId);
            //search and delete the orderId
            for (String orderKey : orders){
                if(orderKey.equals(orderId)){
                    orders.remove(orderId);
                    partner_order.put(partnerId,orders);
                    return;
                }
            }
        }
    }
}
