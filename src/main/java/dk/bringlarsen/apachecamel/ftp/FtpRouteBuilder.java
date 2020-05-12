package dk.bringlarsen.apachecamel.ftp;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

public class FtpRouteBuilder extends RouteBuilder {

    private String directoryToWatch;
    private int ftpServerPort;
    private List<Function<String, Void>> subscribers;

    public FtpRouteBuilder(String directoryToWatch, int ftpServerPort, List<Function<String, Void>> subscribers) {
        this.directoryToWatch = directoryToWatch;
        this.ftpServerPort = ftpServerPort;
        this.subscribers = subscribers;
    }

    @Override
    public void configure() {
        from("sftp://user@localhost:" + ftpServerPort + "/" + directoryToWatch + "?password=secret&localWorkDirectory=/tmp/workdir&move=.done")
                .log("Downloaded file ${file:name} complete.")
                .convertBodyTo(File.class)
                .process(this::process);
    }

    private void process(Exchange exchange) throws IOException {
        File file = (File)exchange.getIn().getBody();
        String fileContent = Files.readString(Paths.get(file.getPath()));
        notifySubscribers(fileContent);
    }

    private void notifySubscribers(String fileContent) {
        subscribers.forEach(subscriber -> subscriber.apply(fileContent));
    }
}
