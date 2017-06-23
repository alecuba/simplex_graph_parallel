Given a energy distribution network (electrical) with different costs and limitations on each section, this fully framework calculates the most optimal solution with less cost objective using the following approach:

-Given a network based on graphs saved on a BD like cassandra (distributed nosql BD) and a current client consumption with the assigned route (previously)
-When a client consumption is updated a first algorithm is used in order to search all posible ways to get from the entry of the distribution network graph to reach each client.
-This possibles routes with limits and costs is formalized in a lineal problem in order to be solved using a simplex algorithm
-The final result is the most optimal low cost for current sitation.

The simplex algorithm is boosted by using OpenMP and MPI bindings for java.

The BD is a nosql distributed in order to get more performance.

Few images..
Initial problem graph.
![Problem graph](energy_graph.jpg?raw=true "Problem graph")

JOMP parallel library stack
![JOMP java stack](jomp_stack.jpg?raw=true "JOMP java stack")

General overview code running
![Code_running](code_running.png?raw=true "Code_running")

Intermediate simplex table result
![simplex_iteration](simplex_iteration.png?raw=true "simplex_iteration")

Parallel speedup
![Parallel speedup](speedup.png?raw=true "Parallel speedup")

