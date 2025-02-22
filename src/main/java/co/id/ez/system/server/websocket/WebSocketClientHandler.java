/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.id.ez.system.server.websocket;

import co.id.ez.system.core.ex.ServiceException;
import co.id.ez.system.core.log.LogService;
import co.id.ez.system.core.rc.RC;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.grizzly.http.HttpRequestPacket;
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
public abstract class WebSocketClientHandler extends WebSocketApplication {

    protected String[] mandatoryKey = new String[]{"token", "user", "client"};
    protected ConcurrentHashMap<WSClient, WebSocket> webSocketClients = new ConcurrentHashMap<>();

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        WebSocket socket = super.createSocket(handler, requestPacket, listeners);
        HashMap<String, String> tReqQuey = parseQuery(requestPacket.getQueryString());
        WSClient client = new WSClient(tReqQuey);
        webSocketClients.put(client, socket);
        return socket;
    }

    @Override
    protected void handshake(HandShake handshake) throws HandshakeException {
        try {
            String tResourchPath = handshake.getLocation();
            URI uri = new URI(tResourchPath);
            HashMap<String, String> tReqQuey = parseQuery(uri.getQuery());
            validateMandatoryParam(tReqQuey);
        } catch (URISyntaxException ex) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(ex).log("[Exception] Some thing wrong on HandShake.", true);
            throw new HandshakeException(1001, "Something wrong on server");
        } catch (ServiceException e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[ServiceException] Some thing wrong on HandShake. " + e.getMessage(), true);
            throw new HandshakeException(Integer.parseInt(e.getRC().getResponseCodeString()), e.getMessage());
        } catch (Exception e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Exception] Some thing wrong on HandShake.", true);
            throw new HandshakeException(1001, "Something wrong on server");
        } catch (Throwable e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Throwable] Some thing wrong on HandShake.", true);
            throw new HandshakeException(1001, "Something wrong on server");
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
    
    public void validateMandatoryParam(HashMap<String, String> requesParam){
        for (String key : mandatoryKey) {
            if(!requesParam.containsKey(key)){
                throw new ServiceException(RC.ERROR_INVALID_MESSAGE, "Invalid mandatory param " + key);
            }
        }
    }

    @Override
    public void onConnect(WebSocket socket) {
        try {
            LogService.getInstance(this).temp("web-socket").log("Connected client: " + socket);
            super.onConnect(socket);
            socket.send("{\"code\": \"200\",\"status\": \"Conected\"}");
        } catch (Exception e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Exception] Some thing wrong on connect socket handler:" + socket, true);
            socket.close(1002, "Invalid Connection");
        } catch (Throwable e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Throwable] Some thing wrong on connect socket handler:" + socket, true);
            socket.close(1002, "Invalid Connection");
        }

    }

    @Override
    protected boolean onError(WebSocket webSocket, Throwable t) {
        LogService.getInstance(this).temp("web-socket")
                .withCause(t).log("[Error] some thing wrong on socket handler:" + webSocket, true);
        return super.onError(webSocket, t);
    }

    public String createSessionID(WebSocket socket) {
        String tVal = socket == null
                ? String.valueOf(Math.abs(UUID.randomUUID().toString().hashCode()))
                : String.valueOf(Math.abs(socket.hashCode() * 53));

        return "@".concat(tVal);
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        LogService.getInstance(this).temp("web-socket").log("Client clossed: " + socket);
        webSocketClients.values().remove(socket);
        super.onClose(socket, frame);
    }

}
