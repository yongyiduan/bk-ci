USE devops_ci_process;
SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS ci_process_schema_update;

DELIMITER <CI_UBF>

CREATE PROCEDURE ci_process_schema_update()
BEGIN

    DECLARE db VARCHAR(100);
    SET AUTOCOMMIT = 0;
    SELECT DATABASE() INTO db;

	IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
                    AND COLUMN_NAME = 'PROJECT_NAME') THEN
        ALTER TABLE T_PIPELINE_WEBHOOK
			ADD COLUMN `PROJECT_NAME` VARCHAR(128) DEFAULT NULL;
    END IF;

	IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
                    AND COLUMN_NAME = 'TASK_ID') THEN
        ALTER TABLE T_PIPELINE_WEBHOOK
			ADD COLUMN `TASK_ID` VARCHAR(34) DEFAULT NULL;
    END IF;

	IF NOT EXISTS(SELECT 1
              FROM information_schema.statistics
              WHERE TABLE_SCHEMA = db
                AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
                AND INDEX_NAME = 'IDX_PROJECT_NAME_REPOSITORY_TYPE') THEN
        ALTER TABLE T_PIPELINE_WEBHOOK
			ADD INDEX `IDX_PROJECT_NAME_REPOSITORY_TYPE` (`PROJECT_NAME`, `REPOSITORY_TYPE`);
    END IF;

    IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_BUILD_HISTORY'
                        AND COLUMN_NAME = 'WEBHOOK_INFO') THEN
            ALTER TABLE T_PIPELINE_BUILD_HISTORY
                ADD COLUMN `WEBHOOK_INFO` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
        END IF;

        IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_BUILD_HISTORY'
                        AND COLUMN_NAME = 'IS_RETRY') THEN
            ALTER TABLE T_PIPELINE_BUILD_HISTORY
                ADD COLUMN `IS_RETRY` BIT(1);
        END IF;

    IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_BUILD_HISTORY'
                        AND COLUMN_NAME = 'ERROR_INFO') THEN
        ALTER TABLE T_PIPELINE_BUILD_HISTORY
            ADD COLUMN `ERROR_INFO` TEXT DEFAULT NULL;
    END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_PIPELINE_SETTING'
                    AND COLUMN_NAME = 'MAX_CON_RUNNING_QUEUE_SIZE') THEN
        ALTER TABLE T_PIPELINE_SETTING ADD COLUMN `MAX_CON_RUNNING_QUEUE_SIZE` int(11) DEFAULT '50';
    END IF;

    IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_BUILD_TASK'
                        AND COLUMN_NAME = 'ATOM_CODE') THEN
        ALTER TABLE T_PIPELINE_BUILD_TASK
            ADD COLUMN `ATOM_CODE` VARCHAR(128) DEFAULT NULL;
    END IF;
	
	IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_BUILD_HISTORY'
                        AND COLUMN_NAME = 'BUILD_MSG') THEN
        ALTER TABLE T_PIPELINE_BUILD_HISTORY 
			ADD COLUMN `BUILD_MSG` VARCHAR(255); 
    END IF;
	
	IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
                        AND COLUMN_NAME = 'DELETE') THEN
        ALTER TABLE T_PIPELINE_WEBHOOK 
			ADD COLUMN `DELETE` BIT(1) DEFAULT 0; 
    END IF;
	
	IF NOT EXISTS(SELECT 1
				  FROM information_schema.statistics
				  WHERE TABLE_SCHEMA = db
					AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
					AND INDEX_NAME = 'UNIQ_PIPELINE_ID_TASK_ID') THEN
		ALTER TABLE T_PIPELINE_WEBHOOK ADD UNIQUE `UNIQ_PIPELINE_ID_TASK_ID`(`PIPELINE_ID`, `TASK_ID`);
	END IF;
	
	IF EXISTS(SELECT 1
				  FROM information_schema.statistics
				  WHERE TABLE_SCHEMA = db
					AND TABLE_NAME = 'T_PIPELINE_WEBHOOK'
					AND INDEX_NAME = 'UNIQ_KEY') THEN
		ALTER TABLE T_PIPELINE_WEBHOOK DROP INDEX `UNIQ_KEY`;
	END IF;

    IF EXISTS(SELECT 1
              FROM information_schema.TABLE_CONSTRAINTS TC
              WHERE TC.TABLE_NAME = 'T_PROJECT_PIPELINE_CALLBACK'
                AND TC.CONSTRAINT_TYPE = 'UNIQUE'
                AND TC.CONSTRAINT_NAME = 'IDX_PROJECT_CALLBACK') THEN
        ALTER TABLE T_PROJECT_PIPELINE_CALLBACK
            DROP INDEX IDX_PROJECT_CALLBACK;
    END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.TABLE_CONSTRAINTS TC
                  WHERE TC.TABLE_NAME = 'T_PROJECT_PIPELINE_CALLBACK'
                    AND TC.CONSTRAINT_TYPE = 'UNIQUE'
                    AND TC.CONSTRAINT_NAME = 'IDX_PROJECT_CALLBACK_EVENTS') THEN
        ALTER TABLE T_PROJECT_PIPELINE_CALLBACK
            ADD UNIQUE INDEX `IDX_PROJECT_CALLBACK_EVENTS` (`PROJECT_ID`, `CALLBACK_URL`, `EVENTS`);
    END IF;

    IF EXISTS(SELECT 1
              FROM information_schema.TABLE_CONSTRAINTS TC
              WHERE TC.TABLE_NAME = 'T_PROJECT_PIPELINE_CALLBACK'
                AND TC.CONSTRAINT_TYPE = 'UNIQUE'
                AND TC.CONSTRAINT_NAME = 'IDX_PROJECT_CALLBACK') THEN
        ALTER TABLE T_PROJECT_PIPELINE_CALLBACK
            DROP INDEX IDX_PROJECT_CALLBACK;
    END IF;

    IF NOT EXISTS(SELECT 1
			  FROM information_schema.statistics
			  WHERE TABLE_SCHEMA = db
				 AND TABLE_NAME = 'T_PIPELINE_FAVOR'
				 AND INDEX_NAME = 'PROJECT') THEN
		ALTER TABLE T_PIPELINE_FAVOR
		  ADD INDEX `PROJECT` (`PROJECT_ID`, `CREATE_USER`);
	END IF;

	IF NOT EXISTS(SELECT 1
			  FROM information_schema.statistics
			  WHERE TABLE_SCHEMA = db
				 AND TABLE_NAME = 'T_PIPELINE_FAVOR'
				 AND INDEX_NAME = 'IDX_CREATE_USER') THEN
		ALTER TABLE T_PIPELINE_FAVOR
		  ADD INDEX `IDX_CREATE_USER` (`CREATE_USER`);
	END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.TABLE_CONSTRAINTS TC
                  WHERE TC.TABLE_NAME = 'T_PROJECT_PIPELINE_CALLBACK'
                    AND TC.CONSTRAINT_TYPE = 'UNIQUE'
                    AND TC.CONSTRAINT_NAME = 'IDX_PROJECT_CALLBACK_EVENTS') THEN
        ALTER TABLE T_PROJECT_PIPELINE_CALLBACK
            ADD UNIQUE INDEX `IDX_PROJECT_CALLBACK_EVENTS` (`PROJECT_ID`, `CALLBACK_URL`, `EVENTS`);
    END IF;

    COMMIT;
END <CI_UBF>
DELIMITER ;
COMMIT;
CALL ci_process_schema_update();
