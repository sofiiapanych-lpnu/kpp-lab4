package lab4.prog;

import javafx.beans.property.SimpleStringProperty;

public class ThreadInfoModel {
    private final SimpleStringProperty threadName;
    private final SimpleStringProperty status;
    private final SimpleStringProperty result;

    public ThreadInfoModel(String threadId, String status, String result) {
        this.threadName = new SimpleStringProperty(threadId);
        this.status = new SimpleStringProperty(status);
        this.result = new SimpleStringProperty(result);
    }
    public SimpleStringProperty getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadId) {
        this.threadName.set(threadId);
    }

    public SimpleStringProperty getThreadStatus() {
        return status;
    }

    public void setThreadStatus(String status) {
        this.status.set(status);
    }

    public SimpleStringProperty getThreadResult() {
        return result;
    }

    public void setThreadResult(String result) {
        this.result.set(result);
    }
}