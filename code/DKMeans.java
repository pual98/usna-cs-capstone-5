import java.util.ArrayList;
public class DKMeans
{
  private int NUM_CLUSTERS;
  //Number of Points
  private int NUM_ENTITIES;
  private int DIMENSIONS;
  private int CATEGORIES;

  private ArrayList<alert> alerts;
  private ArrayList<AlertCluster> clusters;

  public DKMeans(int NUM_CLUSTERS, int NUM_ENTITIES, int DIMENSIONS, int CATEGORIES) {
    this.alerts = new ArrayList<alert>();
    this.clusters = new ArrayList<AlertCluster>();

    this.NUM_CLUSTERS = NUM_CLUSTERS;
    this.NUM_ENTITIES = NUM_ENTITIES;
    this.DIMENSIONS = DIMENSIONS;
    this.CATEGORIES = CATEGORIES;
  }

  public void init() {
    //Create Points
    //needs to be implemented still
    alerts = alert.createRandomEntities(DIMENSIONS, CATEGORIES, NUM_ENTITIES);

    //Create Clusters
    //Set Random Centroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      AlertCluster cluster = new AlertCluster(i);
      alert centroid = alert.createRandomEntity(DIMENSIONS, CATEGORIES);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }

    for(alert e : alerts)
      {
	       System.out.println(e);
      }


    //Print Initial state
    plotClusters();
  }


  private void plotClusters() {
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      AlertCluster c = clusters.get(i);
      c.plotCluster();
    }
  }

  private void clearClusters() {
    for(AlertCluster cluster : clusters) {
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

      ArrayList<alert> lastCentroids = getCentroids();

      //Assign points to the closer cluster
      assignCluster();// <-----------------------------------MARK

      //Calculate new centroids.
      calculateCentroids();//<--------------------------------MARK

      iteration++;

      ArrayList<alert> currentCentroids = getCentroids();//<---------------MARK

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


   private ArrayList<alert> getCentroids() {
    	ArrayList<alert> centroids = new ArrayList<alert>(NUM_CLUSTERS);
    	for(AlertCluster cluster : clusters) {
    		alert aux = cluster.getCentroid();
    		alert point = new alert(aux.getQualities(),aux.getCategories());
    		centroids.add(point);
    	}
    	return centroids;
    }

 private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;

        for(alert point : entities) {
        	min = max;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
            	AlertCluster c = clusters.get(i);
                distance = alert.distanceEuclidean(point, c.getCentroid());
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
    for(AlertCluster cluster : clusters)
      {
      	ArrayList<alert> list = cluster.getAlerts();
      	int n_points = list.size();
      	double[] sum = new double[DIMENSIONS];
      	ArrayList<ArrayList<Integer>> catEntries = new ArrayList<ArrayList<Integer>>();
      	for(int i = 0 ; i < CATEGORIES; i++)
      	  {
      	    catEntries.add(new ArrayList<Integer>());
      	  }

      	for(alert point : list)
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


      	alert centroid = cluster.getCentroid();
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
    for(AlertCluster c : clusters)
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
    DKMeans dkmeans = new DKMeans(NUM_CLUSTERS,NUM_ENTITIES, DIMENSIONS, CATEGORIES);
    dkmeans.init();
    dkmeans.calculate();
    System.out.println("average MSE " + dkmeans.averageMSE());
  }


}
