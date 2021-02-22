import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

public class SharingEntity implements Serializable
{

    private int countShare;
    private ArrayList<HashMap<Integer,Integer>> modeMap;
    private ArrayList<Double> qualities;
    private int numQuals;
    private int numCats;
    private int clusterLabel;
    private int iterationLabel;
    private boolean conv;

    public SharingEntity(int cs, ArrayList<Double> qual,  ArrayList<HashMap<Integer,Integer>> cat)
    {
        this.countShare = cs;
        this.qualities = qual;
        this.modeMap = cat;
        this.numQuals = qual.size();
        this.numCats = cat.size();
    }
    public SharingEntity(int cs, ArrayList<Double> qual,  ArrayList<HashMap<Integer,Integer>> cat, boolean cv)
    {
        this.countShare = cs;
        this.qualities = qual;
        this.modeMap = cat;
        this.numQuals = qual.size();
        this.numCats = cat.size();
        this.conv = cv;
    }

    public SharingEntity()
    {
        this.countShare = 0;
        this.qualities = new ArrayList<Double>();
        for(int i = 0 ; i < 3; i++)
        {
            qualities.add(0.0);
        }
        this.modeMap = new ArrayList<HashMap<Integer,Integer>>();
        for(int i = 0 ; i < 4; i++)
        {
            modeMap.add(new HashMap<Integer,Integer>());
        }
        this.numQuals = qualities.size();
        this.numCats = 4;//.size();
        this.conv = false;
    }

    public void setConv(boolean b)
    {
        conv = b;
    }

    public boolean getConv()
    {
        return conv;
    }

    public double getQual(int i)
    {
        return qualities.get(i);
    }


    public ArrayList<Double> getQuals()
    {
        return qualities;
    }

    public void setQual(int index, Double ds)
    {
        this.qualities.set(index,ds);
    }

    public void setQuals(ArrayList<Double> ds)
    {
        this.qualities = ds;
    }

    public ArrayList<Double> getQualities()
    {
        return qualities;
    }

    public int getNumQuals()
    {
        return qualities.size();
    }
    public  ArrayList<HashMap<Integer,Integer>> getModeMap()
    {
        return modeMap;
    }

    public void setClusterLabel(int label)
    {
        clusterLabel = label;
    }

    public int getClusterLabel()
    {
        return clusterLabel;
    }

    public void setIterationLabel(int label)
    {
        iterationLabel = label;
    }

    public int getIterationLabel()
    {
        return iterationLabel;
    }

    public int getCountShare()
    {
        return countShare;
    }

    public SharingEntity(int qualCount,int catCount)
    {
        this.countShare = 0;
        this.qualities = new ArrayList<Double>();
        this.modeMap = new ArrayList<HashMap<Integer,Integer>>();
        for(int i = 0 ; i < qualCount; i++)
        {
            qualities.add(0.0);
        }
        for(int i = 0 ; i < catCount; i++)
        {
            modeMap.add(new HashMap<Integer,Integer>());
        }
    }

    public int getFinalCount()
    {
        return countShare;
    }

    public static SharingEntity merge(SharingEntity a, SharingEntity b)
    {
        ArrayList<Double> retQuals = new ArrayList<Double>();
        for(int i = 0 ; i <  a.getNumQuals(); i++)
        {
            retQuals.add(a.getQual(i) + b.getQual(i));
        }
        ArrayList<HashMap<Integer,Integer>> retModeMap = KMode.mergeMaps(a.modeMap, b.modeMap);
        int retCountShare = a.countShare + b.countShare;
        SharingEntity ret = new SharingEntity(retCountShare, retQuals, retModeMap);

        return ret;
    }

    //NEWT
    public void addSharingEntity(SharingEntity b)
    {
        ArrayList<Double> retQuals = new ArrayList<Double>();
        for(int i = 0 ; i <  b.getNumQuals(); i++)
        {
            retQuals.add(qualities.get(i) + b.getQual(i));
        }
        qualities = retQuals;
        modeMap =  KMode.mergeMaps(modeMap, b.modeMap);
        countShare = countShare + b.countShare; 
    }


