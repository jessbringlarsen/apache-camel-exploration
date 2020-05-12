package dk.bringlarsen.apachecamel.ftp;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CamelFTPComponentTest extends BaseFTPTest {

    private FTPApplication application;

    @Before
    public void setup() {
        setupFtpServer();
        application = new FTPApplication("/out", sftpServer.getPort());
        application.startup();

    }

    @Test
    public void testFileContentIsRead() {
        application.addSubscriber(fileContent -> {
            Assert.assertThat(fileContent, CoreMatchers.is("1;2;3"));
            return null;
        });
        uploadFileToFTP("/out/somefile.csv", "1;2;3");

        waitForFileToBeProcessed("/out/.done/somefile.csv");
    }

    @After
    public void teardown() {
        application.shutdown();
    }
}