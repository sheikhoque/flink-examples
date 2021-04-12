package com.att.dtv.kda.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.kinesis.connectors.flink.KinesisShardAssigner;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RoundRobinKinesisShardAssigner implements KinesisShardAssigner, Serializable {
    private static Logger LOG = LoggerFactory.getLogger(RoundRobinKinesisShardAssigner.class);
    Map<String, Integer> shardSubtaskMapping;
    Map<Integer, Integer> roundRobinDistribution;

    public RoundRobinKinesisShardAssigner(){
        shardSubtaskMapping = new HashMap<>();
        roundRobinDistribution = new HashMap<>();
    }
    @Override
    public int assign(StreamShardHandle shard, int numParallelSubtasks) {
        String shardId = shard.getShard().getShardId();
        Integer candidateTask;
        if((candidateTask = shardSubtaskMapping.get(shardId))!=null)
            return candidateTask;

        if(roundRobinDistribution.isEmpty()){
            for(int i =0;i<numParallelSubtasks;i++){
                roundRobinDistribution.put(i, 0);
            }
            roundRobinDistribution.put(0, 1);
            shardSubtaskMapping.put(shardId, 0);
        }else{
            int currentMax=roundRobinDistribution.get(0);
            boolean candidateTaskFound=false;
            for(int i =1;i<numParallelSubtasks;i++){
                if(currentMax > roundRobinDistribution.get(i)){
                    roundRobinDistribution.put(i, roundRobinDistribution.get(i)+1);
                    shardSubtaskMapping.put(shardId, i);
                    candidateTaskFound=true;
                    break;
                }
            }
            if(!candidateTaskFound){
                roundRobinDistribution.put(0, roundRobinDistribution.get(0)+1);
                shardSubtaskMapping.put(shardId, 0);
            }
        }
        LOG.info(" ShardId: {} goes to task {}", shardId, shardSubtaskMapping.get(shardId));
        return shardSubtaskMapping.get(shardId);
    }
}
