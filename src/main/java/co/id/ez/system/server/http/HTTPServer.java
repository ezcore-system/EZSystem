/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.id.ez.system.server.http;

import co.id.ez.system.server.*;
import co.id.ez.system.core.config.Configuration;
import co.id.ez.system.core.etc.Utility;
import com.google.common.reflect.ClassPath;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Lutfi
 */
public class HTTPServer extends Server {

    private HttpServer SERVER_CONTAINER;

    public HTTPServer(Configuration configServer) {
        super(configServer);
        BASE_URI = "http://0.0.0.0:";
        PORT = getConfigServer().getInt("port");
        POOL_SIZE = getConfigServer().getInt("pool-size");
        MAX_POOL_SIZE = getConfigServer().getInt("max-pool-size");
        NAME = getConfigServer().getString("name");
        QUEUE = getConfigServer().getInt("queue-size");
    }

    private ResourceConfig createResource() throws IOException {
        Utility.printMessage("Adding Resource ...");
        ResourceConfig resource = new ResourceConfig();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().contains("co.id.ez") 
                    && !info.getName().toLowerCase().contains("module-info")) {
                final Class<?> clazz = info.load();
                Annotation anot = clazz.getAnnotation(Path.class);
                if (anot != null) {
                    Utility.printMessage(" - " + clazz.getName());
                    resource.register(clazz);
                }
            }
        }

        resource.register(ExceptionHTTPHandler.class);
        resource.register(ThrowableHTTPHandler.class);
        Utility.createSeparator();
        return resource;
    }

    @Override
    public void stopServer() {
        if (SERVER_CONTAINER != null) {
            try {
                Utility.printMessage("Shutting down the " + getServerType() + " Server ...");
                SERVER_CONTAINER.shutdownNow();
            } catch (Exception e) {
                Utility.printError(e, "Failed shuting down the service");
            }
        }
    }

    @Override
    public void build() throws IOException, Exception {
        ResourceConfig config = createResource();

        SERVER_CONTAINER = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI + PORT), config);
        SERVER_CONTAINER.getServerConfiguration().setHttpServerName(NAME);
        final TCPNIOTransport transport = SERVER_CONTAINER.getListener("grizzly").getTransport();
        transport.setWorkerThreadPoolConfig(
                ThreadPoolConfig.defaultConfig()
                        .setCorePoolSize(POOL_SIZE)
                        .setMaxPoolSize(MAX_POOL_SIZE)
                        .setQueueLimit(QUEUE)
        );

        addShutdownHook();
        SERVER_CONTAINER.start();
    }

    @Override
    public String getServerType() {
        return "HTTP";
    }

    @Override
    public void addShutdownHook() {
        
    }

    @Override
    public String getServerDetail() {
        return ("\n - Server Name: " + SERVER_CONTAINER.getServerConfiguration().getHttpServerName())
                .concat("\n - Port: " + PORT)
                .concat("\n - Pool Size: " + POOL_SIZE)
                .concat("\n - Max Pool Size: " + MAX_POOL_SIZE);
    }
}
