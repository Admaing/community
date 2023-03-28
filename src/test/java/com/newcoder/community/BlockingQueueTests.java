package com.newcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {
    public static void main(String[] args){
//        生产者不断生产数据
        BlockingQueue blockingQueue = new ArrayBlockingQueue(10);
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
    }
}

class Producer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for(int i=0; i<100; i++){
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName()+"生产:"+queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


class Consumer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while(true){
                //消费者能力不如生产者
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName()+"消费"+queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

