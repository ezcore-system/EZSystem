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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    protected String[] mandatoryKey = new String[]{"token", "user", "client", "topics"};
    protected ConcurrentHashMap<WSClient, WebSocket> webSocketClients = new ConcurrentHashMap<>();

    public abstract void validateAccess(HashMap<String, Object> pQueryMaps);
    
    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        WebSocket socket = super.createSocket(handler, requestPacket, listeners);
        HashMap<String, Object> tReqQuey = parseQuery(requestPacket.getQueryString());
        WSClient client = new WSClient(tReqQuey);
        webSocketClients.put(client, socket);
        return socket;
    }

    @Override
    public void onPing(WebSocket socket, byte[] bytes) {
        super.onPing(socket, bytes); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected void handshake(HandShake handshake) throws HandshakeException {
        try {
            String tResourchPath = handshake.getLocation();
            URI uri = new URI(tResourchPath);
            HashMap<String, Object> tReqQuey = parseQuery(uri.getQuery());
            validateMandatoryParam(tReqQuey);
        } catch (URISyntaxException ex) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(ex).log("[Exception] Some thing wrong on HandShake.", true);
            throw new HandshakeException(WebSocket.PROTOCOL_ERROR, "Something wrong on server");
        } catch (ServiceException e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[ServiceException] Some thing wrong on HandShake. " + e.getMessage(), true);
            throw new HandshakeException(WebSocket.INVALID_DATA, e.getMessage());
        } catch (Exception e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Exception] Some thing wrong on HandShake.", true);
            throw new HandshakeException(WebSocket.PROTOCOL_ERROR, "Something wrong on server");
        } catch (Throwable e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Throwable] Some thing wrong on HandShake.", true);
            throw new HandshakeException(WebSocket.PROTOCOL_ERROR, "Something wrong on server");
        }
    }

    private HashMap<String, Object> parseQuery(String query) {
        HashMap<String, Object> parserd = new HashMap<>();
        if (query != null) {
            String[] tQuerys = query.split("&");
            for (String tQuerye : tQuerys) {
                String[] tSplitQuery = tQuerye.split("=");
                if (tSplitQuery.length >= 2) {
                    if (tSplitQuery[0].equalsIgnoreCase("topics")) {
                        String[] topics = tSplitQuery[1].split(";");
                        List<String> topicList = new ArrayList<>();
                        topicList.addAll(Arrays.asList(topics));
                        parserd.put(tSplitQuery[0], topicList);
                    } else {
                        parserd.put(tSplitQuery[0], tSplitQuery[1]);
                    }
                }
            }
        }

        return parserd;
    }

    public void validateMandatoryParam(HashMap<String, Object> requesParam) {
        for (String key : mandatoryKey) {
            if (!requesParam.containsKey(key)) {
                throw new ServiceException(RC.ERROR_INVALID_MESSAGE, "Invalid mandatory param " + key);
            }
        }
        
        validateAccess(requesParam);
    }

    @Override
    public void onConnect(WebSocket socket) {
        try {
            LogService.getInstance(this).temp("web-socket").log("Connected client: " + socket);
            super.onConnect(socket);
            socket.send("{\"code\": \"200\",\"status\": \"Conected\"}");
            LogService.getInstance(this).temp("web-socket").log("Client available: " + webSocketClients.size());
        } catch (Exception e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Exception] Some thing wrong on connect socket handler:" + socket, true);
            socket.close(WebSocket.END_POINT_GOING_DOWN, "Invalid Connection");
        } catch (Throwable e) {
            LogService.getInstance(this).temp("web-socket")
                    .withCause(e).log("[Throwable] Some thing wrong on connect socket handler:" + socket, true);
            socket.close(WebSocket.END_POINT_GOING_DOWN, "Invalid Connection");
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
        LogService.getInstance(this).temp("web-socket").log("Client available: " + webSocketClients.size());
    }

}
