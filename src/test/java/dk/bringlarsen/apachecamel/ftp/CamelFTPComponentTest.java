package dk.bringlarsen.apachecamel.ftp;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CamelFTPComponentTest extends BaseCamelFTPComponentTest {

    @Before
    public void setup() {
        setupFtpServer();
    }

    @Test
    public void testFileContentIsRead() throws Exception {
        final AtomicReference<String> response = new AtomicReference<>();
        final AtomicReference<String> filename = new AtomicReference<>();
        addRouteAndStartContext(new RouteBuilder() {
            @Override
            public void configure() {
                from(String.format("sftp://user@localhost:%s/out?password=secret&localWorkDirectory=target/workdir&move=.done", sftpServer.getPort()))
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.DEBUG, "Got: ${file:name} with content: ${body}")
                        .process(e -> filename.set(e.getIn().getHeader("CamelFilename", String.class)))
                        .process(e -> response.set(e.getIn().getBody(String.class)));
            }
        });

        uploadFileToFTP("/out/somefile.csv", "1;2;3");
        waitForDoneFile("/out/.done/somefile.csv");

        assertThat(filename.get(), is("somefile.csv"));
        assertThat(response.get(), is("1;2;3"));
    }
}