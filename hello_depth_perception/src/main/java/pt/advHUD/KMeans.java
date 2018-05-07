package pt.advHUD;


import pt.advHUD.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {
 
 private static final Random random = new Random();
 public final List<Point> allPoints;
 public final int k;
 private Clusters PointClusters; //the k Clusters

 /*@param PointsFile : the csv file for input Points
  * @param k : number of clusters
  */
 public KMeans(ArrayList<Point> points, int k) {
  this.k = k;
  this.allPoints = new ArrayList<Point>(points);
 }

 private Point getPointByLine(String line) {
  String[] xyz = line.split(",");
  return new Point(Double.parseDouble(xyz[0]),
    Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2]));
 }

 /**step 1: get random seeds as initial centroids of the k clusters
  */
 private void getInitialKRandomSeeds(){
  PointClusters = new Clusters(allPoints);
  List<Point> kRandomPoints = getKRandomPoints();
  for (int i = 0; i < k && i < kRandomPoints.size(); i++){
   kRandomPoints.get(i).setIndex(i);
   PointClusters.add(new Cluster(kRandomPoints.get(i)));
  } 
 }
  
 private List<Point> getKRandomPoints() {
  List<Point> kRandomPoints = new ArrayList<Point>();
  boolean[] alreadyChosen = new boolean[allPoints.size()];
  int size = allPoints.size();
  for (int i = 0; i < k && size > 0; i++) {
   int index = -1, r = random.nextInt(size--) + 1;
   for (int j = 0; j < r; j++) {
    index++;
    while (alreadyChosen[index])
     index++;
   }
   kRandomPoints.add(allPoints.get(index));
   alreadyChosen[index] = true;
  }
  return kRandomPoints;
 }
  
 /**step 2: assign Points to initial Clusters
  */
 private void getInitialClusters(){
  PointClusters.assignPointsToClusters();
 }
  
 /** step 3: update the k Clusters until no changes in their members occur
  */
 private void updateClustersUntilNoChange(){
  boolean isChanged = PointClusters.updateClusters();
  while (isChanged)
   isChanged = PointClusters.updateClusters();
 }
  
 /**do K-means clustering with this method
  */
 public List<Cluster> getPointsClusters() {
  if (PointClusters == null) {
   getInitialKRandomSeeds();
   getInitialClusters();
   updateClustersUntilNoChange();
  }
  return PointClusters;
 }
}