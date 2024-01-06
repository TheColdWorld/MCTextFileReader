package cn.thecoldworld.textfilereader.api.tasks;

import cn.thecoldworld.textfilereader.variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Task<TResult> {
    protected final List<Consumer<TResult>> callbacks;
    protected final TaskAWaiter<TResult> TaskAWaiter;
    protected TaskAction<TResult> action;
    protected TResult result;
    protected boolean Returned;

    public Task(TaskAction<TResult> run, List<Consumer<TResult>> callbacks) {
        this.action = run;
        this.callbacks = callbacks;
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public Task(TaskAction<TResult> run) {
        this.action = run;
        this.callbacks = List.of();
        TaskAWaiter = new TaskAWaiter<>(this);
    }

    public final boolean isReturned() {
        return Returned;
    }

    public void Start() {
        if (this.Returned) return;
        this.result = action.Run();
        this.Returned = true;
        TaskAWaiter.Return();
        callbacks.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.accept(result), 0, TimeUnit.MICROSECONDS));
    }

    public final @Nullable TResult getResult() {
        return result;
    }

    public final @NotNull TResult getResultOrThrow() {
        return Objects.requireNonNull(result);
    }

    protected void SetAction(TaskAction<TResult> Action) {
        this.action = Action;
    }

    public final TaskAWaiter<TResult> getAWaiter() {
        return TaskAWaiter;
    }

    public interface TaskAction<TResult> {
        TResult Run();
    }
}
