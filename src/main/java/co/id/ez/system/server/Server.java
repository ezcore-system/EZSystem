/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.id.ez.system.server;

import co.id.ez.system.core.config.Configuration;
import java.io.IOException;

/**
 *
 * @author Lutfi
 */
public abstract class Server {

    private final Configuration configServer;
    protected String BASE_URI = "http://0.0.0.0:";
    protected String NAME;
    protected int PORT, POOL_SIZE, MAX_POOL_SIZE, QUEUE;

    public abstract void build() throws IOException, Exception;

    public abstract String getServerType();

    public abstract void addShutdownHook();

    public abstract String getServerDetail();
    
    public abstract void stopServer();

    public Server(Configuration configServer) {
        this.configServer = configServer;
    }

    public Configuration getConfigServer() {
        return configServer;
    }
}
