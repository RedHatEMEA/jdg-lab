# Core Library-mode API
This tutorial covers the following APIs:
* Basic CRUD operations
* Eviction
* Expiration
* Using a cache store for persistence and warm starts
* Transactions
* Using a custom externalizer
* Using the CDI annotations

## Estimated time:
1 hour

## Vehicle
The tutorial will use TicketMonster, EAP's quickstart "application".  The format will be to walk users through the TicketMonster application, deploy the application on an EAP instance.  The tutorial will then demonstrate the performance of the application via a JMeter script, directly exercising TicketMonster's REST endpoints.  The next steps will be to introduce JDG as a performance-boosting grid on top of the database.  This will involve:

* Configuring JDG
* Applying JDG to TicketMonster via the CDI annotations
* Re-running benchmarks to demonstrate the performance increase - even after restarting the application.

This exercise will require:

* A custom version of TicketMonster built with H2 as a database backend (for easy deployment) rather than Postgres.
* Without any JDG integration



