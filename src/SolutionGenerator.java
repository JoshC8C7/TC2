public class SolutionGenerator {
  //Procedure as follows:
  //1. Read in input, construct graph.
  //2. Form MST, select vertices with odd degree as to create a simpler graph of
  // the 'junction' points only.
  //3. Use Simulated Annealing to generate path:
  // a. generate initial solution randomly
  // b. loop until cool/good-enough solution
  // c. Make small change (exchange two cities)
  // d. Decide whether to move
  // e. decrease temperature, continue
  //When calculating distance between edges, use actual length if one exists else flat penalty.


}
