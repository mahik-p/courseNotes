
DROP TABLE aa_client_phone;
DROP TABLE aa_client;

CREATE TABLE aa_client (
    client_id   NUMBER(6, 0),
    client_name VARCHAR2(200) NOT NULL,
    client_risk CHAR(1)       NOT NULL,
    CONSTRAINT aa_client_pk PRIMARY KEY(client_id)
);

CREATE TABLE aa_client_phone (
    client_id    NUMBER(6, 0),
    phone_number VARCHAR2(15) NOT NULL,
    CONSTRAINT aa_client_phone_pk PRIMARY KEY(client_id)
);
    
ALTER TABLE aa_client_phone
    ADD CONSTRAINT aa_client_client_phone_fk
    FOREIGN KEY (client_id)
    REFERENCES aa_client(client_id);

INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (1, 'Ford Prefect', 'H');
INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (2, 'Arthur Dent', 'L');
INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (3, 'Tricia McMillan', 'M');
INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (4, 'Zaphod Beeblebrox', 'P');
INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (5, 'Hotblack Desiato', 'H');
INSERT INTO aa_client (client_id, client_name, client_risk) VALUES (6, 'Slartibartfast', 'L');

INSERT INTO aa_client_phone (client_id, phone_number) VALUES (1, '+441174960234');
INSERT INTO aa_client_phone (client_id,  phone_number) VALUES (2, '+441632960987');
INSERT INTO aa_client_phone (client_id,  phone_number) VALUES (3, '+14245550110');
INSERT INTO aa_client_phone (client_id,  phone_number) VALUES (4, '+914440311010');
INSERT INTO aa_client_phone (client_id,  phone_number) VALUES (6, '+353209174242');

COMMIT;
