package dk.bringlarsen.apachecamel.ftp;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class CamelFTPComponentTest extends BaseCamelFTPComponentTest {

    @Before
    public void setup() {
        setupFtpServer();
    }

    @Test
    public void testFileContentIsRead() throws Exception {
        final AtomicReference<String> response = new AtomicReference<>();
        addRouteAndStartContext(new RouteBuilder() {
            @Override
            public void configure() {
                from("sftp://user@localhost:" + sftpServer.getPort() + "/out?password=secret&localWorkDirectory=/tmp/workdir&move=.done")
                        .convertBodyTo(File.class)
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, "Got: ${file:name} with content: ${body}")
                        .process(e -> response.set((String) e.getIn().getBody()));
            }
        });

        uploadFileToFTP("/out/somefile.csv", "1;2;3");
        waitForDoneFile("/out/.done/somefile.csv");

        Assert.assertThat(response.get(), CoreMatchers.is("1;2;3"));
    }
}