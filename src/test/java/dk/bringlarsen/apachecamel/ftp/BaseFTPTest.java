package dk.bringlarsen.apachecamel.ftp;

import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule;
import org.junit.Assert;
import org.junit.Rule;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class BaseFTPTest {

    @Rule
    public FakeSftpServerRule sftpServer = new FakeSftpServerRule();

    void setupFtpServer() {
        try {
            sftpServer.setPort(2222);
            sftpServer.deleteAllFilesAndDirectories();
            sftpServer.addUser("user", "secret");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    void uploadFileToFTP(String fileName, String content) {
        try {
            sftpServer.putFile(fileName, content, Charset.defaultCharset());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    void waitForFileToBeProcessed(String filename) {
        try {
            for (int retries = 0; retries < 10; retries++) {
                if (!sftpServer.existsFile(filename)) {
                    Thread.sleep(1000);
                } else {
                    return;
                }
            }
            Assert.fail("Expected file to be moved to the .done directory.");
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
    }
}
