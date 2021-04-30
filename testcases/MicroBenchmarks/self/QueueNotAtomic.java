package testcases.MicroBenchmarks.self;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;


public class QueueNotAtomic extends Thread {

    // for random enque deque parts
    Random rand = new Random();
    // the number of threads
    static final int NumThreads = 2;
    static final int ITERS = 100;
    // queue details
    static Queue<Integer> q = new LinkedList<>();
    static int size = 0;
    // the atomicity lock
    public static ReentrantLock re = new ReentrantLock();
    // enqueue operation
    public void enqueue(int x) {
        q.add(x);
        ++size;
        // System.out.println("E " + x);
    }
    // dequeue operation
    public void dequeue(){
        if(size == 0){
            // System.out.println("EMPTY");
            return;
        }
        try{
            int x = q.remove();
            --size;
            // System.out.println("D " + x);
        
        }
        finally{
            return;    
        }
    }
    //read the top element
    public void read(){
        if(size == 0){
            // System.out.println("EMPTY");
            return;
        }
        try{
            int y = q.peek();
            // System.out.println("R " + y);
        }
        finally{
            return;
        }
    }

    //deciding the operation
    @Override
    public void run() {
        for(int i=0; i<ITERS; ++i){
            int operatn = i%3;
            if(operatn == 0)
                this.enqueue(i);
            else if(operatn == 1)
                this.dequeue();
            else if(operatn == 2)
                this.read();
        }

        return;
    }

    // the main function
    public static void main(String args[]) throws Exception {

        QueueNotAtomic objects[] = new QueueNotAtomic[NumThreads];
        for (int i = 0; i < NumThreads; i++) {
            objects[i] = new QueueNotAtomic();
            objects[i].start();
        }
        for(int i=0; i < NumThreads; ++i){
            try{
                objects[i].join();
            }
            catch(Exception e){System.out.println(e);}
        }
    }
}
