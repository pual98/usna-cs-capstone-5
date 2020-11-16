import java.util.ArrayList;

public class AlertCluster
{
  public ArrayList<alert> alerts;
  public alert centroid;
  public int id;

  public AlertCluster(int id)
  {
    this.id = id;
    this.alerts = new ArrayList<alert>();
    this.centroid = null;
  }

  public ArrayList<alert> getAlerts() {
    return alerts;
  }

  public void addAlert(alert e) {
    alerts.add(e);
  }

  public void setAlerts (ArrayList<alert> e) {
    this.alerts = e;
  }

  public alert getCentroid() {
    return centroid;
  }

  public void setCentroid(alert centroid) {
    this.centroid = centroid;
  }


  public int getId() {
    return id;
  }

  public void clear() {
    alerts.clear();
  }

  public void plotCluster() {
    System.out.println("[Cluster: " + id+"]");
    System.out.println("[Centroid: " + centroid + "]");
    System.out.println("[alerts: \n");
    for(alert e : alerts) {
      System.out.println(e);
    }
    System.out.println("]");
  }


  public double MSE()
  {
    int n_points = alerts.size();
    if(n_points == 0)
      {
	return 0.0;
      }
    double sum = 0.0;
    for(alert e : alerts)
      {
	sum +=  Math.pow(alert.distanceEuclidean(e,centroid),2);
      }

    return sum/n_points;
  }

}
