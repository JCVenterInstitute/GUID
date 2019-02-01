# GUID

  * [Introduction](#introduction)
  * [Dependencies](#dependencies)
  * [Installation](#installation)
  * [Usage](#usage)

## Introduction

This application generates a unique number that can be used as an identifier.

## Dependencies

* Wildfly 15 - http://wildfly.org/downloads/
* MySQL - https://www.mysql.com/downloads/
* Maven - https://maven.apache.org/download.cgi
* Git - https://git-scm.com/downloads

## Installation

### Build from Git

1. Fork this repository on Github.
2. Clone *your forked repository* (not our original one) to your system.
3. Replace `localhost` with your server name on `<GUID>/resources/src/main/resources/GuidDBInterface.properties`
4. Generate the most recent `GuidDBInterface.ear` into ear module. 

```shell
git clone https://github.com/YOURUSERNAME/GUID.git
cd GUID
mvn package
```

### Create database

1. Run following queries. 

```shell
CREATE DATABASE jcvi_guid;
CREATE TABLE jcvi_guid.guid_block_table (gblock_id int(10) unsigned NOT NULL AUTO_INCREMENT, gblock_first_guid decimal(20,0) unsigned NOT NULL, gblock_last_guid decimal(20,0) unsigned NOT NULL, gblock_namespace_id int(10) unsigned NOT NULL, gblock_creation_comment varchar(4000), gblock_block_size bigint unsigned NOT NULL, gblock_create_date datetime NOT NULL, gblock_created_by varchar(200) COLLATE latin1_bin NOT NULL, gblock_modify_date datetime, gblock_modified_by varchar(200) COLLATE latin1_bin, PRIMARY KEY (gblock_id), CONSTRAINT gblock_first_guid_uk_ind UNIQUE (gblock_first_guid), CONSTRAINT gblock_last_guid_uk_ind UNIQUE (gblock_last_guid), INDEX FK_guid_block_table_namespace_id (gblock_namespace_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE jcvi_guid.guid_namespace_table (gname_id int(10) unsigned NOT NULL AUTO_INCREMENT, gname_namespace varchar(100) COLLATE latin1_bin NOT NULL, gname_creation_comment varchar(4000) COLLATE latin1_bin, gname_create_date datetime NOT NULL, gname_created_by varchar(200) COLLATE latin1_bin NOT NULL, gname_modify_date datetime, gname_modified_by varchar(200) COLLATE latin1_bin, PRIMARY KEY (gname_id), CONSTRAINT gname_namespace_uk_ind UNIQUE (gname_namespace)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE jcvi_guid.max_allocated_guid (maxid_value decimal(20,0) unsigned NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;
ALTER TABLE jcvi_guid.guid_block_table ADD CONSTRAINT FK_guid_block_table_namespace_id FOREIGN KEY (gblock_namespace_id) REFERENCES jcvi_guid.guid_namespace_table (gname_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
INSERT INTO jcvi_guid.guid_namespace_table (gname_id, gname_namespace, gname_creation_comment, gname_create_date, gname_created_by, gname_modify_date, gname_modified_by) VALUES (1, 'GUID_SERVLET', 'general purpose HTTP servlet for GUID access', NOW(), 'GUIDBLOCKSERVERCLIENT', null, null);
INSERT INTO jcvi_guid.max_allocated_guid (maxid_value) VALUES (1000000);
```

> If you prefer to use a different database, update ```jcvi_guid``` with your database name in MaxAllocatedGUIDRetriever and GuidFactory classes. And rebuilt the ear file. 

> If you prefer to start with a different GUID, change ```max_allocated_guid (1000000)``` value with your preferred identifier. 
### Configure Wildfly

1. Extract `<GUID>/mysql.tar` under `<wildfly>/modules/system/layers/base` to add mysql module into wildfly if you have not done already. 

> <wildfly>/modules/system/layers/base/com/mysql/main/module.xml

> <wildfly>/modules/system/layers/base/com/mysql/main/mysql-connector-java-5.1.14-bin.jar

2. Update `<wildfly>/standalone/configuration/standalone.xml` to add mysql driver and GUID datasource.

```shell
<subsystem xmlns="urn:jboss:domain:datasources:5.0">
    <datasources>
        <datasource jndi-name="java:/GUID_DS" pool-name="guid-datasource" enabled="true" use-java-context="false">
            <connection-url>jdbc:mysql://<host>:<port>/<database_name></connection-url>
            <driver>mysql</driver>
            <pool>
                <min-pool-size>1</min-pool-size>
                <max-pool-size>5</max-pool-size>
            </pool>
            <security>
                <user-name>username</user-name>
                <password>password</password>
            </security>
        </datasource>
        <drivers>
            <driver name="mysql" module="com.mysql">
                <driver-class>com.mysql.jdbc.Driver</driver-class>
            </driver>
        </drivers>
    </datasources>
</subsystem>
```
> Change datasource to ```java:/GUID_DS``` for default-bindings under ```urn:jboss:domain:ee:4.0``` if you remove ExampleDS

3. Copy most recent ear file to deployments folder. 

```shell
cp <GUID>/ear/target/GuidDBInterface.ear <wildfly>/standalone/deployments/ 
```

## Usage

1. Start wildfly

```shell
<wildfly>/bin/standalone.sh
```

2. You can get usage details from main page.

```shell
http://localhost:8080/guid/
```

> EXAMPLE: http://localhost:8080/guid/GuidClientServer?Request=GET&Size=1
