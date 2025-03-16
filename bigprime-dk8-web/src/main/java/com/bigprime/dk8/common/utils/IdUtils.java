package com.bigprime.dk8.common.utils;

import com.bigprime.dk8.common.utils.id.SeqIdGenerator;
import com.bigprime.dk8.common.utils.id.SnowflakeIdGenerator;
import com.bigprime.dk8.common.utils.id.TimestampIdGenerator;
import com.bigprime.dk8.common.utils.id.UUIDGenerator;
import org.apache.commons.codec.digest.DigestUtils;

public class IdUtils {
    public static String getUUId(boolean Is32) {
        UUIDGenerator uuidGenerator = new UUIDGenerator();
        return Is32 ? uuidGenerator.get(32) : uuidGenerator.get(64);
    }

    public static String getTSId() {
        TimestampIdGenerator timestampIdGenerator = new TimestampIdGenerator();
        return timestampIdGenerator.get();
    }

    public static long getSnowId() {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1);
        long id = snowflakeIdGenerator.get();
        return id;
    }

    public static int getSeqId() {
        return SeqIdGenerator.get();
    }

    public static void main( String[] args ) {
        //the original password
        String a = "123456";
        String b = DigestUtils.sha1Hex(DigestUtils.sha1(a.getBytes())).toUpperCase();
        //output the 2 stage encrypted password.
        System.out.println("*"+b);
    }
}
