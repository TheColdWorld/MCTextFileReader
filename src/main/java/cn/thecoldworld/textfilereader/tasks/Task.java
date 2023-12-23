package cn.thecoldworld.textfilereader.tasks;

import cn.thecoldworld.textfilereader.variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Task<TResult> {
    private final List<Consumer<TResult>> callbacks;
    private final TaskAWaiter<TResult, Task<TResult>> TaskAWaiter;
    private Runnable runnable;
    private TResult result;
    private boolean Returned;

    public Task(Runnable run, List<Consumer<TResult>> callbacks) {
        this.runnable = run;
        this.callbacks = callbacks;
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public Task(Runnable run) {
        this.runnable = run;
        this.callbacks = List.of();
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public Task(List<Consumer<TResult>> callbacks) {
        this.runnable = () -> {
        };
        this.callbacks = callbacks;
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public Task() {
        this.runnable = () -> {
        };
        this.callbacks = List.of();
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public void Return(@NotNull TResult result) {
        this.result = result;
        this.Returned = true;
        TaskAWaiter.Return();
        callbacks.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.accept(result), 0, TimeUnit.MICROSECONDS));
    }

    public boolean isReturned() {
        return Returned;
    }

    public void Start() {
        if (this.Returned) return;
        variables.scheduledExecutorService.schedule(runnable, 0, TimeUnit.MICROSECONDS);
        this.Returned = false;
    }

    public @Nullable TResult getResult() {
        return result;
    }

    protected void SetAction(Runnable Action) {
        this.runnable = Action;
    }

    public cn.thecoldworld.textfilereader.tasks.TaskAWaiter<TResult, Task<TResult>> getAWaiter() {
        return TaskAWaiter;
    }
}
