package be.cooking.model;

import be.cooking.Repository;
import be.cooking.model.messages.OrderPlaced;

import java.util.Random;
import java.util.UUID;

public class Waiter {

    private static final Random RANDOM = new Random();
    private Repository<Order> orderRepository;
    private final Publisher publisher;

    public Waiter(Publisher publisher, Repository<Order> orderRepository) {
        this.publisher = publisher;
        this.orderRepository = orderRepository;
    }

    public UUID takeOrder(int tableNumber) {
        final Order order = buildRandomOrder(tableNumber);
        System.out.println("Taking Order.." + order);
        orderRepository.save(order);

        publisher.publish(new OrderPlaced(order));

        return order.getOrderUUID();
    }

    private static Order buildRandomOrder(int tableNumber) {
        final int orderType = RANDOM.nextInt(5);
        switch (orderType) {
            case 0:
                return buildFrietOrder(tableNumber);
            case 1:
                return buildSteakOrder(tableNumber);
            case 2:
                return buildSoepOrder(tableNumber);
            case 3:
                return buildFrietOrder(tableNumber);
            case 4:
                return buildSpaghettiOrder(tableNumber);
            default:
                throw new RuntimeException("Unmatch orderType " + orderType);
        }
    }

    private static Order buildFrietOrder(int tableNumber) {
        return Order.newBuilder()
                .withTableNumber(tableNumber)
                .addItem(ItemCode.JUPILER)
                .addItem(ItemCode.FRIETEN)
                .addItem(ItemCode.BITTER_BALLEN)
                .addTimeToLive(5000)
                .build();
    }

    private static Order buildSteakOrder(int tableNumber) {
        return Order.newBuilder()
                .withTableNumber(tableNumber)
                .addItem(ItemCode.WINE)
                .addItem(ItemCode.STEAK)
                .addTimeToLive(4500)
                .build();
    }

    private static Order buildSoepOrder(int tableNumber) {
        return Order.newBuilder()
                .withTableNumber(tableNumber)
                .addItem(ItemCode.SOEP)
                .addTimeToLive(5500)
                .build();
    }

    private static Order buildSpaghettiOrder(int tableNumber) {
        return Order.newBuilder()
                .withTableNumber(tableNumber)
                .addItem(ItemCode.SPAGHETTI)
                .addTimeToLive(5000)
                .build();
    }
}
