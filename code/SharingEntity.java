import java.util.ArrayList;
import java.util.HashMap;

public class SharingEntity
{
  
  private int countShare;
  private ArrayList<HashMap<Integer,Integer>> modeMap;
  private ArrayList<Double> qualities;
  private int numQuals;
  private int numCats;
  
  public SharingEntity(int cs, ArrayList<Double> qual,  ArrayList<HashMap<Integer,Integer>> cat)
  {
    this.countShare = cs;
    this.qualities = qual;
    this.modeMap = cat;
    this.numQuals = qual.size();
    this.numCats = cat.size();
  }
  
  public SharingEntity()
  {
    this.countShare = 0;
    this.qualities = new ArrayList<Double> qualities();
    for(int i = 0 ; i < 4; i++)
      {
	qualities.add(0);
      }
    this.modeMap = new ArrayList<HashMap<Integer,Integer>> modeMap();
    this.numQuals = qual.size();
    this.numCats = cat.size();
  }

  public double getQual(int i)
  {
    return qualities.get(i);
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
    ArrayList<HashMap<Integer,Integer>> retModeMap = KMode.mergeMapLists(a.modeMap, b.modeMap);
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
    modeMap =  KMode.mergeMapLists(modeMap, b.modeMap);
    countShare = countShare + b.countShare;
  }

  
  public Entity toEntity()
  {
    ArrayList<Double> retQuals = new ArrayList<Double>();
    for(int i = 0 ; i  < qualities.size(); i++)
      {
	retQuals.add(qualities.get(i) / countShare);
      }
    ArrayList<Integer> retCats = KMode.mode(modeMap);
    return new Entity(retQuals, retCats);
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
  public void addEntity(Entity toAdd)
  {
    countShare++;
    qualities = KMode.merge(qualities,toAdd.getQualities);
    newMap = KMode.makeNewModeMap(toAdd.getCategories());
    modeMap = KMode.mergeMaps(modeMap,newMap);
    numQuals = qual.size();
    numCats = cat.size();
  }

  public void addEntity(Entity toAdd)
  {
    countShare++;
    qualities = KMode.merge(qualities,toAdd.getQualities);
    newMap = KMode.makeNewModeMap(toAdd.getCategories());
    modeMap = KMode.mergeMaps(modeMap,newMap);
    numQuals = qual.size();
    numCats = cat.size();
  }

  
}