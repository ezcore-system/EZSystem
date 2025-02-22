package co.id.ez.system;

import co.id.ez.database.DBService;
import co.id.ez.system.server.Server;
import co.id.ez.system.core.config.ConfigService;
import co.id.ez.system.core.config.Configuration;
import co.id.ez.system.core.etc.Utility;
import co.id.ez.system.server.http.HTTPServer;
import co.id.ez.system.server.websocket.WebSocketServer;
import co.id.ez.system.service.FutureService;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.fusesource.jansi.AnsiConsole;

public class Main {

    private static final List<Server> serverList = new ArrayList<>();
    private static final List<FutureService> FUTURE_SERVICE = new ArrayList<>();

    public static void main(String[] args) {
        Main main = new Main();
        try {
            if (args.length < 1) {
                Utility.printMessage("You need to provice configuration directory");
                System.exit(1);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    main.shutdownServer();
                } catch (Exception e) {
                    Utility.printError(e, "[Exception] Failed shuting down the service");
                } finally {
                    main.shutdownServer();
                }
            }));

            System.getProperties().load(Main.class.getResourceAsStream("/properties.properties"));
            long startTime = System.currentTimeMillis();
            AnsiConsole.systemInstall();

            ConfigService.createInstance(args[0]);

            Utility.drawAppsName(Main.class);

            List<Configuration> configServers = ConfigService.getInstance()
                    .getConfigList("ezsystem.connection.servers", new ArrayList());

            for (Configuration configuration : configServers) {
                String tType = configuration.getString("type");

                if (tType.equalsIgnoreCase("http")) {
                    HTTPServer httpServer = new HTTPServer(configuration);
                    Utility.printMessage(httpServer.getServerType());
                    Utility.createSeparator();
                    httpServer.build();
                    serverList.add(httpServer);
                    Utility.printMessage("HTTP Server started");
                    Utility.printMessage(httpServer.getServerDetail());
                    Utility.createSeparator();
                }

                if (tType.equalsIgnoreCase("web-socket")) {
                    WebSocketServer webSocketServer = new WebSocketServer(configuration);
                    Utility.printMessage(webSocketServer.getServerType());
                    Utility.createSeparator();
                    webSocketServer.build();
                    serverList.add(webSocketServer);
                    Utility.printMessage("Web Socket Server started");
                    Utility.printMessage(webSocketServer.getServerDetail());
                    Utility.createSeparator();
                }
            }

            main.loadFutureService();
            main.loadDatabase();
            main.startingFutureService();

            Utility.printMessage("*** START SUCCESS ***");
            long elapsedTime = System.currentTimeMillis() - startTime;
            Utility.printMessage("\nApplication started on " + elapsedTime + " ms at " + Calendar.getInstance().getTime().toString());
        } catch (IOException e) {
            main.shutdownServer();
            Utility.printMessage("*** START FAILED ***");
            Utility.printError(e, "[IOException] Failed when trying to Start service");
            System.exit(1);
        } catch (Exception ex) {
            Utility.printMessage("*** START FAILED ***");
            main.shutdownServer();
            Utility.printError(ex, "[Exception] Failed when trying to Start service");
            System.exit(1);
        } catch (Throwable ex) {
            Utility.printMessage("*** START FAILED ***");
            main.shutdownServer();
            Utility.printError(ex, "[Throwable] Failed when trying to Start service");
            System.exit(1);
        }
        AnsiConsole.systemUninstall();
    }

    private void shutdownServer() {
        Utility.printMessage("Shutting down the Server ...");
        for (Server server : serverList) {
            server.stopServer();
        }

        if (ConfigService.getInstance() != null) {
            ConfigService.getInstance().clear();
        }

        DBService.stop();
        Utility.printMessage("Shutting down Server done ...");
    }

    private void loadFutureService() throws IOException, InstantiationException, IllegalAccessException, 
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Utility.printMessage("Loading Future Service ...");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().contains("co.id.ez")
                    && !info.getName().toLowerCase().contains("module-info")) {
                final Class clazz = info.load();
                if (FutureService.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    Utility.printMessage(" - " + clazz.getName());
                    FUTURE_SERVICE.add((FutureService) clazz.getDeclaredConstructor().newInstance());
                }
            }
        }

        Utility.createSeparator();
    }

    private void startingFutureService() throws InstantiationException, IllegalAccessException {
        FUTURE_SERVICE.forEach(class1 -> {
            class1.start();
        });
    }

    private void loadDatabase() {
        DBService.loadDBConfig();
    }
}
