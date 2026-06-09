package model.Entity;

import java.time.LocalDateTime;
import model.BaseEntity.BaseEntity;
import model.Enum.SaleStatus;
public class FlashSaleEvent extends BaseEntity {

    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SaleStatus status; // UPCOMING, ACTIVE, ENDED

    public FlashSaleEvent() {
        super();
    }

    public FlashSaleEvent(String id, LocalDateTime createdAt,
            LocalDateTime updatedAt,String eventName, LocalDateTime startTime, LocalDateTime endTime, SaleStatus status) {
        super(id, createdAt, updatedAt);
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

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                this.getId(),
                this.getCreatedAt().toString(),
                this.getUpdatedAt().toString(),
                this.eventName,
                this.startTime.toString(),
                this.endTime.toString(),
                this.status.name()
        );
    }
    @Override
    public void fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        this.setId(parts[0]);
        this.setCreatedAt(LocalDateTime.parse(parts[1]));
        this.setUpdatedAt(LocalDateTime.parse(parts[2]));
        this.setEventName(parts[3]);
        this.setStartTime(LocalDateTime.parse(parts[4]));
        this.setEndTime(LocalDateTime.parse(parts[5]));
        this.setStatus(SaleStatus.valueOf(parts[6]));
    }
}
