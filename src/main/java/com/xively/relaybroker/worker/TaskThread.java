package com.xively.relaybroker.worker;

import java.util.concurrent.atomic.AtomicBoolean;

public class TaskThread extends Thread {

    private AtomicBoolean run = new AtomicBoolean(true);

    private Runnable runnable;

    private final Object lock = new Object();


    public TaskThread() {
        super("TaskThread");
    }


    public void run(Runnable newRunnable) {
        synchronized (lock) {
            System.out.println("Got the lock. Assign new task and notify task thread");
            if (runnable != null) {
                throw new IllegalStateException("Already running a task!");
            }
            runnable = newRunnable;
            lock.notifyAll();
        }
    }


    public void stopXivelyPublisher() {
        synchronized (lock) {
            System.out.println("Stop task thread");
            run.set(false);
        }
    }


    public void run() {
        boolean ran = false;
        while (run.get()) {
            System.out.println("In task thread, look for new tasks");
            synchronized (lock) {
                System.out.println("Got the lock. Check whether it needs to run any task");
                try {
                    waitForRunnable();
                    ran = executeRunnable();
                } catch (Throwable exceptionInRunnable) {
                    System.out.println("Error while executing the Runnable: " + exceptionInRunnable);
                } finally {
                    cleanupRunnable();
                    if (ran) {
                        ran = false;
                    }
                }
            }
        }
        System.out.println("Task thread is down");
    }


    private boolean executeRunnable() {
        if (runnable == null) {
            return false;
        }
        System.out.println("Got a new task to run");
        System.out.println("Run the task");
        runnable.run();
        return true;
    }


    private void cleanupRunnable() {
        synchronized (lock) {
            System.out.println("Task execution over");
            runnable = null;
        }
    }


    private void waitForRunnable() {
        while (runnable == null && run.get()) {
            System.out.print(".");
            try {
                lock.wait(500);
            } catch (InterruptedException e) {
                System.out.println("Task thread was interrupted." + e);
            }
        }
    }
}