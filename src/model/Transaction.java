package model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class Transaction {
    protected int id;
    protected Integer studentId;
    protected double amount;
    protected Timestamp timestamp;

    public Transaction() {}

    public Transaction(Integer studentId, double amount) {
        this.studentId = studentId;
        this.amount = amount;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Transaction(int id, Integer studentId, double amount, Timestamp timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public abstract boolean processTransaction(Connection conn) throws SQLException;
}

