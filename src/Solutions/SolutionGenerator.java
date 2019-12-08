package Solutions;
import Solutions.Graph.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import javafx.util.Pair;

public class SolutionGenerator {
  //Procedure as follows:
  //1. Read in input, construct graph.
  //2. Form MST, select vertices with degree > 2 as to create a simpler graph of
  // the 'junction' points only.
  //todo:
  //3. Use Simulated Annealing to generate path:
  // a. generate initial solution randomly
  // b. loop until cool/good-enough solution
  // c. Make small change (exchange two cities)
  // d. Decide whether to move
  // e. decrease temperature, continue
  //When calculating distance between edges, use actual length if one exists else flat penalty.

  private Graph graph;
  private static String filename = "tcdata.csv";
  static private City start;
  HashMap<City, Integer> VerticesDegrees;
  HashSet<City> JunctionDegreeVertices;
  HashSet<Edge> seenEdges;
  Graph mst;
  Graph simplified;


  private SolutionGenerator(){
    graph = new Graph(filename);
  }

  public static void main(String[] args) {
    SolutionGenerator solutionGenerator = new SolutionGenerator();
    start = solutionGenerator.graph.new City("START");
    solutionGenerator.prim();


  }


  private void prim() {

    PriorityQueue<Pair<City, Float>> mst_vertices = new PriorityQueue<>(pairComparator);
    HashMap<City, City> come_from = new HashMap<>(); //key has come from value v->k

    boolean firsttozeroflag = true;
    for (City c : graph.getCities()) {
      if (firsttozeroflag) {
        mst_vertices.add(new Pair<City, Float>(c, 0.0f));
        come_from.put(c, start);
        firsttozeroflag = false;
      } else {
        mst_vertices.add(new Pair<City, Float>(c, Float.MAX_VALUE));
      }
    }

    while (!mst_vertices.isEmpty()) {
      Pair<City, Float> vertex = mst_vertices.poll(); //remove vertex from list
      for (Graph.Edge k : vertex.getKey().getNeighbourEdges()) { //for all neighbours
        for (Pair<City, Float> m : mst_vertices) {
          if (m.getKey().equals(k.getDest())) {
            if (m.getValue() > vertex.getValue() + k
                .getDuration()) { //if current value more than that achievable using new info
              come_from.put(m.getKey(), vertex.getKey());
              mst_vertices.add(new Pair<City, Float>(k.getDest(),
                  k.getDuration() + vertex.getValue())); //add new version to PQ
              mst_vertices.remove(m); //remove old version from PQ to complete update
            }
            break;
          }
        }
      }
    }

    Graph mst = new Graph();
    for (Map.Entry<City, City> entry : come_from.entrySet()) {
      if (!entry.getValue().equals(start)) {
        mst.addEdge(graph.edgeFinder(entry.getValue(), entry.getKey()));
      }
    }
  }

    //MST is complete.

  private static Comparator<Pair<City, Float>> pairComparator = new Comparator<Pair<City, Float>>() {
    public int compare(Pair<City, Float> x, Pair<City, Float> y) {
      return Float.compare(x.getValue(), y.getValue());
    }
  };



  private void extractSimplified() {

    VerticesDegrees = new HashMap<>();
    JunctionDegreeVertices = new HashSet<>();
    for (City vertex : mst.getCities()) {
      VerticesDegrees.put(vertex, 0);
    }
    for (Graph.Edge edge : mst.getEdges()) {
      VerticesDegrees.put(edge.getDep(), VerticesDegrees.get(edge.getDep()) + 1);
      VerticesDegrees.put(edge.getDest(), VerticesDegrees.get(edge.getDest()) + 1);
    }

    for (Map.Entry<City, Integer> entry : VerticesDegrees.entrySet()) {
      if (entry.getValue() > 2) {
        JunctionDegreeVertices.add(entry.getKey());
      }
    }

    //Now Have MST & Junction vertices. Need to find distances between node junctions.
    //Procedure:
    // Iterate over junction vertices,
    // walk down mst from each vertex until another junction vertex reached.
    // Add the sum of edges along this as new edge.
    // As going along, mark edges of mst that have been covered as to not re-cover them.

    seenEdges = new HashSet<>();
    simplified = new Graph(JunctionDegreeVertices);

    for (City c: JunctionDegreeVertices){
      for (Edge e: c.getNeighbourEdges()){
        City neighbour = (e.getDep() == c) ? e.getDest() : e.getDep(); //find where it leads
        //First node will always have at least 1 unseen neighbour.
        mstWalkerRecursive(c,neighbour,e,0f);
      }
    }
    //Have populated simplified graph with lengths.
    //Simplified is now graph of junction nodes and distances between them.

  }

  private void mstWalkerRecursive(City origin, City c, Edge e, Float total) {
    if (!seenEdges.contains(e)) {
      seenEdges.add(e); //If outward edge hasnt already been traversed
      total += e.getDuration(); //add it to walk
      City neighbour = (e.getDep() == c) ? e.getDest() : e.getDep(); //find where it leads
      if (JunctionDegreeVertices.contains(neighbour)) { //if this destination is a junction
        simplified.addEdge(simplified.new virtualEdge(origin, neighbour, total));
        //then add edge origin->this neighbour with length accumulated along walk.
      } else { //else node is not a junction, so keep walking
        Edge nextEdge = null; //Finding the edge pointing out of the node, not towards the one arrived at.
        for (Edge f : neighbour.getNeighbourEdges()) {
          if (f.getDest() != c && f.getDep() != c) {
            nextEdge = f; //find the edge pointing out. Only needs to iterate max twice.
          }
        }
        mstWalkerRecursive(origin, neighbour, nextEdge, total);
        //Make recursive call, passing origin junction, next node is neighbour, nextEdge as
        //edge running out, and running total.
      }
    } else {
      if (c.getNeighbourEdges().size() == 1) {
        //if only 1 neighbour, must have come from it. So cannot progress further.
        //Treat this node as a junction. Add edge origin->current - neighbour will be the prior node.
        simplified.addEdge(simplified.new virtualEdge(origin, c, total));
      }
      //else all edges out of here have been seen and edges already added.
    }
  }


  private void annealing(){

  }

}
