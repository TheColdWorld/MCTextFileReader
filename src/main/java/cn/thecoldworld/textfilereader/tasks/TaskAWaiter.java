package cn.thecoldworld.textfilereader.tasks;

public class TaskAWaiter<TResult, _Task extends Task<TResult>> {
    private final _Task m_Task;

    public TaskAWaiter(_Task task) {
        m_Task = task;
    }

    public synchronized void Return() {
        this.notifyAll();
    }

    public synchronized TResult GetResult() {
        while (!m_Task.isReturned()) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }
        return m_Task.getResult();
    }

    public synchronized TResult GetResultOrThrow() throws Exception {
        while (!m_Task.isReturned()) {
            this.wait();
        }
        return m_Task.getResult();
    }
}
