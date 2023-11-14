package com.semifinished.jdbc.util;


import com.semifinished.config.ConfigProperties;
import com.semifinished.exception.ProjectRuntimeException;
import org.springframework.stereotype.Component;

/**
 * twitter的snowflake算法 -- java实现
 *
 * @author beyond
 */
@Component
public class SnowFlake implements IdGenerator {

    /**
     * 起始的时间戳
     */
    private final static long START_STAMP = 1480166465631L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;  //数据中心
    private final long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStamp = -1L;//上一次时间戳

    public SnowFlake(ConfigProperties configProperties) {
        long dId = configProperties.getDatacenterId();
        if (dId > MAX_DATACENTER_NUM || dId < 0) {
            throw new IllegalArgumentException("数据中心id不能大于31或者小于0");
        }
        long mId = configProperties.getMachineId();
        if (mId > MAX_MACHINE_NUM || mId < 0) {
            throw new IllegalArgumentException("机器标识id不能大于31或者小于0");
        }
        this.datacenterId = dId;
        this.machineId = mId;
    }


    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }

    @Override
    public synchronized String getId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new ProjectRuntimeException("时钟回调，时间戳小于上一次获取");
        }

        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }
        lastStamp = currStamp;

        long id = (currStamp - START_STAMP) << TIMESTAMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;//序列号部分
        return String.valueOf(id);
    }
}
