JDG Lab - TicketMonster + X-Site
================================

This lab will demonstrate the use of JBoss Data Grid inside a demo application. In this case we'll be using the JBoss demo application *TicketMonster*. The original version of this application can be found [here](http://www.jboss.org/jdf/examples/ticket-monster/tutorial/WhatIsTicketMonster/).

In the previous lab, you have used JBoss Data Grid in, so called, *Server Mode*. The Grid was configured on the JDG server and you used the HotRod protocol to connect to the Grid. In this lab, we will show you how to run JBoss Data Grid in *Library Mode*. This mode implies that the Grid runs inside the same JVM as the application itself. Furthermore, configuration and Life Cycle Management (e.g. starting and stopping the cache containers) is the responsibility of the application. 

In this lab we will use a slightly altered version of TicketMonster to allow you to implement a simple JDG integration. Furthermore, additional pages have been added to the TicketMonster UI which are able to display the state (entries) of the cache. This will help to get a feeling of the semantics of JDG during this lab session.

In this lab, we will focus on the CartService. In the TicketMonster application that we will use today, this service uses database persistence via JPA to store the created Carts. We will change this implementation and will use the Data Grid for storage of these entities instead. When this functionality has been implemented, we will elaborate on this setup by defining cache entry expiration and eviction configurations and finally by creating a 2 * 2-node cluster to demonstrate the new cross-site (x-site) replication functionality of JBoss DataGrid.

Lab 1: Using JBoss Data Grid
----------------------------
*Time:* 30 minutes

*Goal:* Alter the implementation of the *CartService* service so it will store its data in the Grid instead of a database. Implement a *CacheManager* which forms the entry point for cache operations:

*Installation:*

1.  Unzip the */ticket-monster-lab/lab1-sources.zip* on your flash drive into any directory.

2.  Start a clean installation of JBoss Developer Studio 6 (or any IDE with Maven integration). If you have not yet installed JBDS, a JBoss Developer Studio 6 installation file is included on your flash drive.

3.  Import the *lab1-sources* root directory, which we unzipped in step 1, into your JBDS workspace. This directory contains a Maven POM file. We will use this POM file to import the project into JBoss Developer Studio:

		* File -> Import -> Maven -> Existing Maven Project.
		* Click 'Browse' and select the 'lab1-sources' directory. 
		* Select the POM file to import the ticket-monster project into the workspace.

4.  Next, unzip the *jboss-eap-6.0.1.zip* which is provided on your flash drive and unzip it in any location. This zip file contains a vanilla jboss-eap-6.0.1 with 4 additional *standalone* directories (which we will use when demonstrating X-Site replication) and 4 custom startup scripts to start these nodes.

5.  Build the ticket-monster project by navigating into the directory in which you unzipped *lab1-sources* in step 1 and run `maven clean install`. Depending on the amount of Maven libraries that need to be downloaded this can take some time.
6.  Start the *standalone-lon-one* JBoss EAP 6.0.1 server with the default (*standalone.xml*) profile. We have provided a pre-configured startup script: *{jboss-eap-6.0.1}/bin/start\_standalone-lon-one.sh*. This script starts the first node of the *London* site (hence the name *standalone-lon-one*).

7.  Start the JBoss EAP 6.0.1 CLI: *{jboss-eap-6.0.1}/bin/jboss-cli.sh*

8.  Connect the CLI to the application server by executing the `connect` command in the CLI.

9.  Deploy the ticket-monster application onto the server using the CLI. We will deploy the WAR file which has been stored in our Maven repository during the Maven build executed in step 5: `deploy ~/.m2/repository/org/jboss/jdf/examples/ticket-monster-lab1/2.1.3-SNAPSHOT/ticket-monster-lab1-2.1.3-SNAPSHOT.war`

This should deploy *ticket-monster* correctly into the JBoss Enterprise Application Platform.

We can now test the ticket-monster app. Navigate to <http://localhost:8080/ticket-monster> and try to order some tickets.

Now that we have setup the ticket-monster application, we can start integrating the JBoss Data Grid system with the application. The version of ticket-monster you just deployed uses an in-memory HSQL DB for all storage. In this lab, we will change the default *org.jboss.jdg.example.ticketmonster.service.CartStore* implementation from the *JpaBasedCartStore* to a *CacheBasedCartStore*.

