import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by andrey on 22.10.17.
 */
public class ThreadPool  implements Executor{
    private volatile boolean isRunning = true;
    int countThread=0;
//    Queue<Runnable> queueTask = new ConcurrentLinkedQueue<>();
     final Queue<Runnable>  queueTask= new LinkedBlockingQueue<>();
    public ThreadPool(int countThread) {
        this.countThread=countThread;
        start();
    }

    private void start(){
        for(int i=0; i< countThread;i++){
            new Thread(new TaskWorker()).start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command != null) {
            synchronized (this) {
                if (!queueTask.offer(command)) System.out.println("Task ca not add in Queue");
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
                }
            }
        }
    }

}
