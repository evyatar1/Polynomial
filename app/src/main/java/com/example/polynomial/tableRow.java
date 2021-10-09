package com.example.polynomial;

import java.io.Serializable;

public class tableRow implements Serializable { // inheritance in order to send this type to another activity
    private int funcID, power;
    private double coefficient;

    public tableRow(int funcID, double coefficient, int power) {
        this.funcID = funcID;
        this.power = power;
        this.coefficient = coefficient;
    }

    public int getPower() {
        return power;
    }

    public int getFuncID() {
        return funcID;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }
}
