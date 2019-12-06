import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graph {

  private HashMap<City,HashSet<Edge>> neighboursCache; //defunct
  private HashSet<Edge> masterGraph;
  private HashSet<City> masterCities;


  Graph(){
    masterCities = new HashSet<>();
    masterGraph = new HashSet<>();
    neighboursCache = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader("tcdata.csv"))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(","); //CSV delimited by comma
        masterGraph.add(new Edge(values));
        masterCities.add(new City(values[0]));
      } //CSV of form Departure, Destination, Duration - key (pair) contains route and value is duration
    } catch (IOException e) {
      System.out.println("Read Error");
      e.printStackTrace();
    }
  }

  class City {
    //Need not overwrite equals/hashcode, as equality on mem reference is valid.
    String name;
    City(String inputCity){
      this.name = inputCity;
    }
    HashSet<Edge> getNeighbourEdges(){
      if (neighboursCache.containsKey(this)){ //If already generated, fetch
        return neighboursCache.get(this);
      }
      else { //Else generate and cache
        HashSet<Edge> localEdgeList = new HashSet<>();
        for (Edge edge: masterGraph){
          if (edge.dep.equals(this) || edge.dest.equals(this)){
            //If this city is start or end, then this edge is relevant.
            localEdgeList.add(edge);
          }
        }
        neighboursCache.put(this,localEdgeList);
        return localEdgeList;
      }
    } //defunct
  }

  class Edge  {

    //Two edges are equal if they have same start and destination, can compare on memory-ref except
    //for when generating from two cities i.e. when swapping - in which case use edgeexists.
    //Start-destination pair (in any order) are unique, irrespective of duration.

    //Edge is lifted from CSV - 'dep'-'dest' undirected edge of weight 'duration'
    private City dep;
    private City dest;
    private float duration;
    //Duration is length of single edge

    Edge(String[] v) {
      this.dep = new City(v[0]);
      this.dest = new City(v[1]);
      this.duration = Float.valueOf(v[2]);
    }
    Edge(City a, City b, float duration){
      this.dep = a;
      this.dest = b;
      this.duration = duration;
    }

    Float getDuration(){
      return duration;
    }
    City getDep(){
      return dep;
    }
    City getDest(){ return dest; }

  }

  class virtualEdge extends Edge{ //Edge that does not exist in graph.
    virtualEdge(City a, City b, float duration){
      super(a,b,duration);
    }
  }

  Edge edgeExists(City a, City b, int penalty) {
    for (Edge e : masterGraph) {
      //Check if edge exists, if it does then return it else create new one with penalty.
      if ((e.dest == a && e.dep == b) || (e.dest == b && e.dep == a)) {
        return e;
      }
    }

    //todo does virtualedge need adding to master list?
    return new virtualEdge(a,b,penalty);
  }

  class Solution { //todo annotate
    private ArrayList<Edge> solutionEdges;
    private float prevBest;
    private float length;

    Solution(){
      solutionEdges = new ArrayList<>();
      float prevBest = Float.MAX_VALUE;
      float length;
    }
    void add(Edge k){
      solutionEdges.add(k);
    }
    private float findTotalLength(){
      float total = 0;
      for (Edge edge: solutionEdges){
        total += edge.getDuration();
      }
      return total;
    }

    float getPrevBest(){
      return prevBest;
    }
    float getLength(){
      return length;
    }

    void exchange(City a, City b, int penalty){
      //swap city a and b in the solution.
      prevBest = findTotalLength(); //Keep track of current value to return after.
      int abIndex = -1;
      //Need x-a-b-y -> x-b-a-y
      for (Edge e : solutionEdges) {
        //find edge a-b
        if ((e.dest == a && e.dep == b) || (e.dest == b && e.dep == a)) {
          abIndex = solutionEdges.indexOf(e);
          break;
        }
      }
      if (abIndex==-1){
        throw new RuntimeException(); //Should never reach here - only if edge not found.
      }
        //Find XA and BY
        Edge XA = solutionEdges.get(abIndex-1);
        Edge BY = solutionEdges.get(abIndex+1);
        //Find cities X and Y
        City X = (XA.getDep() == a) ? XA.getDest() : XA.getDep(); //Want city out of X and A that isn't A
        City Y = (BY.getDep() == b) ? XA.getDest() : XA.getDep();
        //Create XB, AY
        Edge XB = edgeExists(X,b,penalty);
        Edge AY = edgeExists(a,Y,penalty);
        //Replace XA with XB and BY with AY
        solutionEdges.set(abIndex-1,XB);
        solutionEdges.set(abIndex+1,AY);
        //Edge AB remains as its only been reversed.
      length = findTotalLength();
    }
  }
}
