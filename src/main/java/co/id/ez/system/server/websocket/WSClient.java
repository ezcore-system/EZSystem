/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.id.ez.system.server.websocket;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author LUTFI ASB
 */
public class WSClient {
    
    private final String token, userid, clientid;
    private final List<String> topics;

    public WSClient(HashMap<String, Object> queryParam) {
        this.token = queryParam.get("token").toString();
        this.userid = queryParam.get("user").toString();
        this.clientid = queryParam.get("client").toString();
        this.topics = (List<String>) queryParam.get("topics");
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

    public List<String> getTopics() {
        return topics;
    }

    public boolean isConsumeTopic(String topic){
        return topics.contains(topic);
    }

    @Override
    public String toString() {
        return "WSClient{" + "token=" + token + ", userid=" + userid + ", clientid=" + clientid + '}';
    }
}