The code and configuration files required in the following steps are all provided in the *lab1-sources* project in the *src/main/lab1* directory. For time reasons, all source files required to integrate JBoss Data Grid have already been created and only need to be copied to the correct directory. However, feel free to implement your own version of, for example, the *CacheManagerProducer*.

*Description:*

1.  First, we need a component that loads the JBoss Data Grid configuration and produces a JDG *EmbeddedCacheManager* that we can use in our application. Copy the *src/main/lab1/org/jboss/jdf/example/ticketmonster/util/CacheManagerProducer.java* to the *src/main/java* directory. This class is a CDI producer for the *EmbeddedCacheManager*. Note that it references an *infinispan.xml* configuration file, which is the JDG configuration file for the Cache Container. This file has already been pre-configured and is available in *src/main/resources/infinispan*. 
(Note that this file is also already pre-configured for cross-site replication, which we will discuss and demonstrate in lab3).

2.  We will copy the *src/main/lab1/org/jboss/jdf/example/ticketmonster/service/CacheBasedCartStore.java* to the *src/main/java* directory. This is the JBoss DataGrid based implementation of the *CartStore* interface and will replace the *JpaBasedCartStore*.
3.  Now we will copy the RESTful *CacheService.java*. This class provides a simple RESTful interface into the JDG *Cache*, which is used by our customized ticket-monster UI to show the state of the cache. Copy *src/main/lab1/org/jboss/jdf/example/ticketmonster/rest/CacheService.java* to the *src/main/java* directory.
4.  Next, we need to copy the *CacheStartListener.java*. This listener is responsible for starting the cache when the web-application is started. Copy *src/main/lab1/org/jboss/jdf/example/ticketmonster/web/listener/CacheStartListener.java* to the *src/main/java* directory.
5.  Enable the listener in the web.xml by adding the following XML snippet at the top of the *src/main/webapp/WEB-INF/web.xml* file:

		<context-param>
    		<param-name>startCachesOnStartup</param-name>
    		<param-value>true</param-value>
  		</context-param>
  		<listener>
    		<description>CacheStartListener</description>
    		<listener-class>org.jboss.jdf.example.ticketmonster.web.listener.CacheStartListener</listener-class>
  		</listener>

6.  Finally, we need to configure our *CartService* to use the *CacheBasedCartStore* instead of the *JpaBasedCartStore*. We do this by changing the *@JpaBased* qualifier on the *CartStore* in the *org.jboss.jdf.example.ticketmonster.rest.CartService* into *@CacheBased* (note that the *@CacheBased* CDI Qualifier was already present in the project and that the *CacheBasedCartStore* is annotated with this qualifier annotation).


Rebuild ticket-monster by executing a `maven clean install` in the *lab1-sources* folder. Deploy the new *ticket-monster* application onto the JBoss Enterprise Application Platform using the command `deploy ~/.m2/repository/org/jboss/jdf/examples/ticket-monster-lab1/2.1.3-SNAPSHOT/ticket-monster-lab1-2.1.3-SNAPSHOT.war --force` (note the *--force* option which tells the server to overwrite the previous deployable).

Navigate to <http://localhost:8080/ticket-monster> and order some tickets. After placing the tickets into the cart, but before checking out the order, navigate to the custom *Cart Cache* view in the ticket-monster UI. If all has been configured correctly, the view should show the cache entry of the cart.

We have successfully changed the implementation of our *CartStore* from JPA to JBoss Data Grid. In the next section we will configure our cache in more depth.

Lab 2: Expiration and Eviction
------------------------------

*Time:* 10 minutes 

*Goal:* Configure a global expiration on the cache. Observe that cache entries will be removed after the expiration timeout. Configure an expiration time on the CartService's *put* operation. Observe that this expiration time overrides the expiration time set globally. Configure an *eviction* strategy on the cache. Observe that cache entries get evicted from the cache when the eviction policy is triggered.

*Installation:*

There are 2 options to start this lab:

