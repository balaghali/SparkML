CREATE SCHEMA IF NOT EXISTS ts;

CREATE TABLE IF NOT EXISTS ts.db_version (majorVersion smallint NOT NULL,minorVersion smallint NOT NULL,maintVersion smallint NOT NULL,lastUpdate timestamp);

CREATE TABLE IF NOT EXISTS ts.source_type (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.source (id serial PRIMARY KEY,tenantId uuid NOT NULL,type integer REFERENCES ts.source_type(id) NOT NULL,name varchar(64) NOT NULL,description varchar(128),createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.administrative_status (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.availability_status (id serial PRIMARY KEY,name varchar(32) UNIQUE,description varchar(64),createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.application (id serial PRIMARY KEY,sourceId integer REFERENCES ts.source(id),name varchar(64) NOT NULL,description varchar(128),currentAdminStatusId integer REFERENCES ts.administrative_status(id),currentHealthStatusId integer REFERENCES ts.availability_status(id),currentHealthStatusReason varchar(128),location varchar(64) NOT NULL,department varchar(64) NOT NULL,costCenter varchar(64) NOT NULL,owner varchar(64) NOT NULL,createtime timestamp NOT NULL);

CREATE TABLE IF NOT EXISTS ts.state_change(healtheventid serial PRIMARY KEY,createtime TIMESTAMP NOT NULL,appId integer REFERENCES ts.application(id),stateid integer REFERENCES ts.availability_status(id),context varchar(128));

CREATE TABLE IF NOT EXISTS ts.raw_data(createtime TIMESTAMP NOT NULL,tenantId uuid NOT NULL,appId integer REFERENCES ts.application(id),payload jsonb NOT NULL);

INSERT INTO ts.administrative_status(id ,name, description, createtime) VALUES (1, 'NONE', 'none', NOW()),(2, 'ENABLED', 'enabled status', NOW()),(3,'DISABLED', 'disabled status', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO ts.source_type(id,name, description, createtime) VALUES (1,'BIG-IP', 'BIG-IP source', NOW()),(2,'BIG-IQ', 'BIG-IQ source', NOW()),(3,'BLUE', 'Blue source', NOW()) ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS ts.availability_status (id serial PRIMARY KEY, name varchar(32) UNIQUE, description varchar(64),createtime timestamp NOT NULL);
 
INSERT INTO ts.availability_status(id , name, description, createtime) VALUES (1, 'NONE', 'none', NOW()),(2 , 'Healthy', 'Healthy status', NOW()),(3, 'Warning', 'Warning status', NOW()),(4 , 'Critical', 'Unhealthy', NOW()) ON CONFLICT (id) DO NOTHING;

INSERT INTO ts.source(id , tenantId , type , name , description , createtime) VALUES 	(1,md5(random()::text || clock_timestamp()::text)::uuid,1 ,'help.f5net.com' , 'NA' , NOW()),(2,md5(random()::text || clock_timestamp()::text)::uuid,1,'unused-device','NA',NOW()),(5,md5(random()::text || clock_timestamp()::text)::uuid,1,'p-10-145-114-207.mgmt.pdsea.f5net.com','NA',NOW()),(6,md5(random()::text || clock_timestamp()::text)::uuid,1,'ShefaliBigIP13.f5net.com','NA',NOW())

INSERT INTO ts.application(id, sourceid, name, description, currentadminstatusid, currenthealthstatusid, currenthealthstatusreason, location, department, costcenter, owner, createtime) VALUES (3 ,5,'service3.app','NA',2,3,'offline','missingdata','IT',503,'Paula',NOW()),(2,6,'service2.app','NA',2,3,'offline','missingdata','IT',503,'Paula',NOW()),(1,1,'testapp',' ',2,3,'Not available','Seattle','Marketing',100,'Paula',NOW())

INSERT INTO ts.state_change(createtime, appid, stateid, context) VALUES (NOW(),3,3,'missing data'),(NOW(),2,3,'missing data'),(NOW(),1,3,'missing data') ON CONFLICT (healtheventid) DO NOTHING;