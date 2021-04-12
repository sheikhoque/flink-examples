## Capacity Planning of ES Cluster
while sizing the ES cluster, we need to consider two factors - a) minimum storage requirement b) computation power required based on data ingestion throughput. Each rule will come up with its own node requirement and we need to take the max of the two.

**Key Points to remember**
1) Primary Shards - while creating index, we need to define a setting 'number_of_shards'. It is highly recommended not to change this settings once created. 
2) Active Shards - Ideally, an index rolls over a period such as daily/hourly. For a daily index, the active index is current day's index where the writes/updates are happening [log analytics usecase]. For a active index, we have primary shard and replica. If we have 10 primary shard and replica factor of 1, then we have total 20 active shards for a daily index.

We did capacity planning for two different scenarios - 
* [a cluster with one index](https://github.com/sheikhoque/awslearning/blob/master/ElasticSearch/CapacityPlanningSingleIndex.md), 
* [a cluster with multiple index](https://github.com/sheikhoque/awslearning/blob/master/ElasticSearch/CapacityPlanningMultipleIndex.md)

