package com.example.currencies.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String eventLog;

  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  public Event(String eventLog) {
    this.eventLog = eventLog;
  }

  @PrePersist
  public void prePersist() {
    this.date = LocalDateTime.now();
  }
}
