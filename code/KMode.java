import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

public class KMode
{
  public KMode()
  {
    
  }

  
  public static int mode(ArrayList<Integer> list)
  {
    HashMap<Integer,Integer> entries = new HashMap<Integer,Integer>();
    for(Integer i : list)
      {
	if(entries.containsKey(i))
	  {
	    entries.put(i, 1 + entries.get(i));
	  }
	else
	  entries.put(i,1);
      }

    Set<Map.Entry<Integer,Integer>> pairs = entries.entrySet();
    int mode = -1;
    int maxCount = 0;
    for(Map.Entry<Integer,Integer> e : pairs)
      {
	if(e.getValue() > maxCount)
	  {
	    mode = e.getKey();
	    maxCount = e.getValue();
	  }
      }
    return mode;
  }
  
 public static int mode( HashMap<Integer,Integer> entries)
  {
    Set<Map.Entry<Integer,Integer>> pairs = entries.entrySet();
    int mode = -1;
    int maxCount = 0;
    for(Map.Entry<Integer,Integer> e : pairs)
      {
	if(e.getValue() > maxCount)
	  {
	    mode = e.getKey();
	    maxCount = e.getValue();
	  }
      }
    return mode;
  }

  public static ArrayList<Integer> modes(ArrayList<HashMap<Integer,Integer>> entries)
  {
    ArrayList<Integer> ret = new ArrayList<Integer>();
    for(HashMap<Integer,Integer> i : entries)
      {
	ret.add(mode(i));
      }
    return ret;
  }

  //NEWT
  public static ArrayList<HashMap<Integer,Integer>> makeNewModeMap(ArrayList<Integer> cats)
  {
     ArrayList<HashMap<Integer,Integer>> ret = new ArrayList<HashMap<Integer,Integer>>();
    for(int i = 0; i < cats.size(); i++)
      {
	HashMap<Integer,Integer> newCat = new HashMap<Integer,Integer>();
	newCat.put(cats.get(i),1);
	  ret.add(newCat);
      }
    return ret;    
  }
  
  public static HashMap<Integer,Integer> getCategoryHashMap(ArrayList<Integer> list)
  {
    HashMap<Integer,Integer> entries = new HashMap<Integer,Integer>();
    for(Integer i : list)
      {
	if(entries.containsKey(i))
	  {
	    entries.put(i, 1 + entries.get(i));
	  }
	else
	  entries.put(i,1);
      }

    return entries;
  }
  
  public static ArrayList<Double> merge(ArrayList<Double> a, ArrayList<Double> b)
  {
    int QUALITIES = a.size(); 
    ArrayList<Double> ret = new ArrayList<Double>();
    for(int i = 0 ;  i < QUALITIES; i++)
      ret.add(0.0);
    for(int i  = 0 ; i < QUALITIES; i++)
      {
	ret.set(i, a.get(i) + b.get(i));
      }
    return ret;
  }

  public static HashMap<Integer,Integer> mergeMaps(HashMap<Integer,Integer> inA, HashMap<Integer,Integer> inB)
  {
    Set<Map.Entry<Integer,Integer>> A = inA.entrySet();
    Set<Map.Entry<Integer,Integer>> B = inB.entrySet();
    HashMap<Integer,Integer> ret = new HashMap<Integer,Integer>();
    for(Map.Entry<Integer,Integer> e : A)
      {
	ret.put(e.getKey(),e.getValue());
      }
    for(Map.Entry<Integer,Integer> e : B)
      {
	if(ret.containsKey(e.getKey()))
	  {
	    ret.put(e.getKey(), ret.get(e.getKey()) + e.getValue());
	  }
      }
    return ret;
  }
  public static ArrayList<HashMap<Integer,Integer>> mergeMaps(ArrayList<HashMap<Integer,Integer>> inA, ArrayList<HashMap<Integer,Integer>> inB)
  {
    ArrayList<HashMap<Integer,Integer>> ret = new ArrayList<HashMap<Integer,Integer>>();
    for(int i = 0 ; i < inA.size(); i++)
      {
	ret.add(mergeMaps(inA.get(i),inB.get(i)));
      }
    return ret;
  }
  
  public static void main(String[] args)
  {
    ArrayList<Entity> test = Entity.createRandomEntities(2,2,5);

    for(int i = 0; i < test.get(0).getCategoryCount(); i++)
      {
	ArrayList<Integer> cat = new ArrayList<Integer>();
	for(Entity e : test)
	  {
	    System.out.println(e);
	    cat.add(e.getCat(i));

	    
	  }
	HashMap<Integer,Integer> modeMap = getCategoryHashMap(cat);
	HashMap<Integer,Integer> modeMap2 = getCategoryHashMap(cat);
	HashMap<Integer,Integer> merged = mergeMaps(modeMap, modeMap2);

	System.out.println("category " + i + " mode is " + mode(merged));
      }
  }
}
