An example of a simple and scalable API.
2 Possible operations:
- write request: ti represents the initial timestamp,  ty is the event type
- read: request: t1 represents the initial time, t2 the final time, ty is the type

## Installation
1. unzip and move into the dataTest folder
2. create table in cassandra with cql file (i.e. run /[Cassandra-Home]apache-cassandra-2.1.2/bin/cqlsh -f cassandra.cql);
3. in command line: sbt run
4. from browser: go to http://127.0.0.1:8080/write?ti=1234567890&ty=ty5
it should show: "write message sent"
5. http://127.0.0.1:8080/read?t1=0&t2=9999999999&ty=ty5
should show: {
  "type": "ty5",
  "minute": "20576131",
  "number": "1"
}

## Technologies
Spray-Akka: for building the RESTful layer, easily integrable with Akka;
Akka: for building the workers layer. I used the routing and remoting principles to make a scalable application. I used 2 different Akka Actor System (for simplicity the are run on the same machine now) to show that the application could easily be distributed on more hosts.
Cassandra: to manage the atomic updates of counter; the partition properly the records.
