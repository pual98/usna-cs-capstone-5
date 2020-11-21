import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Dataset
{
    public ArrayList<Entity> build(String filename)
    {
        BufferedReader source;
        ArrayList<Entity> ret = new ArrayList<Entity>();
        try{
            source = new BufferedReader(new FileReader(filename));
            String line = "";
            int i = 0;
            while(source.ready())
            {
                line = source.readLine();
                Entity en = Entity.makeEntityFromString(line);
                if(en != null)
                {
                    ret.add(en);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        return ret;
    }
}
