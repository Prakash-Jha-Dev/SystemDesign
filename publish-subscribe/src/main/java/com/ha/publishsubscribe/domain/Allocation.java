package com.ha.publishsubscribe.domain;

import java.util.Objects;

public class Allocation implements Comparable<Allocation> {
    private String topicName;
    private int partition;

    public Allocation(String topicName, int partition) {
        this.topicName = topicName;
        this.partition = partition;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getPartition() {
        return partition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        }

        Allocation oAllocation = (Allocation) obj;
        return topicName.equals(oAllocation.getTopicName()) && partition == oAllocation.getPartition();
    }    

    @Override
    public int hashCode() {
        return Objects.hash(topicName, partition);
    }

    @Override
    public String toString() {
        return topicName+ " " + Integer.toString(partition);
    }

    @Override
    public int compareTo(Allocation obj) {
        if (topicName.equals(obj.getTopicName())) {
            if (partition == obj.getPartition()) {
                return 0;
            } else if (partition < obj.getPartition()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return topicName.compareTo(obj.getTopicName());
        }
    }
}
