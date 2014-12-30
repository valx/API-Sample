CREATE KEYSPACE IF NOT EXISTS datatest
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' :1 };

CREATE TABLE IF NOT EXISTS datatest.events (
  minute int,
  type text,
  number counter,
  PRIMARY KEY (type,minute)
);