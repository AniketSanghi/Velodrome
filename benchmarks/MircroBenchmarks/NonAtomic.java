import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class MultithreadingDemo extends Thread {
    
    public static ReentrantLock re = new ReentrantLock();
    public static int x = 0;
    public static int y = 0;

    public void run( )
    {   
        x = x + 1;
        // System.out.println("Thread " + Thread.currentThread().getId() + " x's value is " + String.valueOf(x));
        y = x;
    }
}
 
// Main Class
class NonAtomic {
    public static void main(String[] args)
    {   

        int n = 1000; // Number of threads
        MultithreadingDemo objects[] = new MultithreadingDemo[n];
        for (int i = 0; i < n; i++) {
            objects[i] = new MultithreadingDemo();
            objects[i].start();
        }
        for(int i=0; i<n; ++i){
            try{
                objects[i].join();
            }
            catch(Exception e){System.out.println(e);}
        }
    }
}