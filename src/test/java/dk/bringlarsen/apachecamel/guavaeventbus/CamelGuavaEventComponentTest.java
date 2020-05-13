package dk.bringlarsen.apachecamel.guavaeventbus;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.guava.eventbus.GuavaEventBusComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.mockito.Mockito;

public class CamelGuavaEventComponentTest extends CamelTestSupport {
    private EventBus fileReceivedEventBus = Mockito.mock(EventBus.class);

    @Override
    protected RoutesBuilder createRouteBuilder() {
        GuavaEventBusComponent busComponent = new GuavaEventBusComponent();
        busComponent.setEventBus(fileReceivedEventBus);
        context().addComponent("subscriber", busComponent);

        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:input")
                        .to("subscriber:eventBus");
            }
        };
    }

    @Test
    public void testEventIsFired() {
        fileReceivedEventBus.register(new Subscriber());

        template.sendBody("direct:input", "Testing!");

        Mockito.verify(fileReceivedEventBus).post("Testing!");
    }


    public class Subscriber {

        @Subscribe
        public void event(String eventBody) {
            System.out.println(eventBody);
        }
    }
}