package dk.bringlarsen.apachecamel;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class CamelFTPTest extends BaseFTPTest {

    Application application;
    List<String> responseList = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        setupFtpServer();
        application = new Application("/out", sftpServer.getPort(), responseList);
        application.startup();
    }

    @Test
    public void testFileContentIsRead() throws Exception {
        putFile("/out/somefile.csv", "1;2;3");

        waitForDoneFile("/out/.done/somefile.csv");

        Assert.assertThat(responseList.size(), CoreMatchers.is(1));
        Assert.assertThat(responseList.get(0), CoreMatchers.is("1;2;3"));
    }

    @After
    public void teardown() {
        application.shutdown();
    }
}