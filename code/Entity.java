import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;

public class Entity implements Serializable
{
  private ArrayList<Double> qualities;
  private ArrayList<Integer> categories;

  private int d;
  private int dCat;
  private int cluster_number = -1;


  public Entity(ArrayList<Double> qualities, ArrayList<Integer> categories)
  {
    this.d = qualities.size();
    this.qualities = qualities;
    this.dCat = categories.size();
    this.categories = categories;
  }

  public Entity getCopy()
  {
    return new Entity(qualities,categories);
  }

  public int getEntitySizeInBytes()
  {
    return (d * 8) + (dCat * 4) + 12;//quality size + category size + Entity structure
  }

  public void setQualities(ArrayList<Double> q)
  {
    qualities = q;
  }

  public void setCategories(ArrayList<Integer> cat)
  {
    categories = cat;
  }


  public double getQual(int i)
  {
    return qualities.get(i);
  }

  public int getCat(int i)
  {
    return categories.get(i);
  }

  public ArrayList<Double> getQualities()
  {
    return qualities;
  }

  public ArrayList<Integer> getCategories()
  {
    return categories;
  }

  public int getQualityCount()
  {
    return d;
  }

  public int getCategoryCount()
  {
    return dCat;
  }

  public void setCluster(int n) {
    this.cluster_number = n;
  }

  public int getAssignedCluster() {
    return this.cluster_number;
  }

  public static Entity createRandomEntity(int d, int dCat, int seed)
  {
    Random r = new Random(seed);
    ArrayList<Double> retQual = new ArrayList<Double>();
    for(int i  = 0 ; i  < d ; i ++)
    {
      retQual.add(r.nextDouble());
    }
    ArrayList<Integer> retCat = new ArrayList<Integer>();
    for(int i  = 0 ; i  < dCat ; i ++)
    {
      retCat.add(r.nextInt(5));
    }
    return new Entity(retQual, retCat);
  }


  public static Entity createRandomEntity(int d, int dCat, long seed)
  {
    Random r = new Random(seed);
    ArrayList<Double> retQual = new ArrayList<Double>();
    for(int i  = 0 ; i  < d ; i ++)
    {
      retQual.add(r.nextDouble());
    }
    ArrayList<Integer> retCat = new ArrayList<Integer>();
    for(int i  = 0 ; i  < dCat ; i ++)
    {
      retCat.add(r.nextInt(5));
    }
    return new Entity(retQual, retCat);
  }

  protected static ArrayList<Entity> createRandomEntities(int d, int dCat, int number)
  {
    ArrayList<Entity> ret = new ArrayList<Entity>(number);
    for(int i = 0; i < number; i++) {
      ret.add(createRandomEntity(d, dCat, 2021));
    }
    return ret;
  }

  protected static double distanceEuclidean(Entity a, Entity b)
  {
    double sum = 0;
    for(int i = 0; i  < a.getQualityCount(); i++ )
      sum += Math.pow(a.getQual(i) - b.getQual(i),2);

    for(int i = 0; i  < a.getCategoryCount(); i++){
      if(a.getCat(i) != b.getCat(i))
        sum+=1;
    }
    return Math.sqrt(sum);
  }

  public static Entity getEmptyEntity() {
    ArrayList<Double> qualities = new ArrayList<Double>();
    ArrayList<Integer> categories = new ArrayList<Integer>();
    //three qualities set to zero
    qualities.add(new Double(0));
    qualities.add(new Double(0));
    qualities.add(new Double(0));
    //four categories set to zero
    categories.add(new Integer(0));
    categories.add(new Integer(0));
    categories.add(new Integer(0));
    categories.add(new Integer(0));

    Entity empty = new Entity(qualities, categories);
    return empty;
  }


  public String toString()
  {
    String ret = "";
    if(d != 0) {
      for(int i = 0 ; i < d; i++)
        ret += qualities.get(i) + ",";
    }
    if(dCat != 0) {
      for(int i = 0 ; i < dCat-1; i++)
        ret += categories.get(i) + ",";
      ret += categories.get(dCat-1);
    }
    return "(" + ret + ")" + " " + cluster_number;
  }

  public boolean isNaN() {
    for(double q : qualities) {
      if(Double.isNaN(q))
        return true;
    }
    return false;
  }


  public static Entity makeEntityFromString(String normalizedData){
    if(normalizedData.contains("null"))
      return null;
    String[] data = normalizedData.split(";");
    //[srcIP, destIP, timestamp, ruleID, srcPort, destPort, alertType]
    double srcIP = Double.parseDouble(data[0]);
    double destIP = Double.parseDouble(data[1]);
    double timestamp = Double.parseDouble(data[2]);
    int ruleID = Integer.parseInt(data[3]);
    int srcPort = Integer.parseInt(data[4]);
    int destPort = Integer.parseInt(data[5]);
    int alertType = Integer.parseInt(data[6]);

    ArrayList<Double> dArr = new ArrayList<Double>();
    dArr.add(srcIP);
    dArr.add(destIP);
    dArr.add(timestamp);

    ArrayList<Integer> iArr = new ArrayList<Integer>();
    iArr.add(ruleID);
    iArr.add(srcPort);
    iArr.add(destPort);
    iArr.add(alertType);
    return new Entity(dArr, iArr);
  }


  public static void main(String[] args)
  {
    Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    // Entity a = Entity.createRandomEntity(2,2);
    // Entity b = Entity.createRandomEntity(2,2);
    // a = Entity.createRandomEntity(2,0);
    // b = Entity.createRandomEntity(2,0);
    // a = Entity.createRandomEntity(0,0);
    // b = Entity.createRandomEntity(0,0);
    // a = Entity.createRandomEntity(0,2);
    // b = Entity.createRandomEntity(0,2);
  }
}
