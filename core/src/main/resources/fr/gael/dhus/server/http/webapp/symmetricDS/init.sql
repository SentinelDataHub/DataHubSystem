-- Insert replica group
INSERT INTO SYM_NODE_GROUP (node_group_id, description) VALUES ('dhus-replica-group', 'DHuS Replica group');

-- Create group link between replicas and master
INSERT INTO SYM_NODE_GROUP_LINK (source_node_group_id, target_node_group_id, data_event_action) VALUES ('dhus-master-group', 'dhus-replica-group', 'W');
INSERT INTO SYM_NODE_GROUP_LINK (source_node_group_id, target_node_group_id, data_event_action) VALUES ('dhus-replica-group', 'dhus-master-group', 'P');

INSERT INTO SYM_ROUTER (router_id, source_node_group_id, target_node_group_id, create_time, last_update_time) VALUES ('masterToReplica','dhus-master-group','dhus-replica-group', current_timestamp, current_timestamp);
INSERT INTO SYM_ROUTER (router_id, source_node_group_id, target_node_group_id, create_time, last_update_time) VALUES ('replicaToMaster','dhus-replica-group','dhus-master-group', current_timestamp, current_timestamp);

-- Synchronization of Users data
INSERT INTO SYM_CHANNEL (channel_id, processing_order, max_batch_size, max_batch_to_send, extract_period_millis, batch_algorithm, enabled, description) VALUES ('USERS_CHANNEL', 1, 1000, 10, 0, 'default', 1, 'USERS channel');

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('USERS', 'USERS', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USERS', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USERS', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('USER_ROLES', 'USER_ROLES', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USER_ROLES', 'masterToReplica', 3, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USER_ROLES', 'replicaToMaster', 3, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('ACCESS_RESTRICTION', 'ACCESS_RESTRICTION', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('ACCESS_RESTRICTION', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('ACCESS_RESTRICTION', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('USER_RESTRICTIONS', 'USER_RESTRICTIONS', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USER_RESTRICTIONS', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('USER_RESTRICTIONS', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('PREFERENCES', 'PREFERENCES', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PREFERENCES', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PREFERENCES', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('SEARCH_PREFERENCES', 'SEARCH_PREFERENCES', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCH_PREFERENCES', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCH_PREFERENCES', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('SEARCHES', 'SEARCHES', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCHES', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCHES', 'replicaToMaster', 1, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('SEARCH_ADVANCED', 'SEARCH_ADVANCED', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCH_ADVANCED', 'masterToReplica', 1, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('SEARCH_ADVANCED', 'replicaToMaster', 1, current_timestamp, current_timestamp);

-- Synchronization of Collections data
INSERT INTO SYM_CHANNEL (channel_id, processing_order, max_batch_size, max_batch_to_send, extract_period_millis, batch_algorithm, enabled, description) VALUES ('COLLECTIONS_CHANNEL', 30, 1000, 10, 0, 'default', 1, 'COLLECTIONS channel');

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('COLLECTIONS', 'COLLECTIONS', 'COLLECTIONS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTIONS', 'masterToReplica', 3, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTIONS', 'replicaToMaster', 3, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('COLLECTION_USER_AUTH', 'COLLECTION_USER_AUTH', 'COLLECTIONS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTION_USER_AUTH', 'masterToReplica', 3, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTION_USER_AUTH', 'replicaToMaster', 3, current_timestamp, current_timestamp);

-- Synchronization of Products data
INSERT INTO SYM_CHANNEL (channel_id, processing_order, max_batch_size, max_batch_to_send, extract_period_millis, batch_algorithm, enabled, description) VALUES ('PRODUCTS_CHANNEL', 50, 1000, 10, 0, 'default', 1, 'PRODUCTS channel');

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('PRODUCTS', 'PRODUCTS', 'PRODUCTS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCTS', 'masterToReplica', 5, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCTS', 'replicaToMaster', 5, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('METADATA_INDEXES', 'METADATA_INDEXES', 'PRODUCTS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('METADATA_INDEXES', 'masterToReplica', 5, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('METADATA_INDEXES', 'replicaToMaster', 5, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('PRODUCT_USER_AUTH', 'PRODUCT_USER_AUTH', 'PRODUCTS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCT_USER_AUTH', 'masterToReplica', 6, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCT_USER_AUTH', 'replicaToMaster', 6, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('CHECKSUMS', 'CHECKSUMS', 'PRODUCTS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('CHECKSUMS', 'masterToReplica', 6, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('CHECKSUMS', 'replicaToMaster', 6, current_timestamp, current_timestamp);

-- Products in Collection/Cart relation
INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('COLLECTION_PRODUCT', 'COLLECTION_PRODUCT', 'COLLECTIONS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTION_PRODUCT', 'masterToReplica', 10, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('COLLECTION_PRODUCT', 'replicaToMaster', 10, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('PRODUCTCARTS', 'PRODUCTCARTS', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCTCARTS', 'masterToReplica', 10, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('PRODUCTCARTS', 'replicaToMaster', 10, current_timestamp, current_timestamp);

INSERT INTO SYM_TRIGGER (trigger_id, source_table_name, channel_id, last_update_time, create_time, SYNC_ON_INCOMING_BATCH) VALUES ('CART_PRODUCTS', 'CART_PRODUCTS', 'USERS_CHANNEL', current_timestamp, current_timestamp, 1);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('CART_PRODUCTS', 'masterToReplica', 10, current_timestamp, current_timestamp);
INSERT INTO SYM_TRIGGER_ROUTER (trigger_id, router_id, initial_load_order, create_time, last_update_time) VALUES ('CART_PRODUCTS', 'replicaToMaster', 10, current_timestamp, current_timestamp);
