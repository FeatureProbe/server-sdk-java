import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadmeUpdater {

    private static final Path README_PATH = Paths.get("../README.md");
    private static final Path POM_PATH = Paths.get("../pom.xml");

    public static void main(String[] args) throws IOException {
        String releaseVersion = args[0];

        String newReadme = new String(Files.readAllBytes(README_PATH))
                .replaceAll("<version>.*</version>",
                        String.format("<version>%s</version>", releaseVersion))
                .replaceAll("implementation 'com\\.featureprobe:server-sdk-java:.*'",
                        String.format("implementation 'com.featureprobe:server-sdk-java:%s'", releaseVersion));

        String newPom = new String(Files.readAllBytes(POM_PATH))
                .replaceAll("<artifactId>server-sdk-java</artifactId>\n    <version>.*</version>",
                        String.format("<artifactId>server-sdk-java</artifactId>\n    <version>%s</version>", releaseVersion));

        try (FileWriter readme = new FileWriter(README_PATH.toString(), false);
             FileWriter pom = new FileWriter(POM_PATH.toString(), false)) {
            readme.write(newReadme);
            pom.write(newPom);
        } catch (IOException e) {
            throw e;
        }
    }

}
