package be.cooking.model.actors;

import be.cooking.generic.messages.MessageBase;
import be.cooking.model.Order;
import be.cooking.model.messages.*;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class PayFirstStrategy extends AbstractStrategy {


    public List<MessageBase> handleEvent(MessageBase m) {
        if (m instanceof OrderPlaced) {
            final OrderPlaced orderPlaced = (OrderPlaced) m;
            return asList(createToThePricing(orderPlaced), createPublishAt(orderPlaced));
        } else if (m instanceof OrderPriced) {
            final OrderPriced orderPriced = (OrderPriced) m;
            return singletonList(createToThePayment(orderPriced));
        } else if (m instanceof OrderPaid) {
            final OrderPaid orderPriced = (OrderPaid) m;
            return handleOrderPaid(createCookFood(orderPriced));
        } else if (m instanceof CookingTimedOut) {
            return handleCookingTimeOutEvent((CookingTimedOut) m);
        } else if (m instanceof OrderCooked) {
            return handleOrderCooked((OrderCooked) m);
        }
        return Collections.emptyList();

    }

    private List<MessageBase> handleOrderPaid(CookFood cookFood) {
        return singletonList(cookFood);
    }

    private List<MessageBase> handleOrderCooked(OrderCooked orderCooked) {
        setOrderToFinished(orderCooked);
        return singletonList(createWorkDone(orderCooked));
    }


    private List<MessageBase> handleCookingTimeOutEvent(CookingTimedOut cookingTimedOut) {
        final Order order = cookingTimedOut.getOrder();
        if (order.getStatus() != Order.Status.COOKED) {
            System.out.println("Order " + order.getOrderUUID() + " already cooked " + order.getStatus());
            return Collections.emptyList();
        } else {
            System.out.println("Order " + order.getOrderUUID() + " not cooked on time. Retry");
            order.increaseTries();
            if (order.getNrOfTries() > ORDER_RETRIES) {
                System.out.println("To many retries " + order.getOrderUUID());
                return handleToManyRetries(cookingTimedOut);
            } else
                return singletonList(createCookFood(cookingTimedOut));
        }
    }


}
