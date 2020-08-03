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

        final AtomicReference<Callback> response = new AtomicReference<>();
        addRouteAndStartContext(new RouteBuilder() {
            @Override
            public void configure() {
                from(String.format("sftp://user@localhost:%s/out?password=secret&localWorkDirectory=target/workdir&move=.done&readLock=changed&fastExistsCheck=true", sftpServer.getPort()))
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, "Got: ${file:name} with content: ${body}")
                        .process(e -> response.set(new Callback(
                                e.getIn().getHeader("CamelFilename", String.class),
                                e.getIn().getBody(String.class))));
            }
        });

        uploadFileToFTP("/out/somefile.csv", "1;2;3");
        waitForDoneFile("/out/.done/somefile.csv");

        assertThat(response.get().getFileName(), is("somefile.csv"));
        assertThat(response.get().getContent(), is("1;2;3"));
    }

    private class Callback {
        private String fileName;
        private String content;

        public Callback(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContent() {
            return content;
        }
    }
}