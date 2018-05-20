CREATE TABLE CARPRICE (
	BRAND VARCHAR(20) NOT NULL,
	RATE DECIMAL(6,2) NOT NULL,
	CONSTRAINT CP_PK PRIMARY KEY (BRAND)
);

/* ~~~~DUMMY CREDIT CARD AND DRIVER LICENSE DATABASE~~~~ */

CREATE TABLE CREDITCARD (        /* VERIFY BY MATCHING CARDNUMBER, EXPIRY DATE AND CARDHOLDER'S NAME */
CARDNUMBER CHAR(16) NOT NULL,    /* FIXED LENGTH 16 DIGITS */
EXPIRYDATE DATE NOT NULL,
CARDHOLDER VARCHAR(20) NOT NULL, /* NAME OF CARDHOLDER */
BALANCE DECIMAL(10,2) NOT NULL,
CVV DECIMAL(3) NOT NULL,
CONSTRAINT CREDITCARD_PK PRIMARY KEY (CARDNUMBER) );

CREATE TABLE BANKACCOUNT (        /* VERIFY BY MATCHING CARDNUMBER, EXPIRY DATE AND CARDHOLDER'S NAME */
BSB CHAR(6) NOT NULL,    /* FIXED LENGTH 16 DIGITS */
ACCOUNTNUMBER VARCHAR(12) NOT NULL,
ACCOUNTHOLDER VARCHAR(20) NOT NULL, /* NAME OF ACCOUNT HOLDER */
BALANCE DECIMAL(10,2) NOT NULL,
CONSTRAINT CREDITCARD_PK PRIMARY KEY (BSB,ACCOUNTNUMBER) );

CREATE TABLE DRIVERLICENSE(      /* VERIFY BY MATCHING  LICENSENUMBER, DOB, FIRSTNAME, AND LASTNAME */
LICENSENUM VARCHAR(15) NOT NULL, /* LICENSE NUMBER */
DOB DATE NOT NULL,               /* DATE OF BIRTH */
FIRSTNAME VARCHAR(20) NOT NULL,
LASTNAME VARCHAR(20) NOT NULL,
EXPIRYDATE DATE NOT NULL,
CONSTRAINT DRIVERLICENSE_PK PRIMARY KEY (LICENSENUM) );

CREATE TABLE REGO(      /* VERIFY BY MATCHING LICENSENUMBER, DOB, FIRSTNAME, AND LASTNAME */
REGO VARCHAR(10) NOT NULL, /* LICENSE NUMBER */
FIRSTNAME VARCHAR(20) NOT NULL,
LASTNAME VARCHAR(20) NOT NULL,
CONSTRAINT REGO_PK PRIMARY KEY (REGO)
);

/* ~~~~~~~~~~~~~~~~SYSTEM DATABASE~~~~~~~~~~~~~~~~ */

CREATE TABLE USER(
    USERNAME VARCHAR(20) NOT NULL,
    PASSWORD VARCHAR(100) NOT NULL,
    FIRSTNAME VARCHAR(20) NOT NULL,
    LASTNAME VARCHAR(20) NOT NULL,
    DOB DATE NOT NULL,
    TYPE VARCHAR(6) NOT NULL,
    CONSTRAINT USER_PK PRIMARY KEY (USERNAME)
);

CREATE TABLE CARRENTER(
    USERNAME VARCHAR(20) NOT NULL,
    LICENSENUM VARCHAR(15) NOT NULL,
    CARDNUMBER CHAR(16) NOT NULL,
    SOCIALMEDIALINK VARCHAR(50),
    CONSTRAINT CARRENTER_PK PRIMARY KEY (USERNAME),
    CONSTRAINT CARRENTER_FK FOREIGN KEY (USERNAME) REFERENCES USER(USERNAME)
);

CREATE TABLE CAROWNER (
    USERNAME VARCHAR(20) NOT NULL,
    BSB CHAR(6) NOT NULL,
    ACCOUNTNUMBER VARCHAR(12) NOT NULL,
    CONSTRAINT CAROWNER_PK PRIMARY KEY (USERNAME),
    CONSTRAINT CAROWNER_FK FOREIGN KEY (USERNAME) REFERENCES CARRENTER(USERNAME)
);

CREATE TABLE CAR (
    REGO VARCHAR(6) NOT NULL,
    BRAND VARCHAR(15) NOT NULL,
    MODEL VARCHAR(15) NOT NULL,
    LOCATION VARCHAR(50) NOT NULL,
    COLOUR VARCHAR(15) NOT NULL,
    TRANSMISSION VARCHAR(6) NOT NULL,
    YEAR DECIMAL(4) NOT NULL,
    CAPACITY DECIMAL(2) NOT NULL,
    ODOMETER DECIMAL(10,2) NOT NULL,
    OWNER VARCHAR(20) NOT NULL,
    IMAGEPATH VARCHAR(20),
    CONSTRAINT CAR_PK PRIMARY KEY (REGO),
    CONSTRAINT CAR_FK FOREIGN KEY (OWNER) REFERENCES CAROWNER(USERNAME),
    CONSTRAINT CAR_CHECK CHECK (TRANSMISSION IN ('AUTO','MANUAL'))
);

