/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.db;

import co.id.ez.database.DB;
import co.id.ez.database.DBService;
import co.id.ez.system.core.config.ConfigService;
import java.sql.SQLException;
import java.util.LinkedList;
import org.json.JSONObject;

/**
 *
 * @author LUTFI ASB
 */
public class DBTester {
    public static void main(String[] args) {
        ConfigService.createInstance("conf/");
        DBService.loadDBConfig();
        
        try {
            String tSQL = "SELECT start_time, end_time "
                    + "FROM `ctl_system_cutoff` "
                    + "WHERE CONCAT( DATE_FORMAT( NOW(), '%Y-%m-%d' ), ' ', start_time ) < DATE_FORMAT( NOW(), '%Y-%m-%d %H:%i:%s' ) "
                    + "AND ( CONCAT( DATE_FORMAT( NOW(), '%Y-%m-%d' ), ' ', end_time ) > DATE_FORMAT( NOW(), '%Y-%m-%d %H:%i:%s' ) "
                    + "OR DATE_ADD( CONCAT( DATE_FORMAT( NOW(), '%Y-%m-%d' ), ' ', end_time ), INTERVAL 1 DAY ) > DATE_FORMAT( NOW(), '%Y-%m-%d %H:%i:%s' )) "
                    + "AND `status` = 1 LIMIT 1";

            LinkedList<JSONObject> tResult = DB.executeQuery("payment", tSQL);
            if (tResult != null) {
                System.out.println("Result: " + tResult);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
