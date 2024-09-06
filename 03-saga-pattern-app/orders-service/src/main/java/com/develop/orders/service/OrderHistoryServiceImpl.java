package com.develop.orders.service;

import com.develop.core.types.OrderStatus;
import com.develop.orders.dao.jpa.entity.OrderHistoryEntity;
import com.develop.orders.dao.jpa.repository.OrderHistoryRepository;
import com.develop.orders.dto.OrderHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {
    private final OrderHistoryRepository orderHistoryRepository;

    @Override
    public void add(UUID orderId, OrderStatus orderStatus) {
        OrderHistoryEntity entity = new OrderHistoryEntity();
        entity.setOrderId(orderId);
        entity.setStatus(orderStatus);
        entity.setCreatedAt(new Timestamp(new Date().getTime()));
        orderHistoryRepository.save(entity);
    }

    @Override
    public List<OrderHistory> findByOrderId(UUID orderId) {
        List<OrderHistoryEntity> entities = orderHistoryRepository.findByOrderId(orderId);
        return entities.stream().map(entity -> {
            OrderHistory orderHistory = new OrderHistory();
            BeanUtils.copyProperties(entity, orderHistory);
            return orderHistory;
        }).toList();
    }
}