/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.id.ez.system.server.websocket;

import co.id.ez.system.core.config.Configuration;
import co.id.ez.system.core.etc.Utility;
import co.id.ez.system.server.Server;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

/**
 *
 * @author LUTFI ASB
 */
public class WebSocketServer extends Server {

    private HttpServer SERVER_CONTAINER;

    public WebSocketServer(Configuration configServer) {
        super(configServer);
        BASE_URI = "http://0.0.0.0:";
        PORT = getConfigServer().getInt("port");
        POOL_SIZE = getConfigServer().getInt("pool-size");
        MAX_POOL_SIZE = getConfigServer().getInt("max-pool-size");
        NAME = getConfigServer().getString("name");
        QUEUE = getConfigServer().getInt("queue-size");
    }

    @Override
    public void build() throws IOException, Exception {
        SERVER_CONTAINER = HttpServer.createSimpleServer(null, PORT);
        SERVER_CONTAINER.getServerConfiguration().setHttpServerName(NAME);
        SERVER_CONTAINER.getListener("grizzly").registerAddOn(new WebSocketAddOn());
        SERVER_CONTAINER.getListener("grizzly").getTransport()
                .setWorkerThreadPoolConfig(
                        ThreadPoolConfig.defaultConfig()
                                .setCorePoolSize(POOL_SIZE)
                                .setMaxPoolSize(MAX_POOL_SIZE)
                                .setQueueLimit(QUEUE)
                );
        createResource();
        SERVER_CONTAINER.start();
    }

    private void createResource() throws IOException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Utility.printMessage("Adding Resource ...");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().contains("co.id.ez")
                    && !info.getName().toLowerCase().contains("module-info")) {
                final Class<?> clazz = info.load();
                if (WebSocketApplication.class.isAssignableFrom(clazz)) {
                    WebSocketPath anot = clazz.getAnnotation(WebSocketPath.class);
                    if (anot != null) {
                        WebSocketApplication app = (WebSocketApplication) clazz.getDeclaredConstructor().newInstance();
                        String tContext = anot.context();
                        String tPath = anot.value();
                        WebSocketEngine.getEngine().register(tContext, tPath, app);
                        Utility.printMessage(" - " + clazz.getName() + ", with path: " + tContext + tPath);
                    }
                }
            }
        }

        Utility.createSeparator();
    }

    @Override
    public String getServerType() {
        return "Web Socket";
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

}
