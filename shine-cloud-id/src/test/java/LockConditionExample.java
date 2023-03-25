import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockConditionExample {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void printOdd() throws InterruptedException {
        lock.lock();
        try {
            for (int i = 1; i <= 10; i += 2) {
                System.out.println("Odd: " + i);
                condition.signal(); // 唤醒等待的线程
                condition.await(); // 将当前线程挂起
            }
            condition.signal(); // 唤醒最后一个等待的线程
        } finally {
            lock.unlock();
        }
    }

    public void printEven() throws InterruptedException {
        lock.lock();
        try {
            for (int i = 2; i <= 10; i += 2) {
                System.out.println("Even: " + i);
                condition.signal(); // 唤醒等待的线程
                condition.await(); // 将当前线程挂起
            }
            condition.signal(); // 唤醒最后一个等待的线程
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        LockConditionExample example = new LockConditionExample();
        Thread t1 = new Thread(() -> {
            try {
                example.printOdd();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                example.printEven();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        t2.start();
    }
}