CREATE TABLE LISTING (
	LISTINGNUM DECIMAL(10) NOT NULL,
    REGO VARCHAR(6) NOT NULL,
    OWNER VARCHAR(20) NOT NULL,
    CONSTRAINT LISTING_PK PRIMARY KEY (LISTINGNUM),
    CONSTRAINT LISTING_CK UNIQUE (REGO),
    CONSTRAINT LISTING_FK1 FOREIGN KEY (OWNER) REFERENCES CAROWNER(USERNAME),
    CONSTRAINT LISTING_FK2 FOREIGN KEY (REGO) REFERENCES CAR(REGO)
);

CREATE TABLE AVAILABILITY (
	LISTINGNUM DECIMAL(10) NOT NULL,
    AVAILDATE DATE,
    CONSTRAINT LISTING_PK PRIMARY KEY (LISTINGNUM,AVAILDATE),
    CONSTRAINT LISTING_FK FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE
);

CREATE TABLE BOOKINGREQUEST (
	LISTINGNUM DECIMAL(10) NOT NULL,
	FROMDATE DATE NOT NULL,
	TODATE DATE NOT NULL,
	REQUESTER VARCHAR(20) NOT NULL,
	PRICE DECIMAL(10,2) NOT NULL,
	CONSTRAINT BREQUEST_PK PRIMARY KEY (LISTINGNUM,REQUESTER),
    CONSTRAINT BREQUEST_FK1 FOREIGN KEY (REQUESTER) REFERENCES CARRENTER(USERNAME),
    CONSTRAINT BREQUEST_FK2 FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE
);

/* CREATE TABLE BOOKING(
	LISTINGNUM DECIMAL(10) NOT NULL,
	FROMDATE DATE NOT NULL,
	TODATE DATE NOT NULL,
	REQUESTER VARCHAR(20) NOT NULL,
	PRICE DECIMAL(10,2) NOT NULL,
	CONSTRAINT BOOKING_PK PRIMARY KEY (LISTINGNUM,FROMDATE,TODATE),
    CONSTRAINT BOOKING_FK1 FOREIGN KEY (REQUESTER) REFERENCES CARRENTER(USERNAME),
    CONSTRAINT BOOKING_FK2 FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE
); */

CREATE TABLE NOTIFICATION (
	NOTIFNUMBER DECIMAL(10) NOT NULL,
	MESSAGE VARCHAR(60) NOT NULL,
	NOTIFTYPE VARCHAR(20) NOT NULL,
	RECEIVER VARCHAR(20) NOT NULL,
	SEEN BOOLEAN NOT NULL,
	CONSTRAINT NOTIF_PK PRIMARY KEY (NOTIFNUMBER),
    CONSTRAINT NOTIF_FK FOREIGN KEY (RECEIVER) REFERENCES CARRENTER(USERNAME)
);

CREATE TABLE TRANSACTION (
	LISTINGNUM DECIMAL(10) NOT NULL,
	FROMDATE DATE NOT NULL,
	TODATE DATE NOT NULL,
	SENDER VARCHAR(20) NOT NULL,
	RECEIVER VARCHAR(20) NOT NULL,
	AMOUNT DECIMAL(10,2) NOT NULL,
	CONSTRAINT T_PK PRIMARY KEY (LISTINGNUM,FROMDATE,TODATE),
   /* CONSTRAINT T_FK1 FOREIGN KEY (LISTINGNUM,FROMDATE,TODATE) REFERENCES BOOKING(LISTINGNUM,FROMDATE,TODATE), */
    CONSTRAINT T_FK2 FOREIGN KEY (SENDER) REFERENCES CARRENTER(USERNAME),
    CONSTRAINT T_FK3 FOREIGN KEY (RECEIVER) REFERENCES CAROWNER(USERNAME),
    CONSTRAINT T_FK1 FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE
);

CREATE TABLE MESSAGE (
	SENDER VARCHAR(20) NOT NULL,
	RECEIVER VARCHAR(20) NOT NULL,
	MESSAGE VARCHAR(100) NOT NULL,
	TSTAMP BIGINT(19) NOT NULL,
	CONSTRAINT M_PK PRIMARY KEY (SENDER,RECEIVER,TSTAMP),
    CONSTRAINT M_FK1 FOREIGN KEY (SENDER) REFERENCES USER(USERNAME),
    CONSTRAINT M_FK2 FOREIGN KEY (RECEIVER) REFERENCES USER(USERNAME)
);

