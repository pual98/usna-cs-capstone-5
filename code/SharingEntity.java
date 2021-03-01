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

  public void setModeMap(ArrayList<HashMap<Integer,Integer>> newMap)
  {
      this.modeMap = newMap;
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
  public void setCountShare(int s)
  {
    countShare = s;
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
    // System.out.println("AFTER addSharingEntity countShare = "+countShare);
  }


  public Entity toEntity()
  {
    ArrayList<Double> retQuals = new ArrayList<Double>();
    for(int i = 0 ; i  < qualities.size(); i++)
    {
      if(Math.abs(qualities.get(i)) < 0.000001)
        qualities.set(i, 0.0);
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

    for(int i = 0 ; i < shares-1; i++)
    {
      ArrayList<Double> squal = new ArrayList<Double>();
      for(int j = 0; j < numQuals; j++)
      {
        squal.add((double)rand.nextFloat());
      }
      SharingEntity toAdd = new SharingEntity();
      toAdd.setClusterLabel(clusterLabel);
      toAdd.setIterationLabel(iterationLabel);
      toAdd.setConv(conv);
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
      // System.out.println("sum " + sum);
      double finalShare = qualities.get(j) - sum;
      // System.out.println("final " + finalShare);
      finalShareQuals.add(finalShare);
    }
    SharingEntity finalShare = new SharingEntity();
    finalShare.setQuals(finalShareQuals);
    finalShare.setClusterLabel(clusterLabel);
    finalShare.setConv(conv);
    finalShare.setIterationLabel(iterationLabel);
    finalShare.countShare = countShare-2;
    // System.out.println("COUNT SHARE AT THE END OF MAKESHARE: "+ finalShare.countShare);
    ret.add(finalShare);
    return ret;
  }

  public static void main(String[] args)
  {
    System.out.println("testing share splitting");
    SharingEntity toSplit = new SharingEntity();
    ArrayList<Double> qualss = new ArrayList<Double>();
    qualss.add(0.2);
    qualss.add(0.2);
    qualss.add(0.1);

    toSplit.setQuals(qualss);
    toSplit.setCountShare(1);
    SharingEntity toSplit2 = new SharingEntity();
    ArrayList<Double> qualss2 = new ArrayList<Double>();
    qualss2.add(0.4);
    qualss2.add(0.4);
    qualss2.add(0.5);

    toSplit2.setQuals(qualss2);
    toSplit2.setCountShare(1);

    System.out.println("OG Values " + toSplit.getQuals());

    Random rand = new Random();
    ArrayList<SharingEntity> shares = toSplit.makeShares(3,rand);
    ArrayList<SharingEntity> shares2 = toSplit2.makeShares(3,rand);
    SharingEntity sumofshares = new SharingEntity();
    System.out.println("size " + shares.size());

    int i = 0;
    System.out.println("FIRST");
    for(SharingEntity s : shares)
    {
      System.out.println("SHARE = "+i+" "+s.getQualities());
      sumofshares.addSharingEntity(s);
      i++;
    }
    i = 0;
    System.out.println("SECOND");
    for(SharingEntity s : shares2)
    {
      System.out.println("SHARE = "+i+" "+s.getQualities());
      sumofshares.addSharingEntity(s);
      i++;
    }
    System.out.println(sumofshares.getQualities());
    System.out.println(sumofshares.toEntity());

  }
}
