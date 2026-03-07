package me.combimagnetron.sunscreen.util;

import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderThreadPoolHandler;

import java.util.concurrent.*;

public class Scheduler {
    private final static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);
    private final static ExecutorService VirtualExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    public static void delayTick(Runnable code) {
        SCHEDULED_EXECUTOR_SERVICE.schedule(code, 50L, TimeUnit.MILLISECONDS);
    }

    public static void async(Runnable code) {
        VirtualExecutorService.execute(code);
    }

    public static <T> T run(Callable<T> code) {
        try {
            return VirtualExecutorService.submit(code).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delay(Runnable code, long delay) {
        RenderThreadPoolHandler.EXECUTOR_SERVICE.schedule(code, delay, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> repeat(Runnable code, Duration duration) {
        return SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(code, 0L, duration.time(), duration.timeUnit());
    }

    public record Duration(long time, TimeUnit timeUnit) {
    }

    public static <T> T async(Callable<T> runnable) {
        try {
            return SCHEDULED_EXECUTOR_SERVICE.schedule(runnable, 0L, TimeUnit.MICROSECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


}
