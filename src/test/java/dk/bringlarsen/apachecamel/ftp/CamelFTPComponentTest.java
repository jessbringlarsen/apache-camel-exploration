package dk.bringlarsen.apachecamel.ftp;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CamelFTPComponentTest extends BaseCamelFTPComponentTest {

    @Before
    public void setup() {
        setupFtpServer();
    }

    @Test
    public void testFileContentIsRead() {
        final AtomicReference<Pair<String>> result = new AtomicReference<>(new Pair<>("", ""));
        addRouteAndStartContext(new RouteBuilder() {
            @Override
            public void configure() {
                from(String.format("sftp://user@localhost:%s/out?password=secret&noop=true", sftpServer.getPort()))
                    .process(e -> result.set(new Pair<>(e.getIn().getHeader("CamelFileName", String.class), e.getIn().getBody(String.class))));
            }
        });
        uploadFileToFTP("/out/somefile.csv", "1;2;3");

        boolean gotFile = waitForCondition(() -> "somefile.csv".equals(result.get().getLeft()));

        assertThat("The expected file was not downloaded!", gotFile, is(true));
        assertThat(result.get().getLeft(), is("somefile.csv"));
        assertThat(result.get().getRight(), is("1;2;3"));
    }

    /**
     * Specify endEmptyMessageWhenIdle=true to enable sending idle messages when all files are consumed.
     */
    @Test
    public void testContextIsShutdownWhenIdle() {
        String routeId = "route-1";
        addRouteAndStartContext(new RouteBuilder() {
            @Override
            public void configure() {
                from(String.format("sftp://user@localhost:%s/out?password=secret&noop=true&sendEmptyMessageWhenIdle=true", sftpServer.getPort()))
                    .routeId(routeId)
                    .choice()
                        .when(body().isNull())
                            .log("Done consuming files, shutting down")
                            .toF("controlbus:route?routeId=%s&action=stop&async=true", routeId)
                    .otherwise()
                        .log(LoggingLevel.INFO, "${body}");
            }
        });
        uploadFileToFTP("/out/somefile.csv", "1;2;3");

        boolean result = waitForCondition(() -> getCamelRouteController().getRouteStatus(routeId).isStopped());

        assertThat("Expected Camel context to be shutdown!", result, is(true));
    }

    private boolean waitForCondition(Supplier<Boolean> condition) {
        try {
            for (int retries = 0; retries < 10; retries++) {
                boolean hasResult = condition.get();
                if(hasResult) { return true; }
                    Thread.sleep(1000);
            }
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}