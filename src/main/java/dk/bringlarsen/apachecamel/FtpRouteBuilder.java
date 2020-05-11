package dk.bringlarsen.apachecamel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FtpRouteBuilder extends RouteBuilder {

    private String directoryToWatch;
    private int ftpServerPort;
    private List<String> responseList;

    public FtpRouteBuilder(String directoryToWatch, int ftpServerPort, List<String> responseList) {
        this.directoryToWatch = directoryToWatch;
        this.ftpServerPort = ftpServerPort;
        this.responseList = responseList;
    }

    @Override
    public void configure() throws Exception {
        from("sftp://user@localhost:" + ftpServerPort + "/" + directoryToWatch + "?password=secret&localWorkDirectory=/tmp/workdir&move=.done")
                .log("Downloaded file ${file:name} complete.")
                .convertBodyTo(File.class)
                .process(this::process);
    }

    private void process(Exchange exchange) throws IOException {
        File file = (File)exchange.getIn().getBody();
        responseList.add(Files.readString(Paths.get(file.getPath())));
    }
}
