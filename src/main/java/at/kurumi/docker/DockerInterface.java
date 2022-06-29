package at.kurumi.docker;

import at.kurumi.docker.entities.Container;
import at.kurumi.docker.entities.ContainerParameter;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Startup;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
@Startup
public class DockerInterface {

    private static final Logger LOG = LogManager.getLogger();

    private DockerClient dockerClient;

    @PostConstruct
    protected void onConstruct() {
        LOG.info("Constructing Docker Interface");
        dockerClient = DockerClientBuilder.getInstance().build();
        LOG.info("Constructed Docker Interface");
    }

    @PreDestroy
    protected void onDestroy() {
        LOG.info("Destroying Docker Interface");
        try {
            dockerClient.close();
            LOG.info("Destroyed Docker Interface");
        } catch (IOException e) {
            LOG.error("Failed to destroy Discord interface. Resources may not have been released");
        }
    }

    public String newContainer(String resourceName, boolean start) {
        LOG.trace("Creating {} Docker container: {}", start ? "and starting" : "", resourceName);
        try (final var is = DockerInterface.class.getClassLoader().getResourceAsStream(resourceName);
             final var isr = new InputStreamReader(is);
             final var br = new BufferedReader(isr)) {
            final var container = parse(br.lines());

            if (container.isPresent()) {
                final var c = container.get();
                final var response = c.create(dockerClient);
                Arrays.stream(response.getWarnings()).forEach(LOG::warn);
                if(start) {
                    c.start(dockerClient);
                }

                return c.getName();
            }
            return "";
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("Could not find container config file");
            return "";
        } catch (IOException e) {
            LOG.error("Could not read container config file");
            return "";
        }
    }



    private Optional<Container> parse(Stream<String> content) {
        final var container = new Container();
        final var parameter = new ContainerParameter();

        content.map(line -> line.split(":", 2))
                .forEach(kv -> {
                    var v = kv[1];
                    switch(kv[0]) {
                        case "depends_on": container.addDependency(v); break;
                        case "image": parameter.setImage(v); break;
                        case "container_name": parameter.setContainerName(v); break;
                        case "port": parameter.addPortMapping(v); break;
                        case "env": parameter.addEnv(v); break;
                        case "volume": parameter.addVolumeMapping(v); break;
                        case "#": /*This line is a comment, ignore it*/ break;
                        // TODO: This should really be an error and cancel the parse process but idk how to get out of here
                        default: LOG.warn("Unknown container config key: {}", kv[0]);
                    }
                });

        if(StringUtil.isNullOrEmpty(parameter.getImage())) {
            LOG.error("Missing image name in container config");
            return Optional.empty();
        }

        if(StringUtil.isNullOrEmpty(parameter.getContainerName())) {
            LOG.error("Missing container name in container config");
            return Optional.empty();
        }

        container.setParameter(parameter);
        return Optional.of(container);
    }

}