CREATE TABLE REVIEW (
	LISTINGNUM DECIMAL(10) NOT NULL,
	REVIEWER VARCHAR(20) NOT NULL,
	REVIEWMESSAGE VARCHAR(200) NOT NULL,
	RATING DECIMAL(1) NOT NULL,
	TSTAMP BIGINT(19) NOT NULL,
	CONSTRAINT REVIEW_PK PRIMARY KEY (LISTINGNUM,REVIEWER,TSTAMP),
    CONSTRAINT REVIEW_FK1 FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE,
    CONSTRAINT REVIEW_FK2 FOREIGN KEY (REVIEWER) REFERENCES USER(USERNAME)
);

CREATE TABLE COMPLAINT (
	CID DECIMAL(10) NOT NULL,
	COMPLAINANT VARCHAR(20) NOT NULL,
	LISTINGNUM DECIMAL(10) NOT NULL,
	DESCRIPTION VARCHAR(100) NOT NULL,
	APPROVED BOOLEAN NOT NULL,
	CONSTRAINT COMPLAINT_PK PRIMARY KEY (CID),
    CONSTRAINT COMPLAINT_FK1 FOREIGN KEY (LISTINGNUM) REFERENCES LISTING(LISTINGNUM) ON DELETE CASCADE,
    CONSTRAINT COMPLAINT_FK2 FOREIGN KEY (COMPLAINANT) REFERENCES CARRENTER(USERNAME)
);

/* ~~~~~~~~~~~~~~~~EXAMPLE INSERTION~~~~~~~~~~~~~~~~ */
INSERT INTO DRIVERLICENSE VALUES('222222222',STR_TO_DATE('12-03-1992', '%d-%m-%Y'),'Hugo','Smith',STR_TO_DATE('22-07-2020', '%d-%m-%Y'));
INSERT INTO DRIVERLICENSE VALUES('111111111',STR_TO_DATE('21-12-1988', '%d-%m-%Y'),'Dan','Tran',STR_TO_DATE('22-07-2020', '%d-%m-%Y'));
INSERT INTO CREDITCARD VALUES('1234567812345678' , STR_TO_DATE('23-04-2020', '%d-%m-%Y'), 'Dan Tran',1000,123);
INSERT INTO CREDITCARD VALUES('8765432187654321' , STR_TO_DATE('13-10-2030', '%d-%m-%Y'), 'Hugo Smith',1000,123);
INSERT INTO REGO VALUES ('PKR123','Dan','Tran');
INSERT INTO REGO VALUES ('PKR234','Dan','Tran');
INSERT INTO REGO VALUES ('PKR456','Dan','Tran');
INSERT INTO REGO VALUES ('123','Hugo','Smith');
INSERT INTO CARPRICE VALUES ('Audi',75);
INSERT INTO CARPRICE VALUES ('Toyota',30);
INSERT INTO CARPRICE VALUES ('Great Wall',22);
INSERT INTO CARPRICE VALUES ('Ferrari',500);
INSERT INTO CARPRICE VALUES ('Mercedes',80);
INSERT INTO CARPRICE VALUES ('Hyundai',75);
INSERT INTO CARPRICE VALUES ('Jeep',100);
INSERT INTO CARPRICE VALUES ('Honda',25);
INSERT INTO CARPRICE VALUES ('Ford',35);
INSERT INTO CARPRICE VALUES ('BMW',65);
INSERT INTO CARPRICE VALUES ('Tesla',200);
INSERT INTO CARPRICE VALUES ('Mini',48);
INSERT INTO CARPRICE VALUES ('Volkswagen',50);

INSERT INTO BANKACCOUNT VALUES ('123456','1234567890','Dan Tran', 100);

DELIMITER $$
CREATE TRIGGER after_carowner_insert
    AFTER INSERT ON CAROWNER
    FOR EACH ROW 
BEGIN
    UPDATE USER SET TYPE = 'OWNER' WHERE USERNAME = NEW.USERNAME;
END;$$
DELIMITER ;
/* INSERT INTO LISTING VALUES(1,'PKR123','Audi','R8','Wollongong','black','manual',2015,4,1000,'ldt999',NULL); */
/* 
CREATE TRIGGER EXCLUSIVE_USERNAME BEFORE INSERT ON CAROWNER
    FOR EACH ROW
	LOOP
        IF NEW.USERNAME IN (SELECT USERNAME FROM CARRENTER) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'USERNAME EXISTS';
        END IF;
	END LOOP;
END;
*/
