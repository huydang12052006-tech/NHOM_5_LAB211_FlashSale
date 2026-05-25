package com.mycompany.flashsale.simulation.model;

import java.time.LocalDateTime;

public class FlashSaleEvent extends BaseEntity {

    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // UPCOMING, ACTIVE, ENDED

    public FlashSaleEvent() {
        super();
    }

    public FlashSaleEvent(String id, String eventName, LocalDateTime startTime, LocalDateTime endTime, String status) {
        super();
        this.id = id;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.id, this.eventName, this.startTime.toString(), this.endTime.toString(), this.status,
                this.createdAt.toString(), this.updatedAt.toString()
        );
    }

    public static FlashSaleEvent fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        FlashSaleEvent e = new FlashSaleEvent();
        e.setId(parts[0]);
        e.setEventName(parts[1]);
        e.setStartTime(LocalDateTime.parse(parts[2]));
        e.setEndTime(LocalDateTime.parse(parts[3]));
        e.setStatus(parts[4]);
        e.setCreatedAt(LocalDateTime.parse(parts[5]));
        e.setUpdatedAt(LocalDateTime.parse(parts[6]));
        return e;
    }
}
