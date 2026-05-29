import org.rmj.gcardappfx.commands.OnlinePointsEntryCancel;

public class testOnlinePointsEntryTDSCancel {
    public static void main(String [] args){       
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        String argument [] = new String[7];
        //:\GGC_Java_Systems\gcard-online-points-entry.bat IntegSys M001111122 M02822000170 2 32323 M02910000004
        argument[0] = "IntegSys";
        argument[1] = "M001111122";
        argument[2] = "M15125000138";
        argument[3] = "1";
        argument[4] = "654347";
        argument[5] = "M02910000002";
        argument[6] = "2026-05-29";
        
        OnlinePointsEntryCancel.main(argument);
    }
}
