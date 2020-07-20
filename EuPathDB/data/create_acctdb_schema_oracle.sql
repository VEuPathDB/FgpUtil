CREATE TABLE "USERACCOUNTS"."ACCOUNTS" (
  "USER_ID"       NUMBER(12,0) NOT NULL ENABLE,
  "EMAIL"         VARCHAR2(255 CHAR) NOT NULL ENABLE,
  "PASSWD"        VARCHAR2(50 BYTE) NOT NULL ENABLE,
  "IS_GUEST"      NUMBER(1,0) NOT NULL ENABLE,
  "SIGNATURE"     VARCHAR2(40 BYTE),
  "STABLE_ID"     VARCHAR2(500 BYTE),
  "REGISTER_TIME" TIMESTAMP (6),
  "LAST_LOGIN"    TIMESTAMP (6),
  CONSTRAINT "ACCOUNTS_PK" PRIMARY KEY ("USER_ID"),
  CONSTRAINT "EMAIL_UNIQ_CONSTRAINT" UNIQUE ("EMAIL")
);

CREATE TABLE "USERACCOUNTS"."ACCOUNT_PROPERTIES" (
  "USER_ID" NUMBER(12,0),
  "KEY"     VARCHAR2(12 BYTE),
  "VALUE"   VARCHAR2(4000 BYTE),
  CONSTRAINT "ACCOUNT_PROPERTIES_PK" PRIMARY KEY ("USER_ID", "KEY")
);

-- NOTE: Care must be taken to set the sequence start value appropriately.
--       This involves recreating the sequence with different values on
--       north and south replicated instances.
CREATE SEQUENCE "USERACCOUNTS"."ACCOUNTS_PKSEQ"
  MINVALUE 1 MAXVALUE 9999999999999999999999999999
  INCREMENT BY 10 START WITH 261307030 CACHE 20 NOORDER NOCYCLE NOKEEP GLOBAL;
