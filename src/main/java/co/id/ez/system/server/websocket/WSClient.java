/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.id.ez.system.server.websocket;

import java.util.HashMap;

/**
 *
 * @author LUTFI ASB
 */
public class WSClient {
    
    private final String token, userid, clientid;

    public WSClient(HashMap<String, String> queryParam) {
        this.token = queryParam.get("token");
        this.userid = queryParam.get("user");
        this.clientid = queryParam.get("client");
    }

    public String getToken() {
        return token;
    }

    public String getUserid() {
        return userid;
    }

    public String getClientid() {
        return clientid;
    }
    
    public String getId(){
        return clientid.concat("@").concat(userid);
    }

    @Override
    public String toString() {
        return "WSClient{" + "token=" + token + ", userid=" + userid + ", clientid=" + clientid + '}';
    }
}
