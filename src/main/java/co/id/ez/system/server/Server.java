/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.id.ez.system.server;

import co.id.ez.database.DBService;
import co.id.ez.system.core.config.ConfigService;
import co.id.ez.system.core.etc.Utility;
import co.id.ez.system.service.FutureService;
import com.google.common.reflect.ClassPath;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Lutfi
 */
public class Server {

    private static final String BASE_URI = "http://0.0.0.0:";
    private static String NAME;
    private static int PORT, POOL_SIZE, MAX_POOL_SIZE, QUEUE;
    private static HttpServer mServerContainer;
    private static final List<FutureService> futureService = new ArrayList<>();

    public static void createHttpServer(long startTime) throws IOException, Exception {
        
        PORT = ConfigService.getInstance().getInt("ezsystem.connection.server.port");
        POOL_SIZE = ConfigService.getInstance().getInt("ezsystem.connection.server.pool-size");
        MAX_POOL_SIZE = ConfigService.getInstance().getInt("ezsystem.connection.server.max-pool-size");
        NAME = ConfigService.getInstance().getString("ezsystem.connection.server.name");
        QUEUE = ConfigService.getInstance().getInt("ezsystem.connection.server.queue-size");
        
        ResourceConfig config = createResource();
        
        mServerContainer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI + PORT), config);
        mServerContainer.getServerConfiguration().setHttpServerName(NAME);
        final TCPNIOTransport transport = mServerContainer.getListener("grizzly").getTransport();
        transport.setWorkerThreadPoolConfig(
                ThreadPoolConfig.defaultConfig()
                        .setCorePoolSize(POOL_SIZE)
                        .setMaxPoolSize(MAX_POOL_SIZE)
                        .setQueueLimit(QUEUE)
        );
        
        loadFutureService();
        loadDB();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Utility.printMessage("Shutting down the service...");
                mServerContainer.shutdownNow();
                ConfigService.getInstance().clear();
                DBService.stop();
                stopingFutureService();
                Utility.printMessage("Done, exit.");
            } catch (IllegalAccessException | InstantiationException e) {
                Utility.printError(e, "[IllegalAccessException | InstantiationException] Failed shuting down the service");
            } catch (Exception e) {
                Utility.printError(e, "[Exception] Failed shuting down the service");
            } finally{
                mServerContainer.shutdownNow();
            }
        }));

        mServerContainer.start();
        createServerDetail();
        startingFutureService();
        
        Utility.printMessage("*** START SUCCESS ***");
        long elapsedTime = System.currentTimeMillis() - startTime;
        Utility.printMessage("\nApplication started on " + elapsedTime + " ms at " + Calendar.getInstance().getTime().toString());
    }

    private static ResourceConfig createResource() throws IOException {
        Utility.printMessage("Adding Resource ...");
        ResourceConfig resource = new ResourceConfig();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().startsWith("co.id.ez")) {
                final Class<?> clazz = info.load();
                Annotation anot = clazz.getAnnotation(Path.class);
                if (anot != null) {
                    Utility.printMessage(" - " + clazz.getName());
                    resource.register(clazz);
                }
            }
        }

        resource.register(ExceptionHandler.class);
        resource.register(ThrowableHandler.class);
        Utility.createSeparator();
        return resource;
    }
    
    private static void loadFutureService() throws IOException, InstantiationException, IllegalAccessException {
        Utility.printMessage("Loading Future Service ...");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().startsWith("co.id.ez")) {
                final Class clazz = info.load();
                if (clazz.getSuperclass() == FutureService.class) {
                    Utility.printMessage(" - " + clazz.getName());
                    futureService.add((FutureService) clazz.newInstance());
                }
            }
        }

        Utility.createSeparator();
    }
    
    private static void startingFutureService() throws InstantiationException, IllegalAccessException{
        futureService.forEach(class1 -> {
            class1.start();
        });
    }
    
    private static void stopingFutureService() throws InstantiationException, IllegalAccessException{
        futureService.forEach(class1 -> {
            class1.stop();
        });
    }
    
    private static void createServerDetail(){
        Utility.printMessage("Server propertes");
        Utility.printMessage(" - Server Name: " + mServerContainer.getServerConfiguration().getHttpServerName());
        Utility.printMessage(" - Port: " + PORT);
        Utility.printMessage(" - Pool Size: " + POOL_SIZE);
        Utility.printMessage(" - Max Pool Size: " + MAX_POOL_SIZE);
        Utility.createSeparator();
    }
    
    private static void loadDB(){
        DBService.loadDBConfig();
    }

    public static void stopServer(){
        if(mServerContainer != null){
            try {
                Utility.printMessage("Shutting down the service...");
                mServerContainer.shutdownNow();
                ConfigService.getInstance().clear();
                DBService.stop();
                Utility.printMessage("Done, exit.");
            } catch (Exception e) {
                Utility.printError(e, "Failed shuting down the service");
            }
        }
    }
}
