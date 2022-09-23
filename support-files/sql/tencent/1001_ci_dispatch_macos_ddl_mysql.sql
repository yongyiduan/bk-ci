USE devops_ci_dispatch_macos;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `T_BUILD_HISTORY` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PROJECT_ID` varchar(128) NOT NULL,
  `BUILD_ID` varchar(128) NOT NULL,
  `VM_SEQ_ID` varchar(128) NOT NULL,
  `VM_IP` varchar(64) NOT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `STATUS` varchar(64) DEFAULT NULL,
  `PIPELINE_ID` varchar(128) DEFAULT NULL,
  `VM_ID` int(64) DEFAULT NULL,
  `RESOURCE_TYPE` varchar(64) DEFAULT NULL,
  `CONTAINER_HASH_ID` varchar(128) DEFAULT NULL,
  `EXECUTE_COUNT` int(64) DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_VMIP` (`VM_IP`) USING BTREE,
  KEY `IDX_BUILD_SEQ_START` (`BUILD_ID`,`VM_SEQ_ID`,`START_TIME`),
  KEY `IDX_VM_IP_STATUS` (`VM_IP`,`STATUS`),
  KEY `IDX_STATUS_START_PROJECT_PID_NID` (`STATUS`,`START_TIME`,`PROJECT_ID`,`PIPELINE_ID`,`BUILD_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=744552 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_BUILD_TASK` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BUILD_ID` varchar(128) NOT NULL,
  `VM_SEQ_ID` varchar(128) NOT NULL,
  `VM_IP` varchar(255) NOT NULL,
  `BUILD_HISTORY_ID` bigint(20) NOT NULL DEFAULT '0',
  `PROJECT_ID` varchar(128) DEFAULT NULL,
  `PIPELINE_ID` varchar(128) DEFAULT NULL,
  `VM_ID` int(64) DEFAULT NULL,
  `RESOURCE_TYPE` varchar(64) DEFAULT NULL,
  `CONTAINER_HASH_ID` varchar(128) DEFAULT NULL,
  `EXECUTE_COUNT` int(64) DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `IDX_BUILDID_SEQID` (`BUILD_ID`,`VM_SEQ_ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=744131 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_DEVCLOUD_VIRTUAL_MACHINE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `VM_ID` int(11) NOT NULL,
  `IP` varchar(64) NOT NULL,
  `NAME` varchar(128) DEFAULT NULL,
  `ASSET_ID` varchar(128) DEFAULT NULL,
  `USER` varchar(2048) DEFAULT NULL,
  `PASSWORD` varchar(2048) DEFAULT '',
  `CREATOR` varchar(64) DEFAULT NULL,
  `CREATE_AT` varchar(64) DEFAULT NULL,
  `MEMORY` varchar(64) DEFAULT NULL,
  `DISK` varchar(64) DEFAULT NULL,
  `OS` varchar(64) DEFAULT NULL,
  `CPU` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_VM_ID` (`VM_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=485023 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_MAC_HOST_MACHINE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(64) NOT NULL DEFAULT '',
  `IP` varchar(64) NOT NULL,
  `USER_NAME` varchar(64) NOT NULL,
  `PASSWORD` varchar(64) NOT NULL DEFAULT '',
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `IDX_IP` (`IP`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_VIRTUAL_MACHINE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(128) NOT NULL,
  `IP` varchar(64) NOT NULL,
  `USER_NAME` varchar(64) NOT NULL,
  `PASSWORD` varchar(64) NOT NULL DEFAULT '',
  `MOTHER_MACHINE_IP` varchar(64) NOT NULL,
  `STATUS` varchar(64) NOT NULL DEFAULT 'Unknown',
  `VM_TYPE_ID` int(11) NOT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `STATUS_CHANGE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `IDX_IP` (`IP`) USING BTREE,
  UNIQUE KEY `IDX_NAME` (`NAME`) USING BTREE,
  KEY `IDX_STATUS_START` (`STATUS`,`START_TIME`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_VIRTUAL_MACHINE_TYPE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `SYSTEM_VERSION` varchar(255) NOT NULL,
  `VERSION` varchar(255) NOT NULL,
  `XCODE_VERSION` varchar(2048) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  `DISPLAY` bit(1) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
