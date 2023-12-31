package radon.engine.tasks;

import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.graphics.Graphics;
import radon.engine.logging.Log;
import radon.engine.util.types.Singleton;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class TaskManager extends RadonSystem {

    private static final int TASK_POP_TIMEOUT = 16;

    private static final Task TERMINATION_TASK = new TerminationTask();

    @Singleton
    private static TaskManager instance;
    
    public static void submitGraphicsTask(Task task) {

        if(task == null) {
            Log.error("Task cannot be null", new NullPointerException());
            return;
        }

        if(Graphics.isGraphicsThread()) {
            task.perform();
        } else {
            instance.graphicsTaskQueue.add(task);
        }
    }
    
    public static void submitTask(Task task) {
        instance.submit(task);
    }
    
    public static int taskCount() {
        return instance.taskQueue.size();
    }


    private final AtomicBoolean running;
    private final BlockingQueue<Task> taskQueue;
    private final Queue<Task> graphicsTaskQueue;
    private final ExecutorService taskThread;
    private final TaskProcessor taskProcessor;

    private TaskManager(RadonSystemManager systemManager) {
        super(systemManager);

        running = new AtomicBoolean(false);
        taskQueue = new PriorityBlockingQueue<>();
        graphicsTaskQueue = new ArrayDeque<>();

        taskThread = newSingleThreadExecutor(runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("TaskManager Thread");
            thread.setDaemon(true);
            return thread;
        });

        taskProcessor = new TaskProcessor();
    }

    @Override
    protected void init() {
        running.set(true);
        taskThread.submit(this::run);
    }

    @Override
    protected void terminate() {

        taskQueue.add(TERMINATION_TASK);
        running.set(false);
        taskProcessor.shutdown();
        taskThread.shutdown();

        Log.info("[TASK-MANAGER]: Waiting for " + taskCount() + " tasks to complete...");

        try {
            taskThread.awaitTermination(1000, MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for TaskManager to terminate", e);
        }
    }

    public void executeGraphicsTasks() {

        while(!graphicsTaskQueue.isEmpty()) {

            Task task = graphicsTaskQueue.poll();

            if(task.state() != Task.State.CANCELED) {
                task.perform();
            }
        }
    }

    private void submit(Task task) {

        if(task == null) {
            Log.error("Cannot submit a null task");
            return;
        }

        if(task.state() == Task.State.CANCELED) {
            return;
        }

        taskQueue.add(task);
    }

    private void run() {

        while(running.get()) {

            final Task task = popTask();

            if(task == TERMINATION_TASK) {
                return;
            }

            if(task != null && task.state() != Task.State.CANCELED) {
                taskProcessor.submit(task);
            }
        }
    }

    private Task popTask() {
        try {
            return taskQueue.take();
        } catch (InterruptedException e) {
            Log.error("TaskManager thread has been interrupted", e);
        }
        return null;
    }


    private static final class TerminationTask extends Task {
        @Override
        protected void perform() {

        }
    }
}
