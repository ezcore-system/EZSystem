/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.ws;

import co.id.ez.system.core.ex.ServiceException;
import co.id.ez.system.core.rc.RC;
import co.id.ez.system.server.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.HandShake;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 *
 * @author LUTFI ASB
 */
@WebSocketPath("/hello")
public class WSApplication extends WebSocketApplication {

    protected ConcurrentHashMap<String, WebSocket> webSocketClients = new ConcurrentHashMap<>();
    protected String[] mandatoryKey = new String[]{"token", "user", "client"};
    
    @Override
    protected void handshake(HandShake handshake) throws HandshakeException {
        try {
            String tResourchPath = handshake.getLocation();
            URI uri = new URI(tResourchPath);
            HashMap<String, String> tReqQuey = parseQuery(uri.getQuery());
            validateMandatoryParam(tReqQuey);
            
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new HandshakeException(HttpStatus.FORBIDDEN_403.getStatusCode(), "Something wrong on server");
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new HandshakeException(HttpStatus.GATEWAY_TIMEOUT_504.getStatusCode(),"");
        } catch (Exception e) {
            e.printStackTrace();
            throw new HandshakeException(HttpStatus.BAD_REQUEST_400.getStatusCode(), "Something wrong on server");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new HandshakeException(1001, "Something wrong on server");
        }
    }
    
    public void validateMandatoryParam(HashMap<String, String> requesParam){
        for (String key : mandatoryKey) {
            if(!requesParam.containsKey(key)){
                throw new ServiceException(RC.ERROR_INVALID_MESSAGE, "Invalid mandatory param " + key);
            }
        }
    }
    
    private HashMap<String, String> parseQuery(String query) {
        HashMap<String, String> parserd = new HashMap<>();
        if (query != null) {
            String[] tQuerys = query.split("&");
            for (String tQuerye : tQuerys) {
                String[] tSplitQuery = tQuerye.split("=");
                if (tSplitQuery.length >= 2) {
                    parserd.put(tSplitQuery[0], tSplitQuery[1]);
                }
            }
        }

        return parserd;
    }
    
    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        WebSocket socket = super.createSocket(handler, requestPacket, listeners);
        HashMap<String, String> tReqQuey = parseQuery(requestPacket.getQueryString());
        if(!tReqQuey.isEmpty()){
            webSocketClients.put(tReqQuey.get("user").concat(tReqQuey.get("client")), socket);
        }
        return socket;
    }
    
    @Override
    protected boolean onError(WebSocket webSocket, Throwable t) {
        t.printStackTrace();
        System.out.println("Closed connection, " + t);
        return super.onError(webSocket, t);
    }

    @Override
    public void onConnect(WebSocket socket) {
        System.out.println("New Connection .... ");
        socket.send("Selamat datang, anda sudah terkoneksi dengan kamu");
        System.out.println("Current Client: " + webSocketClients.size());
        
        for (String string : webSocketClients.keySet()) {
            System.out.println("Socket: " + socket +" <> " + webSocketClients.get(string));
            if(webSocketClients.get(string).equals(socket)){
                System.out.println("Socket ID: " + string);
            }
        }
        
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        System.out.println("Closed connection, " + socket);
        webSocketClients.values().remove(socket);
        super.onClose(socket, frame);
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        System.out.println("Receive from " + socket + ": " + text);
    }

}
