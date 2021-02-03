package com.lzr.Expction;

public class FailExp extends RuntimeException{
    public FailExp(String message) {
        super(message);
    }
}
