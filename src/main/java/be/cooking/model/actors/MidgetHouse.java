package be.cooking.model.actors;

import be.cooking.generic.Handler;
import be.cooking.generic.ThreadedHandler;
import be.cooking.generic.Topic;
import be.cooking.generic.messages.MessageBase;
import be.cooking.model.messages.OrderPlaced;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MidgetHouse {

    private final Map<UUID, Midget> midgets = new HashMap<>();
    private final Topic topic;


    public MidgetHouse(Topic topic) {
        this.topic = topic;
    }

    private void checkOrderShouldNotAlreadyExists(OrderPlaced orderPlaced) {
        if (midgets.containsKey(orderPlaced.getCorrelationUUID()))
            throw new RuntimeException("CorrelationUUid already exists");
    }

    private void checkMidgetExists(Midget midget) {
        if (!midgets.containsKey(midget.getCorrelationId()))
            throw new RuntimeException("Cannot clean midget because midget is not found");
    }

    public class OrderPlaceHandler implements Handler<OrderPlaced> {
        @Override
        public void handle(OrderPlaced orderPlaced) {
            checkOrderShouldNotAlreadyExists(orderPlaced);
            final Midget midget = new Midget(topic, orderPlaced.getCorrelationUUID(), this::removeMidgetByCorrelationId);
            midgets.put(orderPlaced.getCorrelationUUID(), midget);
            final ThreadedHandler<MessageBase> messageBaseThreadedHandler = new ThreadedHandler<>("MidgetHouse", new MessageBaseHanlder());
            topic.subscribe(orderPlaced.getCorrelationUUID(), messageBaseThreadedHandler);
            messageBaseThreadedHandler.start();
        }

        public void removeMidgetByCorrelationId(Midget midget) {
            checkMidgetExists(midget);
            midgets.remove(midget.getCorrelationId());
        }
    }


    public class MessageBaseHanlder implements Handler<MessageBase> {
        @Override
        public void handle(MessageBase value) {
            final Midget midget = midgets.get(value.getCorrelationUUID());
            if (midget != null) {
                midget.handle(value);
            }
        }
    }
}