a.  Continue with the Lab 1 environment. Note that when you continue with lab 1, you will have to deploy the *ticket-monster-lab1-2.1.3-SNAPSHOT.war* WAR file with the *--force* option to overwrite the previous deployment.

b.  Setup a fresh environment using the provided *lab2-sources.zip*. Note that when you start with *lab2-sources*, you'll have to undeploy the *ticket-monster-lab1-2.1.3-SNAPSHOT.war* WAR file from the JBoss EAP to prevent conflicts with the new *ticket-monster-lab2-2.1.3-SNAPSHOT.war* WAR file. You can use the command `undeploy ticket-monster-lab1-2.1.3-SNAPSHOT.war`.


### Expiration 

*Description:*
Now that we have converted ticket-monster to use a JDG Cache instead of a database, we want to add some additional semantics. First of all, our carts should become invalid after a certain time period. As JDG provides support for *expiration* of cache entries, we can easily implement this feature. Defining the expiration time for cache entries can be done in 2 different ways. One can define the expiration configuration globally in the Infinispan/JDG configuration file, or one can specify the expiration time when putting a value in the cache. We will explore the first option first.

1.  Open the Infinispan/JDG configuration file *src/main/resources/infinispan/infinispan.xml* and add the following XML snippet in the `<namedCache name=TICKETMONSTER_CARTS>` section of the configuration file, in between the `</clustering>` and `<sites>` XML elements.

		<!-- Configure the expiration of the cache entries -->
		<!-- Entries are configured to be removed after 30 seconds. -->
		<expiration lifespan="30000" reaperEnabled="true" wakeUpInterval="5000"/>

2.  Build the project using `mvn clean install` and deploy the new ticket-monster WAR file onto the JBoss EAP.

3.  Go through the ticket ordering process and add tickets to the cart, but don't checkout the order (as this would delete the Cart cache entry from the cache). Open the *Cart Cache* view and observe that the cache contains one entry. Wait 30 seconds (the lifespan of the cache entry which we configured in *infinispan.xml*). Refresh the view. The cache entry should have been removed.


	We will now show how to set the expiration time on a cache entry when putting it in the cache, overriding the option we've set in the global configuration.

4.  In the *org.jboss.jdf.example.ticketmonster.service.CacheBasedCartStore.saveCart(Cart cart)* method, add expiration arguments to the `put` operation on the cache:


		this.cartsCache.put(cart.getId(), cart, 60, TimeUnit.SECONDS);
	
	This will set the expiration of the entry to one minute. 
		

5.  Build the project using `mvn clean install` and deploy the new ticket-monster WAR file onto the JBoss EAP

6.  Again, go through the same ticket ordering process. Observe that the cache entry is now automatically removed after a minute instead of 30 seconds, thus overriding the global expiration setting in the configuration file.


### Eviction

Another way to automatically remove entries from the cache is through a process called *eviction*. Eviction differs from expiration in the sense that expiration semantics are time-based, where eviction is usually based on the amount of data in the cache and is used to not to run out of memory. Eviction is typically used in conjunction with a cache store, so that entries are not permanently lost when evicted, since eviction only removes entries from memory and not from cache stores or the rest of the cluster. 
Due to time reasons, we will however not use cache store in this lab-session. We will configure the LRU (Least Recently Used) eviction strategy and set the maxEntries to 3. We can than open a number of different carts in the TicketMonster system and observe that the maximum number of entries in the cache is 3.

1.  Disable the expiration code and configuration applied in the previous steps of this lab. This is to prevent cache entries from being removed from the cache due to expiration instead of eviction.

2.  Enable eviction by adding the following configuration setting to *src/main/resources/infinispan/infinispan.xml*, in the `<namedCache name=TICKETMONSTER_CARTS>` section in between the `</clustering>` and `<sites>` XML elements:

		<eviction strategy="LRU" maxEntries="3"/>

3.  Build the project using `mvn clean install` and deploy the application on JBoss EAP.

4.  Again, put some tickets in a cart, but don't proceed to checkout. A new Cart entry gets created every time you enter the *Select Tickets* page, so the easiest way to generate a number of carts is to reload the *Select Tickets* page after you put some tickets in your cart.

5.  Observe that we never exceed 3 entries in the cache by examining the *Cart Cache* view.


