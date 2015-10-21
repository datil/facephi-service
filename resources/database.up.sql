CREATE TABLE API_KEY (
       id NUMBER PRIMARY KEY,
       hashed_api_key VARCHAR2(255) NOT NULL,
       created DATE default sysdate,
       is_active NUMBER(1) CHECK (is_active IN ('0','1'))
);

CREATE UNIQUE INDEX api_key_unique_index ON api_key (hashed_api_key);

CREATE SEQUENCE api_key_id_seq increment by 1;

CREATE OR REPLACE TRIGGER api_key_trg
BEFORE INSERT ON API_KEY
FOR EACH ROW
BEGIN
SELECT api_key_id_seq.nextval INTO :new.id FROM dual;
END;
/

CREATE TABLE USER_ACCOUNT (
	id NUMBER PRIMARY KEY,
	username VARCHAR(255) NOT NULL,
        identification VARCHAR(20) NOT NULL,
	created DATE default sysdate,
	last_updated DATE default sysdate,
	is_active NUMBER(1) CHECK (is_active IN ('0','1')),
        is_locked NUMBER(1) CHECK (is_locked IN ('0','1')),
	face BLOB NOT NULL
);

CREATE UNIQUE INDEX user_account_unique_index ON user_account (username);

CREATE UNIQUE INDEX identification_unique_index ON user_account (identification);

CREATE SEQUENCE user_account_id_seq increment by 1;

CREATE OR REPLACE TRIGGER user_account_trg
BEFORE INSERT ON USER_ACCOUNT
FOR EACH ROW
BEGIN
SELECT user_account_id_seq.nextval INTO :new.id FROM dual;
END;
/

CREATE OR REPLACE
TRIGGER trg_ad_audit
BEFORE UPDATE ON USER_ACCOUNT FOR EACH ROW
BEGIN
    :new.last_updated := sysdate;
END;
/

CREATE TABLE USER_LOG (
	id NUMBER PRIMARY KEY,
	username VARCHAR(255) NOT NULL,
        identification VARCHAR(255) NOT NULL,
	created DATE default sysdate,
	transaction_type VARCHAR(255) NOT NULL,
	transaction_result VARCHAR(255) NOT NULL
);

CREATE SEQUENCE user_log_id_seq increment by 1;

CREATE OR REPLACE TRIGGER user_log_trg
BEFORE INSERT ON USER_LOG
FOR EACH ROW
BEGIN
SELECT user_log_id_seq.nextval INTO :new.id FROM dual;
END;
/

CREATE INDEX user_log_index ON user_log (username, transaction_type, transaction_result, created);
CREATE INDEX user_identification_log_index ON user_log (identification, transaction_type, transaction_result, created);
