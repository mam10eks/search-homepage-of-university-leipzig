# Visualize Events of the Search-Engine

This is a small python example script to show how a consumer could read and process
all search-engine-events which are persisted in kafka in a batch process.
This consumer retrieves all events, and transforms them into a graph structure which is
persisted in neo4j.

An subgraph of a tiny user session which could then be seen in the neo4j ui is:
![Screenshot of a User-Session-Graph](Screenshot from 2018-04-02 16-13-09.png "A Screenshot of a User-Session-Graph")
