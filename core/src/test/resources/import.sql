/* java -cp ~/.m2/repository/org/hsqldb/hsqldb/2.3.1/hsqldb-2.3.1.jar org.hsqldb.util.DatabaseManagerSwing */
/* Add users *************************************************************************/
INSERT INTO PUBLIC.USERS(UUID, LOGIN, PASSWORD, PASSWORD_ENCRYPTION, CREATED, UPDATED, DELETED, EMAIL) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'koko', 'koko', 'NONE', '2014-06-03 15:35:05.037000', '2014-06-03 15:35:05.037000', FALSE, 'koko@kokoFactories.fr');
INSERT INTO PUBLIC.USERS(UUID, LOGIN, PASSWORD, PASSWORD_ENCRYPTION, CREATED, UPDATED, DELETED) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'riko', 'koko', 'NONE', '2014-06-03 15:35:05.037000', '2014-06-03 15:35:05.037000', TRUE);
INSERT INTO PUBLIC.USERS(UUID, LOGIN, PASSWORD, PASSWORD_ENCRYPTION, CREATED, UPDATED, DELETED) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'toto', 'koko', 'NONE', '2014-06-03 15:35:05.037000', '2014-06-03 15:35:05.037000', FALSE);
INSERT INTO PUBLIC.USERS(UUID, LOGIN, PASSWORD, PASSWORD_ENCRYPTION, CREATED, UPDATED, DELETED) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'babar', 'koko', 'NONE', '2014-06-03 15:35:05.037000', '2014-06-03 15:35:05.037000', FALSE);
-- add roles AUTHED, SEARCH, DOWNLOAD, UPLOAD, DATA_MANAGER, USER_MANAGER, SYSTEM_MANAGER, ARCHIVE_MANAGER, STATISTICS
INSERT INTO PUBLIC.USER_ROLES(USER_UUID, ROLES) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'AUTHED')
INSERT INTO PUBLIC.USER_ROLES(USER_UUID, ROLES) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'AUTHED')
INSERT INTO PUBLIC.USER_ROLES(USER_UUID, ROLES) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'AUTHED')
INSERT INTO PUBLIC.USER_ROLES(USER_UUID, ROLES) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'AUTHED')

/* Preferences ***********************************************************************************/
INSERT INTO PREFERENCES VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PREFERENCES VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PREFERENCES VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PREFERENCES VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')
-- set users preferences 1 3 2 0
UPDATE PUBLIC.USERS AS u SET u.PREFERENCES_UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0' WHERE u.UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0'
UPDATE PUBLIC.USERS AS u SET u.PREFERENCES_UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1' WHERE u.UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1'
UPDATE PUBLIC.USERS AS u SET u.PREFERENCES_UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2' WHERE u.UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2'
UPDATE PUBLIC.USERS AS u SET u.PREFERENCES_UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3' WHERE u.UUID = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3'

/* Add Products **********************************************************************************/
INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID, OWNER_UUID, DOWNLOAD_PATH) VALUES(0, '2014-06-05 15:35:05.037000', '2014-06-05 21:35:05.037000', FALSE, 'file:/home/lambert/test/prod0', TRUE, 'prod0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'file:/home/lambert/test/prod0')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID, OWNER_UUID) VALUES(1, '2014-06-05 15:35:05.037000', '2014-06-05 20:35:05.037000', TRUE, 'file:/home/lambert/test/prod1', FALSE, 'prod1','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, SIZE, UUID, DOWNLOAD_PATH, OWNER_UUID) VALUES(2, '2014-06-02 15:35:05.037000', '2014-06-05 19:35:05.037000', FALSE, 'file:/home/lambert/test/prod2', FALSE, 'prod2', 512,'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'downloadPath', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID, OWNER_UUID) VALUES(3, '2014-06-03 15:35:05.037000', '2014-06-05 18:35:05.037000', FALSE, 'file:/home/lambert/test/prod3', FALSE, 'prod3','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, ORIGIN, SIZE, UUID) VALUES(4, '2014-06-06 15:35:05.037000', '2014-06-12 17:35:05.037000', TRUE, 'file:/home/lambert/test/prod4', FALSE, 'prod4', 'space', 1042,'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID) VALUES(5, '2014-06-07 15:35:05.037000', '2014-06-16 16:35:05.037000', FALSE, 'file:/home/lambert/test/prod5', TRUE, 'prod5','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID, ORIGIN, DOWNLOAD_PATH) VALUES(6, '2014-06-07 15:35:05.037000', '2014-06-16 15:35:05.037000', FALSE, 'file:/home/lambert/test/prod6', TRUE, 'prod6','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6', 'space', 'file:/home/lambert/test/prod6')

