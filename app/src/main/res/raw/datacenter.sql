DROP TABLE IF EXISTS USER_NOTIFICATION;
create table USER_NOTIFICATION(
	d_id       LONG,
    cindex      int,
   	command   varchar(40),
   	ctime      LONG
);
DROP TABLE IF EXISTS DEVICE_STATUSINFO;
create table DEVICE_STATUSINFO(
	id		    LONG primary key,
	zj_id       LONG,
   	device_name          varchar(40),
	device_logo	     	 varchar(50),
   	device_type          varchar(20),
	device_tid	     	 long,
	device_where         varchar(20),
	device_controltype   varchar(20),
	appdownload          varchar(255),
	apppackage           varchar(100),
   	device_lastcommand   varchar(40),
   	device_lasttime      LONG,
   	special              int,
   	device_dtype         varchar(40),
	app_acceptmessage    varchar(1),
	re_1                 varchar(10),
	sort                 int,
	status               int,
	nr                   int,
	nt                   int,
	ca                   varchar(10),
	cak                  varchar(20),
	dr                   int,
	lowb                 int,
	ipc					 varchar(100),
	device_slavedId      varchar(20),
	bipc       varchar(40),
	eid         varchar(20),
	fa           int,
	mc           varchar(10)
);
DROP TABLE IF EXISTS ZHUJI_ADSINFO;
create table ZHUJI_ADSINFO(
	zj_id       LONG,
   	name        varchar(40),
	logo	    varchar(50),
   	remark      varchar(20),
	ca	     	varchar(20),
	ut          int,
	url         varchar(20)
);

DROP TABLE IF EXISTS ZHUJI_SETINFO;
create table ZHUJI_SETINFO(
	zj_id       LONG,
   	k        varchar(10),
	v	    varchar(10)
);

DROP TABLE IF EXISTS DEVICE_COMMAND;
create table DEVICE_COMMAND(
    m_id       LONG,
	d_id       LONG,
   	command   varchar(40),
   	ctime      LONG,
   	ct         varchar(40),
   	dcg         LONG,
   	special     int
);

DROP TABLE IF EXISTS ZHUJI_STATUSINFO;
create table ZHUJI_STATUSINFO(
	id		    LONG primary key,
   	name             varchar(40),
   	dwhere             varchar(40),
   	logo             varchar(40),
   	dbn              varchar(20),
	online	     	 varchar(50),
	masterid	     	 varchar(50),
	cid                  int,
	admin                varchar(10),
	scene                varchar(10),
	uc                   int,
	updateStatus         int,
	powerStatus          int,
	batteryStatus        int,
	simStatus            int,
	wanType              int,
	gsm                  int,
	statusCall           int,
	statusSms            int,
	ca                   varchar(10),
    cak                  varchar(20),
    scenet                  varchar(10),
    bipc		LONG,
    ipcid		LONG,
    camerac                  varchar(10),
    cameraid                 varchar(10),
    cameran                  varchar(50),
    camerap                  varchar(50),
    dt                  varchar(50),
    dtid                 varchar(50),
    ac                 int,
    ex                 int,
    la                 int,
    rolek             varchar(40)

);
DROP TABLE IF EXISTS GROUP_STATUSINFO;
create table GROUP_STATUSINFO(
	id		    LONG primary key,
	zj_id       LONG,
   	name             varchar(40),
   	logo             varchar(40),
	bipc       varchar(40)
);
DROP TABLE IF EXISTS GROUP_DEVICE_RELATIOIN;
create table GROUP_DEVICE_RELATIOIN(
   	gid          LONG,
   	did          LONG
);
DROP TABLE IF EXISTS ZHUJI_GROUP_STATUSINFO;
create table ZHUJI_GROUP_STATUSINFO(
	id		    LONG,
   	name             varchar(40),
   	logo             varchar(40)
);
DROP TABLE IF EXISTS ZHUJI_GROUP_DEVICE_RELATIOIN;
create table ZHUJI_GROUP_DEVICE_RELATIOIN(
   	gid          LONG,
   	did          LONG
);
DROP TABLE IF EXISTS USER_INFO;
create table USER_INFO(
	id	     	LONG primary key,
	account              varchar(50),
   	name                 varchar(50),
   	mobile               varchar(12),
   	email                varchar(80),
   	logo                 varchar(100),
	role            varchar(5),
   	code       varchar(50)
);

DROP TABLE IF EXISTS USER_CONFIG;
create table USER_CONFIG(
	`key`			       varchar(100),
	`value`              varchar(100)
);

DROP TABLE IF EXISTS FAMINY_MEMBER;
create table FAMINY_MEMBER(
    id LONG,
    name   varchar(15),
    sex    varchar(5),
    birthday varchar(20),
    logo varchar(30),
    objectiveWeight varchar(10),
    height varchar(5),
    odbp int,
    osbp int,
    skinFid LONG
);

DROP TABLE IF EXISTS devices_key;
CREATE TABLE devices_key (
  `d_id` LONG,
  `key_name` varchar(20),
  `key_ico` varchar(100),
  `key_command` varchar(20),
  `key_sort` int,
  `key_where` int,
  key_sstate int
);

DROP TABLE IF EXISTS PERS;
CREATE TABLE PERS(
    id LONG,
    zj_id LONG,
    k varchar(40),
    v varchar(5)
)
