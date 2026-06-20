package org.rmj.gcardappfx.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.FileUtil;
import org.rmj.gcard.trans.agentFX.XMGCOnPoints;
import org.rmj.gcard.trans.agentFX.XMGCard;

public class OnlinePointsEntryCancel {
    public static void main (String [] args){
        final String RESULT_DIR = "D:/GGC_Java_Systems/temp/res.TMP";
        
        JSONObject loJSON = new JSONObject();
        
        if (args.length != 7){ 
            loJSON.put("result", "error");
            loJSON.put("message", "Invalid parameters detected.");
            FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
            
            System.exit(1); 
        }
        
        if (!loadProperties()){
            loJSON.put("result", "error");
            loJSON.put("message", "Unable to load config.");
            FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
            
            System.exit(1); 
        } else System.out.println("Config file loaded successfully.");
        
        String lsProdctID = args[0];
        String lsUserIDxx = args[1];
        
        GRider poGRider = new GRider(lsProdctID);
        
        if (!poGRider.loadUser(lsProdctID, lsUserIDxx)){            
            loJSON.put("result", "error");
            loJSON.put("message", poGRider.getErrMsg());
            FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
            
            System.exit(1); 
        }
        
        String lsVal = CommonUtils.getConfiguration(poGRider, "QRActVldtn");
        if (lsVal != null) System.setProperty("app.gcard.digital.1", lsVal);    
        
        OnlineEntry instance = new OnlineEntry(poGRider, args[2], args[3], args[4], args[5], args[6]);
        
        if (instance.CancelTransaction()){
            loJSON.put("result", "success");
            loJSON.put("message", "Transaction ONLINE POINTS was cancelled successfully.");
            FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());

            System.exit(0); 
        } else {
            loJSON.put("result", "error");
            loJSON.put("message", instance.getMessage());
            FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());

            System.exit(1); 
        }
    }    
    
    public static class OnlineEntry{
        final String RESULT_DIR = "D:/GGC_Java_Systems/temp/res.TMP";
        
        GRider poGRider;
        XMGCOnPoints poTrans;
        XMGCard poCard;
        
        String psGCardNox;
        String psDigitalx;
        String psSourceNo;
        String psSourceCd;
        String pdTransact;
        
        String psMessage;
        
        public String getMessage(){return psMessage;}

        public OnlineEntry(GRider foGRider, String fsGCardNox, String fsDigitalx, String fsReferNox, String fsSourceCd, String fdTransact){
            JSONObject loJSON = new JSONObject();
            
            if (foGRider == null){                
                loJSON.put("result", "error");
                loJSON.put("message", "Application driver is not set.");
                FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
                System.exit(1); 
            }
            
            if (fsGCardNox.isEmpty()){
                loJSON.put("result", "error");
                loJSON.put("message", "G-Card number must not be empty.");
                FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
                System.exit(1); 
            }
            
            if (fsDigitalx.isEmpty()){
                loJSON.put("result", "error");
                loJSON.put("message", "G-Card digital must not be empty.");
                FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
                System.exit(1); 
            }
            
            if (fsReferNox.isEmpty()){
                System.err.println("Reference number must not be empty.");
                System.exit(1);
            }
            
            if (fsSourceCd.isEmpty()){
                loJSON.put("result", "error");
                loJSON.put("message", "Transaction code must not be empty.");
                FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
                System.exit(1); 
            }
            
            if (fdTransact.isEmpty()){
                loJSON.put("result", "error");
                loJSON.put("message", "Transaction date must not be empty.");
                FileUtil.fileWrite(RESULT_DIR, (String) loJSON.toJSONString());
                System.exit(1); 
            }
            
            poGRider = foGRider;
            poTrans = new XMGCOnPoints(poGRider, poGRider.getBranchCode(), false);
            
            psGCardNox = fsGCardNox;
            psDigitalx = fsDigitalx;
            psSourceNo = fsReferNox;
            psSourceCd = fsSourceCd;
            pdTransact = fdTransact;
        }
        
        public boolean CancelTransaction(){
            System.setProperty("app.device.type", psDigitalx);
            System.setProperty("app.card.no", getCardNo());
            
            String lsSQL = "SELECT" +
                                "  sTransNox" +
                                ", SUM(nPointsxx) nPointsxx" + 
                            " FROM G_Card_Detail" + 
                            " WHERE sGCardNox = " + SQLUtil.toSQL(psGCardNox) +
                                " AND dTransact = " + SQLUtil.toSQL(pdTransact) +
                                " AND sSourceCd = " + SQLUtil.toSQL(psSourceCd) +
                                " AND sSourceNo = " + SQLUtil.toSQL(psSourceNo) +
                            " GROUP BY sTransNox";
            
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            try {
                if (!loRS.next()){
                    psMessage = "No transaction exist.";
                    return true;
                }
                
                if (loRS.getDouble("nPointsxx") <= 0.00){
                    psMessage = "G-Card points was already cancelled.";
                    return true;
                }
                
                if (poTrans.loadTransaction(loRS.getString("sTransNox"))){
                    if (!poTrans.connectCard()){
                        psMessage = poTrans.getMessage();
                        return false;
                    }
                    
                    if (!poTrans.cancelTransaction(loRS.getString("sTransNox"))){
                        psMessage = poTrans.getMessage();
                        return false;
                    }
                } else {
                    psMessage = poTrans.getMessage();
                    return false;
                }
                
            } catch (SQLException e) {
                psMessage = e.getMessage();
                return false;
            }
            
            return true;
        }
        
        private String getCardNo(){
            String lsSQL = "SELECT sCardNmbr FROM G_Card_Master WHERE sGCardNox = " + SQLUtil.toSQL(psGCardNox);

            ResultSet loRS = poGRider.executeQuery(lsSQL);

            try {
                if (loRS.next()) return loRS.getString("sCardNmbr");
            } catch (SQLException ex) {
                Logger.getLogger(OnlinePointsEntryCancel.class.getName()).log(Level.SEVERE, null, ex);
            }

            return "";
        }    
    }
    
    private static boolean loadProperties(){
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream("D:\\GGC_Java_Systems\\config\\gcard.properties"));

            //User and Application Info 
            System.setProperty("user.id", po_props.getProperty("user.id"));
            System.setProperty("app.product.id", po_props.getProperty("app.product.id"));
            System.setProperty("app.main.office", po_props.getProperty("app.main.office"));
            System.setProperty("app.debug.mode", po_props.getProperty("app.debug.mode"));

            //G-Card Information
            System.setProperty("app.card.connected", "");
            System.setProperty("app.gcard.no", "");
            System.setProperty("app.gcard.holder", "");
            System.setProperty("app.card.no", "");
            System.setProperty("app.device.type", "");
            System.setProperty("app.device.data", "");
            
            //validation
            System.setProperty("app.gcard.digital.1", po_props.getProperty("app.gcard.digital.1"));

            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