    public Entity toEntity()
    {
        ArrayList<Double> retQuals = new ArrayList<Double>();
        for(int i = 0 ; i  < qualities.size(); i++)
        {
            retQuals.add(qualities.get(i) / countShare);
        }
        ArrayList<Integer> retCats = KMode.modes(modeMap);
        Entity ret = new Entity(retQuals, retCats);
        ret.setCluster(clusterLabel);
        return ret;
    }

    public long estimatedSizeInBytes()
    {
        long ret = qualities.size() * 8;//size of qualities
        for(int i = 0 ; i < modeMap.size(); i++)
        {
            ret += 16 * modeMap.get(i).size();//size of category map
        }
        ret += 8;//count share
        return ret;
    }

    //NEWT
    /**
      public void addEntity(Entity toAdd)
      {
      countShare++;
      qualities = KMode.merge(qualities,toAdd.getQualities);
      newMap = KMode.makeNewModeMap(toAdd.getCategories());
      modeMap = KMode.mergeMaps(modeMap,newMap);
      numQuals = qual.size();
      numCats = cat.size();
      }**/

    public void addEntity(Entity toAdd)
    {
        countShare++;
        qualities = KMode.merge(qualities,toAdd.getQualities());
        ArrayList<HashMap<Integer,Integer>> newMap = KMode.makeNewModeMap(toAdd.getCategories());
        modeMap = KMode.mergeMaps(modeMap,newMap);
        numQuals = qualities.size();
        numCats = toAdd.getCategoryCount();
    }

    public ArrayList<SharingEntity> makeShares(int shares, Random rand)
    {
        ArrayList<SharingEntity> ret = new ArrayList<SharingEntity>();

        for(double q : qualities)
        {
            for(int i = 0 ; i < shares-1; i++)
            {
                ArrayList<Double> squal = new ArrayList<Double>();
                for(int j = 0; j < numQuals; j++)
                {
                    squal.add(rand.nextDouble());
                }
                SharingEntity toAdd = new SharingEntity();
                toAdd.setClusterLabel(clusterLabel);
                toAdd.setIterationLabel(iterationLabel);
                toAdd.countShare = 1;
                toAdd.setQuals(squal);
                ret.add(toAdd);
            }
            ArrayList<Double> finalShareQuals = new ArrayList<Double>();
            for(int j = 0; j < numQuals; j++)
            {
                double sum = 0;
                for(int i = 0; i < shares-1; i++)
                {
                    sum += ret.get(i).getQual(j);
                }
                double modSum =(double) (sum - (double)(Math.floor(sum)));
                double finalShare = qualities.get(j) - modSum; 
                finalShareQuals.add(finalShare);
            }
            SharingEntity finalShare = new SharingEntity();
            finalShare.setQuals(finalShareQuals);
            finalShare.setClusterLabel(clusterLabel);
            finalShare.setIterationLabel(iterationLabel);
            finalShare.countShare = countShare-2;
            ret.add(finalShare);
        }
        return ret;
    }

    public static void main(String[] args)
    {
        System.out.println("testing share splitting");
        SharingEntity toSplit = new SharingEntity();
        ArrayList<Double> qualss = new ArrayList<Double>();
        qualss.add(0.8578789798);
        qualss.add(0.4535655656);
        qualss.add(0.1434345343);

        toSplit.setQuals(qualss);
        System.out.println("OG Values " + toSplit.getQuals());


        Random rand = new Random();

        ArrayList<SharingEntity> shares = toSplit.makeShares(6,rand);

        ArrayList<Double> reconst = new ArrayList<Double>();

        for(int i = 0 ; i < 3; i++)
        {
            reconst.add(0.0);
        }

        for(int i = 0 ; i < 6; i++)
        {
            ArrayList<Double> dubs = shares.get(i).getQuals();
            System.out.println("Share " + i + " "  + shares.get(i).getQuals());
            for(int j = 0 ; j < 3 ; j++)
            {
                double rec = reconst.get(j) + dubs.get(j);
                reconst.set(j, rec - ((double) (int) rec));
            }
        }
        System.out.println("NG Values " + reconst);

    }

}
