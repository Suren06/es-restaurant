package be.cooking;

import be.cooking.generic.*;
import be.cooking.generic.messages.MessageBase;
import be.cooking.model.Order;
import be.cooking.model.Repository;
import be.cooking.model.actors.*;
import be.cooking.model.messages.*;

import java.util.Arrays;
import java.util.List;

class Context {
    final Topic topic = new Topic();
    final AlarmClock clock = new AlarmClock(topic);
    final ThreadedHandler threadedAlarmClock = createActor("AlarmClock", clock);
    final MidgetHouse midgetHouse = new MidgetHouse(topic);
    final ThreadedHandler orderPrinter = createActor("Printer", new OrderPrinter());
    final Cashier cashier = new Cashier(topic);
    final ThreadedHandler threadCashier = createActor("MoneyMan", cashier);
    final ThreadedHandler manager = createActor("Manager", new Manager(topic));

    final Cook cookKoen = new Cook(topic, "Koen", 350);
    final TTLChecker ttlCookKoen = new TTLChecker(cookKoen);
    final ThreadedHandler koen = createActor("Cook Koen", ttlCookKoen);

    final Cook cookGuido = new Cook(topic, "Guido", 200);
    final TTLChecker ttlCookGuido = new TTLChecker(cookGuido);
    final ThreadedHandler guido = createActor("Cook Guido", ttlCookGuido);

    final Cook cookGreg = new Cook(topic, "Greg", 600);
    final TTLChecker ttlCookGreg = new TTLChecker(cookGreg);
    final ThreadedHandler greg = createActor("Cook Greg", ttlCookGreg);

    final MoreFair cookers = MoreFair.newBuilder()
            .withHandler(koen)
            .withHandler(guido)
            .withHandler(greg)
            .build();

    final Repository<Order> orderRepository = new Repository<>();
    final ThreadedHandler bobTheDistributer = new ThreadedHandler("HandlerBob", cookers);

    final Waiter waiter = new Waiter(topic, orderRepository);
    final List<ThreadedHandler> threadedHandlers = Arrays.asList(orderPrinter, threadCashier, manager, bobTheDistributer, koen, greg, guido, threadedAlarmClock);
    final List<Cook> cooks = Arrays.asList(cookGreg, cookGuido, cookKoen);

    private Context() {
    }

    public static Context create() {
        final Context context = new Context();
        context.wire();
        startThreadHandlers(context);
        return context;
    }

    private static void startThreadHandlers(Context context) {
        context.threadedHandlers.forEach(ThreadedHandler::start);
        context.clock.start();

    }

    private static ThreadedHandler createActor(String name, Handler<? extends MessageBase> handler) {
        return new ThreadedHandler(name, handler);
    }

    private void wire() {
        topic.subscribe(PriceOrder.class, manager);
        topic.subscribe(ToThePayment.class, cashier);
        topic.subscribe(CookFood.class, bobTheDistributer);
        topic.subscribe(PublishAt.class, threadedAlarmClock);

        topic.subscribe(OrderPlaced.class, midgetHouse.new OrderPlaceHandler());
        topic.subscribe(WorkDone.class, midgetHouse.new WorkDoneHandler());
    }

}
