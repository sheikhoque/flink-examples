## Capacity Planning Multiple Index Cluster 

**Formulas**
```
       Storage Required        = Daily Source Data * (1 + Replica) * Days of Retention                  .... (I)
       Primary Shard Size      = 50 GB [log Analytics] ***[40GB for search usecase]                     .... (II)
       Primary Shard Count     = Daily Source Data * 1.25 / Primary Shard Size *** 0.25 is the overhead .... (III) 
       SHARD VS CPU            = 1 : 1.5                                                                .... (IV)
       ACTIVE SHARDS           = Primary Shard Count * (1+Replica)                                      .... (V)
       Number of active shards should be divisble by number of instances                                .....(V*)
```

**Use Case**
> Consider a use case, where we have heartbeat getting ingested every 20 second to elastic search. Lets say,
> we have 400K concurrent user sending heartbeats, where each heartbeat is of size 1200  bytes. We are 
>ingesting some quality metrics to ElasticSearch in two indices - 1) short_interval*, 2)long_interval*. We want to
>do proper capacity planning for our ElasticSearch cluster. 

**Storage Requirement**
```
        Document Arrival        = 20s
        Document Size           = 1200 bytes
        Concurrent Users        = 400000
        Concurrent Request/Sec  = Concurrent Users/Document Arrival = 400000/20 = 20000
        Daily Total Request     = Concurrent Request/Sec *60*60*24 = 20000*60*60*24 = 1728*10^6
        Daily Source Data       = Document Size * Daily Total Request = 1200 * 1728 * 10^6 / 10^9 GB = 2073.6 GB
        Replica                 = 1    
        [Considering r5.4xlarge]
        r5.4xlarge 
                EBS Volume = 6000 GB   ......(VI)
                VCPU       = 16        ......(VII)
                MEMORY     = 128 GB    ......(VIII)
        UltraWarm Large
                Volume     = 20TB
                VCPU       = 16

        ***Note that for both index, daily source data is same as we are considering each request as independent session for worst case
```

## Capacity Planning without ultrawarm Multiple index

**Index 1**
```
        Retention Period        = 30
        Primary Shard Count     = Math.Ceiling((Daily Source Data * 1.25)/50 GB) [**(III)** ] = 52
        Active Shards           = Primary Shard Count * (1+ Replica) [**(V)**] = 52 ( 1+1) = 104
        Min. Storage Required   = Daily Source Data * (1 + Replica) * Retention Period [**(I)**] = 2073.6*(1+1)*30 = 124416 GB 
        [Considering r5.4xlarge]
        No. Instances based on Storage      = Math.CEILING(Min. Storage Required / EBS Volume **[VI]**)/3 (each document of an interval is updated atmost thrice) = Math.CEILING(124416/6000/3) = 7 ..(IX)
        No. Instances based on CPU Required = Math.CEILING(Active Shards * Shard vs CPU [**(IV)**]/VCPU Per Instance [**(VII)**)) = 10   ..(X)

        Considering (IX) && (X), we should have 10 instances of R5.4xlarge instances. 

        as per (V*) number of active shards should be divisible by number of instances to make sure no data skewness.
        So, active shards/No. Instances based on Storage = 104/21 = Not Divisible, 
        Hence, we consider 24 instances with 120 active shards (24*5). This will let us have 120/(1+Replica)= 120/2=60 primary shards,
        where each shard will be of (Daily Source Data * 1.25)/Primary Shard Count = (2073.6*1.25)60 = 43GB , that would let us have a growth in shard size(50FB) as well
       
        As recommended to use 3 availability zone, each zone will have 24/3 = 8 instances.

        **Cost Calculation...Monthly**
        Cost for R5.4xlarge instances = 24*HourlyRate*24*30 = 24*1.487*24*30 = **25695.36** ....(XI)
        
        
```

**Index 2**
```
        Retention Period        = 365
        Primary Shard Count     = Math.Ceiling((Daily Source Data * 1.25)/50 GB) [**(III)** ] = 52
        Active Shards           = Primary Shard Count * (1+ Replica) [**(V)**] = 52 ( 1+1) = 104
        Min. Storage Required   = Daily Source Data * (1 + Replica) * Retention Period [**(I)**] = 2073.6*(1+1)*365 = 1513728 GB 
        [Considering r5.4xlarge]
        No. Instances based on Storage      = Math.CEILING(Min. Storage Required / EBS Volume **[VI]**)/3(each document  is updated thrice) = Math.CEILING(1513728/6000/3) = 85 ..(IX)
        No. Instances based on CPU Required = Math.CEILING(Active Shards * Shard vs CPU [**(IV)**]/VCPU Per Instance [**(VII)**)) = 10   ..(X)

        Considering (IX) && (X), we should have 85 instances of R5.4xlarge instances. 


        As recommended to use 3 availability zone, each zone will have 85/3 = 29 instances per zone [85 in one zone]

        **Cost Calculation...Monthly**
        Cost for R5.4xlarge instances = 85*HourlyRate*24*30 = 85*1.487*24*30 = **91004** ....(XI)
        
        
```


