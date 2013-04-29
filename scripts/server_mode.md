# Server-mode

Demo setting up a JDG cluster using Hot Rod.  Write a simple "hello world" webapp to store data in the grid via the Hot Rod Java client.  Inspect the grid using JON.  Inspect the grid using the CLI.  Demonstrate failover (stopping nodes, restarting, etc).

## Steps:

* Explain architecture, using block diagrams, etc.  Set up a cluster of 4 nodes.
* Talk through the configuration.  Distribution, num owners, segments, state transfer, etc.  Enable JMX for monitoring.
* Start up cluster.
* Bring up IDE, write simple hello world webapp, Maybe a game - [number guessing game](http://www.coderanch.com/t/542340/java/java/number-guessing-game)? Or a better, more interactive app?
* Webapp to use the RemoteClient.  Build and deploy in EAP instance.
* Fire up JON and monitor the grid.  Demo stats, etc.
* Fire up CLI, inspect contents of the grid.
* Shut down nodes, restart, demonstrate failover.

Duration: 1 hour.  

