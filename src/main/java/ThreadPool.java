import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by andrey on 22.10.17.
 */
public class ThreadPool  implements Executor{
    private volatile boolean isRunning = true;
    int currentCount= 0;
    int countThread=0;
//    Queue<Runnable> queueTask = new ConcurrentLinkedQueue<>();
     final Queue<Runnable>  queueTask= new LinkedBlockingQueue<>();
    public ThreadPool(int countThread) {
        this.countThread=countThread;
       // start();
    }

/*    private void start(){
        for(int i=0; i< countThread;i++){
            new Thread(new TaskWorker()).start();
        }
    }*/

    private void createThread(){
        new Thread(new TaskWorker()).start();
    }


    @Override
    public void execute(Runnable command) {
        if (command != null) {
            synchronized (this) {
                if(currentCount<countThread) {
                    createThread();
                    currentCount++;
                }
                if (!queueTask.offer(command)) System.out.println("Task can not add in Queue");
            }
        }
    }

    public void shutDown(){
        isRunning = false;
    }


    private final class TaskWorker implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                final Runnable nextTask;
                synchronized (this) {
                     nextTask = queueTask.poll();
                }

                if (nextTask != null) {
                    nextTask.run();
                }else{
                    synchronized (this){
                        currentCount--;
                    }
                    return;
                }
            }
        }
    }

}