**Total Monthly Cost without UltraWarm for both index  = 116699**

##Capacity Planning with ultrawarm Multiple index

**Index 1**
```
        Hot Retention Period        = 7
        Warm Retention Period       = 23
        Primary Shard Count     = Math.Ceiling((Daily Source Data * 1.25)/50 GB) [**(III)** ] = 52
        Active Shards           = Primary Shard Count * (1+ Replica) [**(V)**] = 52 ( 1+1) = 104
        Min. Storage Required   = Daily Source Data * (1 + Replica) * Hot Retention Period [**(I)**] = 2073.6*(1+1)*7 = 29030.4 GB 
        **[Considering r5.4xlarge]**
        No. Instances based on Storage      = Math.CEILING(Min. Storage Required / EBS Volume **[VI]**)/3 = Math.CEILING(29030.4/6000/3) = 2 ..(XII)
        No. Instances based on CPU Required = Math.CEILING(Active Shards * Shard vs CPU [**(IV)**]/VCPU Per Instance [**(VII)**)) = 10   ..(XIII)
        **[Considering UltraWarm large]**
        Storage Requirement     = Daily Source Data * Warm Retention Period = 2073.6*23 = 47693 GB
        UltraWarm Instances Req.= Math.Ceiling(Storage Requirement/ UltraWarm Volume Per Instance) = Math.Ceiling(47693/20000/3) = 1

        Considering (IX) && (X), we should have 10 instances of R5.4xlarge instances. 
        Now, number of active shards should be divisible by number of instances to make sure no data skew.
        So, 104+6 = 110 [divisible by 10], so ideally we should have 110 active shards, ie. 55 primary shards instead of 52. 
        So, we define, number_of_shards in index setting as 55. 
       
        As recommended to use 3 availability zone, each zone will have 10/3 = 3 instances [one zone will have 4]

        **Cost Calculation...Monthly**
        Cost for R5.4xlarge instances = 10*HourlyRate*24*30 = 10*1.487*24*30 = **10706.4**
        Cost for UltraWarm large instances = 1 * HourlyRate * 24  * 30 = 3* 2.68 * 24 *30 = **2000**
        Total Monthly Cost Using UltraWarm = 12706.2 ... (XIV)
       
        
```

**Index 2**
```
        Hot Retention Period        = 7
        Warm Retention Period       = 365-7=358
        Primary Shard Count     = Math.Ceiling((Daily Source Data * 1.25)/50 GB) [**(III)** ] = 52
        Active Shards           = Primary Shard Count * (1+ Replica) [**(V)**] = 52 ( 1+1) = 104
        Min. Storage Required   = Daily Source Data * (1 + Replica) * Hot Retention Period [**(I)**] = 2073.6*(1+1)*7 = 29030.4 GB 
        **[Considering r5.4xlarge]**
        No. Instances based on Storage      = Math.CEILING(Min. Storage Required / EBS Volume **[VI]**) = Math.CEILING(29030.4/6000) = 5 ..(XII)
        No. Instances based on CPU Required = Math.CEILING(Active Shards * Shard vs CPU [**(IV)**]/VCPU Per Instance [**(VII)**))/3 = 4   ..(XIII)
        **[Considering UltraWarm large]**
        Storage Requirement     = Daily Source Data * Warm Retention Period = 2073.6*358 = 742348 GB
        UltraWarm Instances Req.= Math.Ceiling(Storage Requirement/ UltraWarm Volume Per Instance) = Math.Ceiling(742348/20000)/3 = 13

        Considering (IX) && (X), we should have 10 instances of R5.4xlarge instances. 
        Now, number of active shards should be divisible by number of instances to make sure no data skew.
        So, 104+6 = 110 [divisible by 10], so ideally we should have 110 active shards, ie. 55 primary shards instead of 52. 
        So, we define, number_of_shards in index setting as 55. 
       
        As recommended to use 3 availability zone, each zone will have 10/3 = 3 instances [one zone will have 4]

        **Cost Calculation...Monthly**
        Cost for R5.4xlarge instances = 10*HourlyRate*24*30 = 10*1.487*24*30 = **10706.4**
        Cost for UltraWarm large instances = 13 * HourlyRate * 24  * 30 = 13* 2.68 * 24 *30 = **24442**
        Total Monthly Cost Using UltraWarm = 35147... (XIV)
       
        
```

**Total Monthly Cost without UltraWarm for both index  = 116699**
**Total Monthly Cost with UltraWarm for both index  = 47853**

**Using UltraWarm, we can save 41% monthly**

