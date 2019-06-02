CREATE SCHEMA IF NOT EXISTS ts;

DROP TABLE IF EXISTS ts.db_version;

CREATE TABLE ts.db_version (majorVersion smallint NOT NULL,minorVersion smallint NOT NULL,maintVersion smallint NOT NULL,lastUpdate timestamp);

INSERT INTO ts.db_version(majorVersion, minorVersion, maintVersion, lastUpdate)  VALUES (1, 0, 0, NOW());

DROP TABLE IF EXISTS ts.source_type CASCADE;

CREATE TABLE ts.source_type (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

INSERT INTO ts.source_type(name, description, createtime) VALUES ('BIG-IP', 'BIG-IP source', NOW()),('BIG-IQ', 'BIG-IQ source', NOW()),('BLUE', 'Blue source', NOW());

DROP TABLE IF EXISTS ts.source CASCADE;

CREATE TABLE ts.source (id serial PRIMARY KEY,tenantId integer REFERENCES ts.tenant(id) NOT NULL,type integer REFERENCES ts.source_type(id) NOT NULL,name varchar(64) NOT NULL,description varchar(128),createtime timestamp NOT NULL);

DROP TABLE IF EXISTS ts.administrative_status CASCADE;

CREATE TABLE ts.administrative_status (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

INSERT INTO ts.administrative_status(name, description, createtime) VALUES ('NONE', 'none', NOW()),('ENABLED', 'enabled status', NOW()),('DISABLED', 'disabled status', NOW());

DROP TABLE IF EXISTS ts.availability_status CASCADE;

CREATE TABLE ts.availability_status (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

INSERT INTO ts.availability_status(name, description, createtime) VALUES ('NONE', 'none', NOW()),('Healthy', 'Healthy status', NOW()),('Warning', 'Warning status', NOW()),('Critical', 'Unhealthy', NOW());

DROP TABLE IF EXISTS ts.application CASCADE;

CREATE TABLE ts.application (id serial PRIMARY KEY,sourceId integer REFERENCES ts.source(id),name varchar(64) NOT NULL,description varchar(128),currentAdminStatusId integer REFERENCES ts.administrative_status(id),currentHealthStatusId integer REFERENCES ts.availability_status(id),currentHealthStatusReason varchar(128),location varchar(64) NOT NULL,department varchar(64) NOT NULL,costCenter varchar(64) NOT NULL,owner varchar(64) NOT NULL,createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.state_change(healtheventid serial PRIMARY KEY,createtime TIMESTAMP NOT NULL,appId integer REFERENCES ts.application(id),stateid integer REFERENCES ts.availability_status(id),context varchar(128));

CREATE TABLE IF NOT EXISTS ts.raw_data(createtime TIMESTAMP NOT NULL,tenantId uuid NOT NULL,appId integer REFERENCES ts.application(id),payload jsonb NOT NULL);