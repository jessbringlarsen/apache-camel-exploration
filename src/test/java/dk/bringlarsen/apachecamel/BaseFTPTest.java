package dk.bringlarsen.apachecamel;

import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule;
import org.junit.Assert;
import org.junit.Rule;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class BaseFTPTest {


    @Rule
    public FakeSftpServerRule sftpServer = new FakeSftpServerRule();

    void setupFtpServer() throws IOException {
        sftpServer.setPort(2222);
        sftpServer.deleteAllFilesAndDirectories();
        sftpServer.addUser("user", "secret");
    }

    void putFile(String fileName, String content) {
        try {
            sftpServer.putFile(fileName, content, Charset.defaultCharset());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    void waitForDoneFile(String filename) throws Exception {
        for (int retries = 0; retries < 10; retries++) {
            if(!sftpServer.existsFile(filename)) {
                Thread.sleep(1000);
            } else {
                return;
            }
        }
        Assert.fail("Expected file to be moved to the .done directory.");
    }
}
