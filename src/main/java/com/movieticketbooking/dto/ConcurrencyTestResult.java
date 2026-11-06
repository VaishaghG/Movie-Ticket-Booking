package com.movieticketbooking.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for displaying the outcome of the Multithreading Concurrency Demonstration.
 */
public class ConcurrencyTestResult {

    private String targetSeatNumber;
    private Long showId;
    private String winnerCustomer;
    private String rejectedCustomer;
    private String winnerStatus;
    private String rejectedStatus;
    private List<String> executionLogs = new ArrayList<>();

    public ConcurrencyTestResult() {
    }

    public ConcurrencyTestResult(String targetSeatNumber, Long showId) {
        this.targetSeatNumber = targetSeatNumber;
        this.showId = showId;
    }

    public String getTargetSeatNumber() {
        return targetSeatNumber;
    }

    public void setTargetSeatNumber(String targetSeatNumber) {
        this.targetSeatNumber = targetSeatNumber;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getWinnerCustomer() {
        return winnerCustomer;
    }

    public void setWinnerCustomer(String winnerCustomer) {
        this.winnerCustomer = winnerCustomer;
    }

    public String getRejectedCustomer() {
        return rejectedCustomer;
    }

    public void setRejectedCustomer(String rejectedCustomer) {
        this.rejectedCustomer = rejectedCustomer;
    }

    public String getWinnerStatus() {
        return winnerStatus;
    }

    public void setWinnerStatus(String winnerStatus) {
        this.winnerStatus = winnerStatus;
    }

    public String getRejectedStatus() {
        return rejectedStatus;
    }

    public void setRejectedStatus(String rejectedStatus) {
        this.rejectedStatus = rejectedStatus;
    }

    public List<String> getExecutionLogs() {
        return executionLogs;
    }

    public void setExecutionLogs(List<String> executionLogs) {
        this.executionLogs = executionLogs;
    }

    public void addLog(String log) {
        this.executionLogs.add(log);
    }
}
