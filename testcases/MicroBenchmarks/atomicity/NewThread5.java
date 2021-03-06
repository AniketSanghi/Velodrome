package testcases.MicroBenchmarks.atomicity;

public class NewThread5 implements Runnable {

  int index;
  Thread t;
  static MyObject4 obj = new MyObject4(10);
  
  public NewThread5(int i) {
    // Create a new thread
    if (i == 1) {
      index = 1;
      t = new Thread(this, "First Thread");
    } else if (i == 2){
      index = 2;
      t = new Thread(this, "Second Thread");
    } else {
      index = 3;
      t = new Thread(this, "Third Thread");
    }
    t.start(); // Start the thread
  }

  @Override
  public void run() {
    access();
  }
  
  public void access() {
    try {
      if (index == 1) {
        System.out.println("Thread 1 writing to obj for the first time");
        obj.a = 10;
        Thread.sleep(10000);
        obj.a = 40;
        System.out.println("Thread 1 writing to obj for the second time");
      } else if (index == 2) {
        System.out.println("Thread 2 writing to obj for the first time");
        obj.a = 50;
        Thread.sleep(5000);
//        obj.a = 60;
//        System.out.println("Thread 2 writing to obj for the second time");
      } else if (index == 3) {
        System.out.println("Thread 3 writing to obj for the first time");
        obj.a = 100;
      } else {
        // Do nothing
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
}