INSERT INTO PUBLIC.PRODUCTS(ID, CREATED, UPDATED, LOCKED, PATH, PROCESSED, IDENTIFIER, UUID, ORIGIN, DOWNLOAD_PATH) VALUES(7, '2014-06-07 15:35:05.037000', '2014-06-16 15:35:05.037000', FALSE, 'file:/home/lambert/test/prod7', TRUE, 'prod7','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7', 'space/invaders', 'file:/home/lambert/test/prod7')

-- product authorized user
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')

INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(1, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')

INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')

INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(3, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(3, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')

INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(5, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(5, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(5, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')

INSERT INTO PUBLIC.PRODUCT_USER_AUTH(PRODUCTS_ID, USERS_UUID) VALUES(6, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')

/* CHECKSUM **************************************************************************************/
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (6, 'MD5','abc')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (6, 'SHA-1', 'acb')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (6, 'SHA-256', 'bac')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (2, 'MD5', 'MON MD5 PRODUCT 2')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (7, 'MD5','abc')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (7, 'SHA-1', 'acb')
INSERT INTO PUBLIC.CHECKSUMS(PRODUCT_ID, DOWNLOAD_CHECKSUM_ALGORITHM, DOWNLOAD_CHECKSUM_VALUE) VALUES (7, 'SHA-256', 'bac')
/* Add MetadataIndex *****************************************************************************/
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('Size ', 'text/plain', '1GB', 2)
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('Name', 'text/plain', 'metadata', 2)
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('Size ', 'text/plain', '2GB', 1)
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('Name', 'text/plain', 'meta', 1)
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('deletable', 'text/plain', 'test', 6)
INSERT INTO PUBLIC.METADATA_INDEXES(NAME, TYPE, VALUE, PRODUCT_ID) VALUES('updatable', 'text/plain', 'test', 7)

/* Add Collections + root collection *************************************************************/
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME, DESCRIPTION) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', '#.root', 'Root of all the collections')
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'Asia')
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'Africa')
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'Japan')
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4', 'China')
INSERT INTO PUBLIC.COLLECTIONS(UUID, NAME) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5', 'SouthAfrica')
-- insert products in collection
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 0)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 1)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 2)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 3)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 5)

INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 4)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 5)

INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 0)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 1)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 2)

INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4', 1)
INSERT INTO PUBLIC.COLLECTION_PRODUCT(COLLECTIONS_UUID, PRODUCTS_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4', 3)
-- add authorized users
INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')

INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')

INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')

INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')

INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.COLLECTION_USER_AUTH(COLLECTIONS_UUID, USERS_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')
/* Add Searches **********************************************************************************/
INSERT INTO PUBLIC.SEARCHES(UUID, COMPLETE, FOOTPRINT, NOTIFY, VALUE) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', NULL, NULL, FALSE, 'value0')
INSERT INTO PUBLIC.SEARCHES(UUID, COMPLETE, FOOTPRINT, NOTIFY, VALUE) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', NULL, NULL, TRUE, 'value1')
INSERT INTO PUBLIC.SEARCHES(UUID, COMPLETE, FOOTPRINT, NOTIFY, VALUE) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', NULL, NULL, TRUE, 'value2')
INSERT INTO PUBLIC.SEARCHES(UUID, COMPLETE, FOOTPRINT, NOTIFY, VALUE) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', NULL, NULL, FALSE, 'value3')
-- add advances
INSERT INTO PUBLIC.SEARCH_ADVANCED(SEARCH_UUID, ADVANCED, ADVANCED_KEY) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'advanceValue', 'advanceKey')
-- add search preferences
INSERT INTO PUBLIC.SEARCH_PREFERENCES(PREFERENCE_UUID, SEARCHES_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.SEARCH_PREFERENCES(PREFERENCE_UUID, SEARCHES_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.SEARCH_PREFERENCES(PREFERENCE_UUID, SEARCHES_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')

/* Add AccessRestriction *************************************************************************/
INSERT INTO PUBLIC.ACCESS_RESTRICTION(ACCESS_RESTRICTION, UUID, EXPIRED, BLOCKING_REASON) VALUES('locked', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', TRUE, 'punition1')
INSERT INTO PUBLIC.ACCESS_RESTRICTION(ACCESS_RESTRICTION, UUID, EXPIRED, BLOCKING_REASON) VALUES('expired', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', FALSE, 'too late')
INSERT INTO PUBLIC.ACCESS_RESTRICTION(ACCESS_RESTRICTION, UUID, EXPIRED, BLOCKING_REASON) VALUES('locked', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', FALSE, 'punition2')
INSERT INTO PUBLIC.ACCESS_RESTRICTION(ACCESS_RESTRICTION, UUID, EXPIRED, BLOCKING_REASON) VALUES('locked', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', FALSE, 'punition3')
-- add User Restrictions
INSERT INTO PUBLIC.USER_RESTRICTIONS(USER_UUID, RESTRICTION_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.USER_RESTRICTIONS(USER_UUID, RESTRICTION_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.USER_RESTRICTIONS(USER_UUID, RESTRICTION_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')

/* Add files scanner *****************************************************************************/
INSERT INTO PUBLIC.FILE_SCANNER(ID, ACTIVE, STATUS, STATUS_MESSAGE, URL, USERNAME) VALUES(0, FALSE, 'ok', 'success', 'coco-abricot', 'test')
INSERT INTO PUBLIC.FILE_SCANNER(ID, ACTIVE, STATUS, STATUS_MESSAGE) VALUES(1, TRUE, 'running', 'running')
INSERT INTO PUBLIC.FILE_SCANNER(ID, ACTIVE, STATUS, STATUS_MESSAGE) VALUES(2, FALSE, 'error', 'failed')
INSERT INTO PUBLIC.FILE_SCANNER(ID, ACTIVE, STATUS, STATUS_MESSAGE) VALUES(3, TRUE, 'running', 'running...')
-- associate files scanner and collections
INSERT INTO PUBLIC.FILESCANNER_COLLECTIONS(FILE_SCANNER_ID, COLLECTIONS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.FILESCANNER_COLLECTIONS(FILE_SCANNER_ID, COLLECTIONS_UUID) VALUES(0, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.FILESCANNER_COLLECTIONS(FILE_SCANNER_ID, COLLECTIONS_UUID) VALUES(1, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1')
INSERT INTO PUBLIC.FILESCANNER_COLLECTIONS(FILE_SCANNER_ID, COLLECTIONS_UUID) VALUES(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4')
-- add file scanner preferences
INSERT INTO PUBLIC.FILE_SCANNER_PREFERENCES(PREFERENCE_UUID, FILE_SCANNER_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 0)
INSERT INTO PUBLIC.FILE_SCANNER_PREFERENCES(PREFERENCE_UUID, FILE_SCANNER_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 2)
INSERT INTO PUBLIC.FILE_SCANNER_PREFERENCES(PREFERENCE_UUID, FILE_SCANNER_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 1)

/* Add Product Cart ******************************************************************************/
INSERT INTO PUBLIC.PRODUCTCARTS(UUID, USER_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0')
INSERT INTO PUBLIC.PRODUCTCARTS(UUID, USER_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3')
INSERT INTO PUBLIC.PRODUCTCARTS(UUID, USER_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
INSERT INTO PUBLIC.PRODUCTCARTS(UUID, USER_UUID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2')
-- add product in cart
INSERT INTO PUBLIC.CART_PRODUCTS(CART_UUID, PRODUCT_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 0)
INSERT INTO PUBLIC.CART_PRODUCTS(CART_UUID, PRODUCT_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0', 5)
INSERT INTO PUBLIC.CART_PRODUCTS(CART_UUID, PRODUCT_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1', 5)
INSERT INTO PUBLIC.CART_PRODUCTS(CART_UUID, PRODUCT_ID) VALUES('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3', 0)

/* Add NetworkUsage ******************************************************************************/
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(0, '2014-07-10 17:00:00.000000', TRUE, 2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(1, '2014-07-10 17:00:00.000000', TRUE, 4, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(2, '2014-07-12 17:00:00.000000', FALSE, 8, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(3, '2014-07-13 17:00:00.000000', TRUE, 16, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(4, '2014-07-14 17:00:00.000000', FALSE, 32, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(5, '2014-07-16 17:00:00.000000', TRUE, 64, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(6, '2014-07-16 17:00:00.000000', FALSE, 128, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3');
INSERT INTO PUBLIC.NETWORK_USAGE(ID, DATE, IS_DOWNLOAD, SIZE, USER_UUID) VALUES(7, '2014-07-16 17:01:00.000000', TRUE, 512, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2');
