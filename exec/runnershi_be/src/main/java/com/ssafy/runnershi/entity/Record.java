package com.ssafy.runnershi.entity;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.DynamicUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
public class Record {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long recordId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  private UserInfo user;

  private double distance;

  @Temporal(TemporalType.TIMESTAMP)
  private Date endTime;

  private int runningTime;
  private String title;
}