Lab3: Cross-Site (X-Site) Replication
-------------------------------------

*Time:* 20 minutes

*Goal:* Demonstrate JBoss Data Grid Cross-Site (X-Site) replication in a 2 * 2-node cluster.


*Installation:*
There are 2 options to start this lab:

a. Continue with the Lab 2 environment.

b. Setup a fresh environment using the provided *lab3-sources.zip*.	


*Description:*

In this section we will configure the Infinispan/JBoss DataGrid system for cross-site (x-site) replication. X-Site replication is a technique whether the grid's content is replicated between individual clusters in different data-centers in different geographical locations.

This example requires us to run a cluster of JDG nodes. We will be using 2 2-node clusters running on a single machine. To accomplish, we multi-home the machine (giving it multiple loopback ip-addresses), which allows us to run each JDG system on its own ip-address, preventing port conflicts.

On Mac OS-X, you can use the following command to assign multiple ip-address to the loopback device:

		ifconfig lo0 alias 127.0.0.2
		ifconfig lo0 alias 127.0.0.3
		ifconfig lo0 alias 127.0.0.4


In this example we will be using 2 locations, London (LON) and New York City (NYC). The provided JBoss EAP installation that you installed in the first lab-session has already been prepared to run multiple JBoss EAP servers. In the *jboss-eap-6.0* directory you will find 4 custom *standalone* directories:
		
		standalone-lon-one
		standalone-lon-two
		standalone-nyc-one
		standalone-nyc-two

These represent the 2 nodes in London and the 2 nodes in NYC. The *jboss-eap-6.0/bin* directory contains prepared startup scripts that start these nodes with the correct configuration options, e.g. ip-address, JGroups site configuration, etc.


The ticket-monster application has already been setup to support x-site replication. This is configured via the files provided in *src/main/resources/infinispan*. We will give a short description of these config files:
		infinispan.xml: main Infinispan configuration file.
		LON.xml: JGroups configuration for the local London cluster.
		NYC.xml: JGroups configuration for the local New York cluster.
		relay2.xml: RELAY2 configuration file referenced by both the LON.xml and NYC.xml
		global.xml: The global JGroups configuration for RELAY2, referenced by relay2.xml

To see X-Site replication in action we need to:

1.  Do a `mvn clean install` in the *lab3-sources* directory to build the TicketMonster web application, which has been preconfigured for x-site replication. (this only applies if you're starting with a clean *lab3-sources* directory. If you're continueing from *lab1-sources* or *lab2-sources*, please execute a `mvn clean install` in one of those directories).

2.  Start the 4 JBoss EAP nodes (lon-one, lon-two, nyc-one and nyc-two) via the provided startup scripts in *jboss-eap-6.0.1/bin* directory

3.  Start the JBoss CLI via *jboss-eap-6.0.1/bin/jboss-cli.sh*

4.  Connect the CLI to *london-one*: `connect 127.0.0.1:9999`.

5.  Deploy the ticket-monster application: `deploy ~/.m2/repository/org/jboss/jdf/examples/ticket-monster-lab3/2.1.3-SNAPSHOT/ticket-monster-lab3-2.1.3-SNAPSHOT.war` (if a previous version of *ticket-monster* was already deployed on this server, please undeploy that version first using the CLI `undeploy` command).

6.  Observe that on *london-one* 2 JGroups channels are started, one for the local Infinispan/JDG cluster and one for X-Site replication.

7.  Connect the CLI to *london-two* by calling: `connect 127.0.0.2:9999`

8.  Deploy the ticket-monster app to *london-two*. Observe that only one channel is started, the local London JGroups channel. The reason for this is that there is already a RELAY2 site-master for the London cluster (i.e. *london-one*) who has already started the global channel.

9.  Repeat for the 2 NYC nodes on 127.0.0.3 and 127.0.0.4

10.  Open the ticket-monster application on a London node and add some tickets to a Cart, but don't checkout.

11.  Open the *Cart Cache* view in the London node, observe that the Cart has been added to the cache.

12.  Open the *Cart Cache* view in on one of the New York nodes, observe that the Cart has been replicated to the New York cluster.


