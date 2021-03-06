package com.ssafy.runnershi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ssafy.runnershi.entity.Custom;
import com.ssafy.runnershi.entity.UserInfo;

public interface CustomRepository extends JpaRepository<Custom, Long> {

  public Custom findByUser_UserId_UserId(String userId);

  public Custom findByUser(UserInfo userInfo);

}
