package cn.thecoldworld.textfilereader.api.tasks;

import cn.thecoldworld.textfilereader.variables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutReturnTask<TResult> extends Task<TResult> {

    protected Runnable action;

    public OutReturnTask(Runnable run, List<Consumer<TResult>> callbacks) {
        super(() -> null, callbacks);
        this.action = run;
    }

    public OutReturnTask(Runnable run) {
        super(() -> null);
        this.action = run;
    }

    public OutReturnTask(List<Consumer<TResult>> callbacks) {
        super(() -> null, callbacks);
        this.action = () -> {
        };
    }

    public OutReturnTask() {
        super(() -> null);
        this.action = () -> {
        };
    }

    public void Return(@NotNull TResult result) {
        this.result = result;
        this.Returned = true;
        TaskAWaiter.Return();
        callbacks.forEach(i -> variables.scheduledExecutorService.schedule(() -> i.accept(result), 0, TimeUnit.MICROSECONDS));
    }


    @Override
    public void Start() {
        if (this.Returned) return;
        variables.scheduledExecutorService.schedule(action, 0, TimeUnit.MICROSECONDS);
        this.Returned = false;
    }

    protected void SetAction(Runnable Action) {
        this.action = Action;
    }

    /**
     * @deprecated
     */
    @Override
    @Contract("_->fail")
    protected void SetAction(TaskAction<TResult> Action) {
        throw new IllegalStateException();
    }
}
