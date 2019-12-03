# ChordAkka
A Chord implementation based on Akka

For now it's based on the following example: https://developer.lightbend.com/guides/akka-quickstart-java/index.html

# Prerequisites 
- [Maven](https://maven.apache.org/)
- Java 11 or higher (openjdk)

## How to run
run: `mvn compile exec:exec`

To run central node use parameter: `-Dconfig.resource=/centralNode.conf`

To run regular nodes use parameter: `-Dconfig.resource=/regularNode.conf`

To set your own node id specify node.id=x in the environment variables.

Test run: first run central node then run the regular node

## Branching
Branching will be done according the [Feature Branching Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow).

## Useful links
- https://doc.akka.io/docs/akka/current/typed/guide/index.html

## Code Formatting
Standard IntelliJ java formatting profile
