import java.util.ArrayList;
public class KMeans
{
  private int NUM_CLUSTERS;
  //Number of Points
  private int NUM_ENTITIES;
  private int DIMENSIONS;
  private int CATEGORIES;

  private ArrayList<Entity> entities;
  private ArrayList<EntityCluster> clusters;

  public KMeans(int NUM_CLUSTERS, int NUM_ENTITIES, int DIMENSIONS, int CATEGORIES) {
    this.entities = new ArrayList<Entity>();
    this.clusters = new ArrayList<EntityCluster>();

    this.NUM_CLUSTERS = NUM_CLUSTERS;
    this.NUM_ENTITIES = NUM_ENTITIES;
    this.DIMENSIONS = DIMENSIONS;
    this.CATEGORIES = CATEGORIES;
  }

  public void init() {
    //Create Points
    entities = Entity.createRandomEntities(DIMENSIONS, CATEGORIES, NUM_ENTITIES);

    //Create Clusters
    //Set Random Centroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster cluster = new EntityCluster(i);
      Entity centroid = Entity.createRandomEntity(DIMENSIONS, CATEGORIES);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }

    for(Entity e : entities)
      {
	       System.out.println(e);
      }


    //Print Initial state
    plotClusters();
  }


  private void plotClusters() {
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster c = clusters.get(i);
      c.plotCluster();
    }
  }

  private void clearClusters() {
    for(EntityCluster cluster : clusters) {
      cluster.clear();
    }
  }

  public void calculate() {
    boolean finish = false;
    int iteration = 0;

    // Add in new data, one at a time, recalculating centroids with each new one.
    while(!finish) {
      //Clear cluster state
      clearClusters();

      ArrayList<Entity> lastCentroids = getCentroids();

      //Assign points to the closer cluster
      assignCluster();// <-----------------------------------MARK

      //Calculate new centroids.
      calculateCentroids();//<--------------------------------MARK

      iteration++;

      ArrayList<Entity> currentCentroids = getCentroids();//<---------------MARK

      //Calculates total distance between new and old Centroids
      double distance = 0;
      for(int i = 0; i < lastCentroids.size(); i++) {
	distance += Entity.distanceEuclidean(lastCentroids.get(i),currentCentroids.get(i));
      }
      System.out.println("#################");
      System.out.println("Iteration: " + iteration);
      System.out.println("Centroid distances: " + distance);
      plotClusters();

      if(distance == 0) {
	finish = true;
      }
    }
  }


   private ArrayList<Entity> getCentroids() {
    	ArrayList<Entity> centroids = new ArrayList<Entity>(NUM_CLUSTERS);
    	for(EntityCluster cluster : clusters) {
    		Entity aux = cluster.getCentroid();
    		Entity point = new Entity(aux.getQualities(),aux.getCategories());
    		centroids.add(point);
    	}
    	return centroids;
    }

 private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;

        for(Entity point : entities) {
        	min = max;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
            	EntityCluster c = clusters.get(i);
                distance = Entity.distanceEuclidean(point, c.getCentroid());
                if(distance < min){
                    min = distance;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addEntity(point);
        }
    }

  private void calculateCentroids()
  {
    for(EntityCluster cluster : clusters)
      {
	ArrayList<Entity> list = cluster.getEntities();
	int n_points = list.size();
	double[] sum = new double[DIMENSIONS];
	ArrayList<ArrayList<Integer>> catEntries = new ArrayList<ArrayList<Integer>>();
	for(int i = 0 ; i < CATEGORIES; i++)
	  {
	    catEntries.add(new ArrayList<Integer>());
	  }

	for(Entity point : list)
	  {
	    for(int i = 0 ; i < DIMENSIONS; i++)
	      sum[i] += point.getQualities().get(i);
	    for(int i = 0; i < CATEGORIES; i++)
	      {
		catEntries.get(i).add(point.getCat(i));
	      }
	  }
	ArrayList<Integer> cats = new ArrayList<Integer>();
	for(int i = 0; i < CATEGORIES; i++)
	  {
	    cats.add(KMode.mode(catEntries.get(i)));
	  }


	Entity centroid = cluster.getCentroid();
	if(n_points > 0)
	  {
	    ArrayList<Double> quals = new ArrayList<Double>();
	    for(int i = 0; i < DIMENSIONS; i++)
	      {
		quals.add(sum[i]/n_points);
	      }
	    centroid.setQualities(quals);
	    centroid.setCategories(cats);
	  }
      }

  }

  public double averageMSE()
  {
    double ret = 0.0;
    for(EntityCluster c : clusters)
      {
	ret += c.MSE();
      }
    return ret/NUM_CLUSTERS;
  }



  public static void main(String[] args)
  {

    int NUM_CLUSTERS = 10;
    //Number of Points
    int NUM_ENTITIES = 1000;
    int DIMENSIONS = 4;
    int CATEGORIES = 2;
    KMeans dkmeans = new KMeans(NUM_CLUSTERS,NUM_ENTITIES, DIMENSIONS, CATEGORIES);
    dkmeans.init();
    dkmeans.calculate();
    System.out.println("average MSE " + dkmeans.averageMSE());
  }


}
