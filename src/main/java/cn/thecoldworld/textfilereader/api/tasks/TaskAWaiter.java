package cn.thecoldworld.textfilereader.api.tasks;

public final class TaskAWaiter<TResult> {
    private final Task<TResult> m_Task;

    public TaskAWaiter(Task<TResult> task) {
        m_Task = task;
    }

    public synchronized void Return() {
        this.notifyAll();
    }

    public TResult GetResult() {
        while (!m_Task.isReturned()) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException ignored) {
            }
        }
        return m_Task.getResult();
    }

    public TResult GetResultOrThrow() throws Exception {
        while (!m_Task.isReturned()) {
            synchronized (this) {
                this.wait();
            }
        }
        return m_Task.getResult();
    }
}
