package com.newcoder.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
//测试代码怎么引用主类呢？
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
//        redisTemplate = new RedisTemplate<>();
        System.out.println(redisTemplate);

//        String redisKey = "test:count";
//        redisTemplate.opsForValue().set(redisKey,1);
//
//        System.out.println(redisTemplate.opsForValue().get(redisKey));
//        System.out.println(redisTemplate.opsForValue().increment(redisKey));
//        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
//        System.out.println(redisTemplate.opsForValue().get(redisKey));
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
    }
//    @Test
//    public void testHashes(){
//        String redisKey = "test:user";
//        redisTemplate.opsForHash().put();
//    }

//  多次访问同一个key  绑定key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }
//    事务中不能查询

//    编程式事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
//                启用事务
                operations.multi();
                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"wangsu");
                operations.opsForSet().add(redisKey,"zhangssn");
                System.out.println(operations.opsForSet().members(redisKey));
                return operations.exec();
//                return null;
            }
        });
        System.out.println(obj);
    }

//    统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for (int i = 1; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 1; i < 100000; i++) {
            int r = (int)(Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

//    将3组数据合并，在统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 1; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:02";
        for (int i = 1; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:02";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }

        String unionKey = "test:h11:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    // 统计一组数据的bool值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
//        redisTemplate.opsForValue().setBit(redisKey,1,true);
//        查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        Object obj  = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3,2,true);
        redisTemplate.opsForValue().setBit(redisKey3,3,true);
        redisTemplate.opsForValue().setBit(redisKey3,4,true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4,4,true);
        redisTemplate.opsForValue().setBit(redisKey4,5,true);
        redisTemplate.opsForValue().setBit(redisKey4,6,true);

        String redisKey = "test:bm:or";

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
//                        存放到第一个key里面
                        redisKey.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());

            }
        });
        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,6));


    }

//    统计3组数据的bool ，并对3组数据做OR运算
}
