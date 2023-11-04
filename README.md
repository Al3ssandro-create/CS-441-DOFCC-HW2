
# Graph Analysis Framework using Spark GraphX
Alessandro Martinolli  
E-mail: [amart409@uic.edu](amart409@uic.edu)  
Video: [https://youtu.be/HJ3P1rcW6-U](https://youtu.be/HJ3P1rcW6-U)
## Overview 
This Scala project provides a framework for performing complex graph analysis using Apache Spark's GraphX library. It is designed to facilitate the execution of graph algorithms, such as random walks and SimRank similarity computations, in a distributed computing environment.

## Features

- Custom graph elements definition (`CustomNode`, `CustomEdge`)
- Graph utilities for extracting valuable nodes
- Random walk implementation for graph exploration
- SimRank algorithm for node similarity computation
- Graph deserialization from JSON format

## Prerequisites

- Apache Spark
- SBT (Scala Build Tool) or similar build tool
- Scala
- An environment that supports Spark job execution

## Installation

To set up the project, clone the repository to your local machine or server where Spark is installed:

```sh
git clone https://github.com/Al3ssandro-create/CS-484-HW2
cd your-project-directory
```
Build the project using SBT:
```sh
sbt clean compile
```
Generate the jar file
```sh
sbt assembly
```
## Usage

To run the application, submit the job to Spark using the `spark-submit` command:

```sh
spark-submit --class com.lsc.Main target/scala-2.12/Cloud-jar.jar ./input/transformedGraph.json ./input/transformedGraph.perturbed.json
```
Change ./input/transformedGraph.json ./input/transformedGraph.perturbed.json with the name of your json file.


Compile and test the program using SBT
```sh
sbt clean test
```
Replace `your-spark-master-url` with your Spark master's URL and `your-jar-file.jar` with the name of the assembled JAR file of your project.
## Structure

- `CustomEdge.scala`: Defines the `CustomEdge` class representing the edges in the graph.
- `CustomGraph.scala`: Defines the `CustomGraph` class representing the graph structure.
- `CustomNode.scala`: Defines the `CustomNode` class representing the nodes in the graph.
- `GraphUtils.scala`: Provides utilities for working with graphs, such as extracting valuable nodes.
- `Main.scala`: The main entry point of the application, coordinating the graph processing tasks.
- `RandomWalk.scala`: Implements the random walk algorithm on the graph.
- `ReadGraph.scala`: Contains methods to deserialize graphs from JSON files or URLs.
- `SimRank.scala`: Implements the SimRank algorithm for calculating node similarity.
## Additional Information
- The program includes commented sections that indicate prior workflows and logic. These sections can be uncommented based on requirements.
- Logging is facilitated through slf4j, ensuring detailed logs regarding the status of graph loading, shard creation, and MapReduce job execution.
- If you need to convert your file into a json refer to: []