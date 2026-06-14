package model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private String nis;
    private String name;
    private String studentClass;
    private double savingsBalance;
    private Timestamp createdAt;
    private boolean isActive;

    public Student() {}

    public Student(int id, String nis, String name, String studentClass, double savingsBalance, Timestamp createdAt, boolean isActive) {
        this.id = id;
        this.nis = nis;
        this.name = name;
        this.studentClass = studentClass;
        this.savingsBalance = savingsBalance;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public Student(int id, String nis, String name, String studentClass, double savingsBalance, Timestamp createdAt) {
        this(id, nis, name, studentClass, savingsBalance, createdAt, true);
    }

    public Student(String nis, String name, String studentClass, double savingsBalance) {
        this.nis = nis;
        this.name = name;
        this.studentClass = studentClass;
        this.savingsBalance = savingsBalance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public double getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(double savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return name + " (" + nis + ") - " + studentClass;
    }
}

