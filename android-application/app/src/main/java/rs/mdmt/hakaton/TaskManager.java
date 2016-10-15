package rs.mdmt.hakaton;


import android.os.Handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskManager
{
    public interface Function
    {
        void call();
    }

    public interface ReturningFunction<T>
    {
        T call();
    }

    private final static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final static int CORE_POOL_SIZE = 8;
    private final static int MAXIMUM_POOL_SIZE = 8;
    private final static int KEEP_ALIVE_TIME = 1;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static TaskManager instance = new TaskManager();
    private static Handler mainHandler;

    private ExecutorService[] threadPools = new ExecutorService[Task.TOTAL];

    private TaskManager()
    {
        threadPools[Task.SERVER] = Executors.newFixedThreadPool(1);
        threadPools[Task.GENERAL] = Executors.newFixedThreadPool(CORE_POOL_SIZE - 1);

        mainHandler = new Handler();
    }

    public static TaskManager getInstance()
    {
        return instance;
    }

    public void execute(Runnable runnable)
    {
        threadPools[Task.GENERAL].execute(runnable);
    }

    public static Handler getMainHandler()
    {
        return mainHandler;
    }

    public void execute(final Function f)
    {
        threadPools[Task.GENERAL].execute(new Runnable()
        {
            @Override
            public void run()
            {
                f.call();
            }
        });
    }

    public void executeAndPost(final Function async, final Function main)
    {
        threadPools[Task.GENERAL].execute(new Runnable()
        {
            @Override
            public void run()
            {
                async.call();
                mainHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        main.call();
                    }
                });
            }
        });
    }

    public <T extends Object> Future<T> execute(Callable<T> callable)
    {
        return threadPools[Task.GENERAL].submit(callable);
    }

    public void execute(final Task task)
    {
        threadPools[task.type].execute(new Runnable()
        {
            @Override
            public void run()
            {
                task.execute();
            }
        });
    }

    public void executeAndPost(final Task.Ui task)
    {
        threadPools[task.type].execute(new Runnable()
        {
            @Override
            public void run()
            {
                task.execute();
                mainHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        task.uiExecute();
                    }
                });
            }
        });
    }
}
