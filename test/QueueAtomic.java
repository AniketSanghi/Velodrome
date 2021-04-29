package test;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;


public class QueueAtomic extends Thread {

    // for random enque deque parts
    Random rand = new Random();
    // the number of threads
    static final int NumThreads = 2;
    static final int ITERS = 100000;
    // queue details
    static Queue<Integer> q = new LinkedList<>();
    static int size = 0;
    // the atomicity lock
    public static ReentrantLock re = new ReentrantLock();
    // enqueue operation
    public void enqueue(int x) {
        re.lock();
            q.add(x);
            ++size;
            // System.out.println("E " + x);
        re.unlock();
    }
    // dequeue operation
    public void dequeue(){
        re.lock();
            if(size == 0){
                // System.out.println("EMPTY");
                re.unlock();
                return;
            }
            int x = q.remove();
            --size;
            // System.out.println("D " + x);
        re.unlock();
        return;
    }
    //read the top element
    public void read(){
        re.lock();
            if(size == 0){
                // System.out.println("EMPTY");
                re.unlock();
                return;
            }
        int y = q.peek();
        // System.out.println("R " + y);
        re.unlock();
    }

    //deciding the operation
    @Override
    public void run() {

        for(int i=0; i<ITERS; ++i){
            int operatn = i%3;
            if(operatn == 0)
                this.enqueue(rand.nextInt(3));
            else if(operatn == 1)
                this.dequeue();
            else if(operatn == 2)
                this.read();
        }

        return;
    }

    // the main function
    public static void main(String args[]) throws Exception {

        QueueAtomic objects[] = new QueueAtomic[NumThreads];
        for (int i = 0; i < NumThreads; i++) {
            objects[i] = new QueueAtomic();
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
