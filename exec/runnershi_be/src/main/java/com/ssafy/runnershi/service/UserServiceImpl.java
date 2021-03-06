package com.ssafy.runnershi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.runnershi.entity.Custom;
import com.ssafy.runnershi.entity.Friend;
import com.ssafy.runnershi.entity.Profile;
import com.ssafy.runnershi.entity.SearchResult;
import com.ssafy.runnershi.entity.User;
import com.ssafy.runnershi.entity.UserInfo;
import com.ssafy.runnershi.entity.UserResult;
import com.ssafy.runnershi.repository.CustomRepository;
import com.ssafy.runnershi.repository.FriendRepository;
import com.ssafy.runnershi.repository.UserInfoRepository;
import com.ssafy.runnershi.repository.UserRepository;


@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private UserInfoRepository userInfoRepo;

  @Autowired
  private CustomRepository customRepo;

  @Autowired
  private FriendRepository friendRepo;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private PasswordEncodingService pwdEncoding;

  @Autowired
  private SetOperations<String, String> set;

  @Autowired
  private ZSetOperations<String, String> zset;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Override
  public UserResult signInKakao(String accessToken) {
    String reqURL = "https://kapi.kakao.com/v2/user/me";
    try {
      URL url = new URL(reqURL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      // ????????? ????????? Header??? ????????? ??????
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      // int responseCode = conn.getResponseCode();
      // System.out.println("responseCode : " + responseCode);

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

      String line = "";
      String result = "";

      while ((line = br.readLine()) != null) {
        result += line;
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode kakaoUserInfo = mapper.readTree(result);

      String userId = kakaoUserInfo.path("id").asText();
      User user = userRepo.findByUserId(userId);

      UserResult userResult = new UserResult();

      // ????????????
      if (user == null) {

        userResult.setUserId(userId);
        return userResult;

      }

      if (user.getFlag() == 1) {
        userResult.setResult("deleted user");
        return userResult;
      }

      // ?????????
      userResult.setToken(jwtService.create(user.getUserId()));
      userResult.setUserId(user.getUserId());
      userResult.setUserName(user.getUserName());
      Custom custom = customRepo.findByUser_UserId_UserId(user.getUserId());
      if (custom != null)
        userResult.setRunningType(custom.getRunningType());

      return userResult;

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

  }


  @Override
  public UserResult signInNaver(String accessToken) {
    String reqURL = "https://openapi.naver.com/v1/nid/me";
    try {
      URL url = new URL(reqURL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      // ????????? ????????? Header??? ????????? ??????
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      // int responseCode = conn.getResponseCode();
      // System.out.println("responseCode : " + responseCode);

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

      String line = "";
      String result = "";

      while ((line = br.readLine()) != null) {
        result += line;
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode naverUserInfo = mapper.readTree(result);

      String userId = naverUserInfo.path("response").path("id").asText();
      User user = userRepo.findByUserId(userId);

      UserResult userResult = new UserResult();

      // ????????????
      if (user == null) {

        userResult.setUserId(userId);
        return userResult;

      }

      if (user.getFlag() == 1) {
        userResult.setResult("deleted user");
        return userResult;
      }

      // ?????????
      userResult.setToken(jwtService.create(user.getUserId()));
      userResult.setUserId(user.getUserId());
      userResult.setUserName(user.getUserName());
      Custom custom = customRepo.findByUser_UserId_UserId(user.getUserId());
      if (custom != null)
        userResult.setRunningType(custom.getRunningType());

      return userResult;

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }


  @Override
  public UserResult enterName(User idName) {

    if (idName == null || idName.getUserId() == null || "".equals(idName.getUserId())
        || idName.getUserName() == null)
      return null;

    UserResult userResult = new UserResult();

    if (!isValidName(idName.getUserName())) {
      userResult.setResult("invalid name");
      return userResult;
    }

    User user = new User();
    user.setUserId(idName.getUserId());
    user.setUserName(idName.getUserName());
    user.setHasCustom((byte) 0);
    user.setFlag((byte) 0);
    userRepo.save(user);

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(user);
    userInfo.setUserName(user);
    userInfoRepo.save(userInfo);

    String value = user.getUserId() + ";" + user.getUserName();

    set.add(value, value);
    zset.add("totalDistanceRank", value, userInfo.getTotalDistance());
    zset.add("totalTimeRank", value, userInfo.getTotalTime());
    zset.add("totalPaceRank", value, userInfo.getBestPace());

    zset.add("weeklyDistanceRank", value, userInfo.getWeeklyDistance());
    zset.add("weeklyTimeRank", value, userInfo.getWeeklyTime());
    zset.add("weeklyPaceRank", value, userInfo.getWeeklyPace());

    userResult.setResult("SUCCESS");
    userResult.setToken(jwtService.create(user.getUserId()));
    userResult.setUserId(user.getUserId());
    userResult.setUserName(user.getUserName());

    userResult.setResult("SUCCESS");
    userResult.setToken(jwtService.create(user.getUserId()));
    userResult.setUserId(user.getUserId());
    userResult.setUserName(user.getUserName());

    return userResult;
  }

  @Override
  public UserResult enterRunningType(UserResult userResult) {

    if (userResult == null || userResult.getRunningType() == null || userResult.getUserId() == null
        || userResult.getToken() == null || userResult.getUserName() == null)
      return null;

    UserInfo userInfo = userInfoRepo.findByUserId_UserId(userResult.getUserId());

    if (userInfo == null) {
      userResult.setRunningType(null);
      userResult.setResult("invalid id");
    }


    Custom custom = customRepo.findByUser_UserId_UserId(userInfo.getUserId().getUserId());
    if (custom == null) {
      custom = new Custom();
    }
    custom.setUser(userInfo);
    custom.setRunningType(userResult.getRunningType());
    customRepo.save(custom);

    return userResult;
  }

  @Override
  public Map nameChk(String name) {

    HashMap<String, String> map = new HashMap<String, String>();

    if (isValidName(name)) {
      map.put("result", "valid");
      map.put("validName", name);
      return map;
    }

    map.put("result", "invalid name");
    return map;
  }

  @Override
  public Map emailChk(String email) {

    HashMap<String, String> map = new HashMap<String, String>();

    if (!isValidEmail(email)) {
      map.put("result", "invalid email");
      return map;
    }


    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email); // ?????????????????? ?????? ????????? ?????? ????????? ????????? ??????

    // ?????? ?????? ?????? ??????
    Random random = new Random();
    String key = "";
    for (int i = 0; i < 3; i++) {
      int index = random.nextInt(25) + 65; // A~Z?????? ?????? ????????? ??????
      key += (char) index;
    }
    int numIndex = random.nextInt(8999) + 1000; // 4?????? ????????? ??????
    key += numIndex;
    message.setSubject("[???????????????] ??????????????? ????????? ??????????????? ??????????????????.");
    message.setText("?????? ?????? : " + key);

    javaMailSender.send(message);

    map.put("result", "valid");
    map.put("code", key);
    map.put("validEmail", email);

    return map;

  }


  @Override
  public UserResult signUpRunHi(User user) {
    if (user == null || user.getEmail() == null || user.getUserName() == null
        || user.getPwd() == null)
      return null;

    UserResult userResult = new UserResult();

    SimpleDateFormat SDF = new SimpleDateFormat("yyMMddHHmmssS");
    user.setUserId(SDF.format(new Date()));

    if (!isValidEmail(user.getEmail())) {
      System.out.println("?????????");
      userResult.setResult("invalid email");
      return userResult;
    }


    if (!isValidName(user.getUserName())) {
      System.out.println("????????????");
      userResult.setResult("invalid name");
      return userResult;
    }

    if (!isValidPwd(user.getPwd())) {
      userResult.setResult("invalid pwd");
      System.out.println("pwd");
      return userResult;
    }

    while (userRepo.findByUserId(user.getUserId()) != null)
      user.setUserId(SDF.format(new Date()));

    String pwd = user.getPwd();
    user.setPwd(pwdEncoding.encode(pwd));
    user.setHasCustom((byte) 0);
    user.setFlag((byte) 0);
    userRepo.save(user);

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(user);
    userInfo.setUserName(user);
    userInfoRepo.save(userInfo);

    String value = user.getUserId() + ";" + user.getUserName();

    set.add(value, value);
    zset.add("totalDistanceRank", value, userInfo.getTotalDistance());
    zset.add("totalTimeRank", value, userInfo.getTotalTime());
    zset.add("totalPaceRank", value, userInfo.getBestPace());

    zset.add("weeklyDistanceRank", value, userInfo.getWeeklyDistance());
    zset.add("weeklyTimeRank", value, userInfo.getWeeklyTime());
    zset.add("weeklyPaceRank", value, userInfo.getWeeklyPace());

    userResult.setResult("SUCCESS");
    userResult.setToken(jwtService.create(user.getUserId()));
    userResult.setUserId(user.getUserId());
    userResult.setUserName(user.getUserName());

    return userResult;
  }


  @Override
  public UserResult signInRunHi(User user) {

    if (user.getEmail() == null || user.getPwd() == null)
      return null;

    UserResult userResult = new UserResult();

    User chkUser = userRepo.findByEmail(user.getEmail());
    if (chkUser == null) {
      userResult.setResult("no user");
      return userResult;
    }

    if (chkUser.getFlag() == 1) {
      userResult.setResult("deleted user");
      return userResult;
    }

    if (pwdEncoding.matches(user.getPwd(), chkUser.getPwd())) {

      userResult.setToken(jwtService.create(chkUser.getUserId()));
      userResult.setUserId(chkUser.getUserId());
      userResult.setUserName(chkUser.getUserName());
      Custom custom = customRepo.findByUser_UserId_UserId(chkUser.getUserId());
      if (custom != null)
        userResult.setRunningType(custom.getRunningType());

      userResult.setResult("SUCCESS");
      return userResult;
    }

    userResult.setResult("no match pwd");
    return userResult;
  }


  public boolean isValidEmail(String email) {

    boolean err = false;

    // ????????????
    if (userRepo.findByEmail(email) != null)
      return err;

    // ????????? ??????
    // ????????? ??????
    String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(email);
    if (m.matches()) {
      err = true;
    }
    return err;
  }

  public boolean isValidName(String name) {
    boolean err = false;

    // ????????????
    if (userRepo.findByUserName(name) != null)
      return err;

    // ????????? ??????
    // ?????? | ?????? | ?????? | - | _ | .
    String regex = "^[_.a-zA-Z0-9???-???-]*$";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(name);
    if (m.matches() && 1 <= name.length() && name.length() <= 20) {
      err = true;
    }

    return err;
  }

  public boolean isValidPwd(String pwd) {

    boolean err = false;

    // ????????? ??????
    // ???????????? (??????, ??????, ???????????? ?????? 8~20?????? ??????)
    String regex = "^.*(?=^.{8,20}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=~]).*$";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(pwd);
    if (m.matches()) {
      err = true;
    }

    return err;

  }


  @Override
  public String pwdChk(User user) {

    User chkUser = userRepo.findByUserId(user.getUserId());
    if (chkUser == null || !chkUser.getEmail().equals(user.getEmail())) {
      return "no match";
    }

    if (pwdEncoding.matches(user.getPwd(), chkUser.getPwd())) {
      return "SUCCESS";
    }

    return "no match";
  }


  @Override
  public String leave(String userId) {

    User user = userRepo.findByUserId(userId);
    if (user == null)
      return "no match id";

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 60);

    user.setFlag((byte) 1);
    user.setExpiryDate(cal.getTime());
    userRepo.save(user);

    String oldValue = user.getUserId() + ";" + user.getUserName();
    zset.remove("totalDistanceRank", oldValue);
    zset.remove("totalTimeRank", oldValue);
    zset.remove("totalPaceRank", oldValue);

    zset.remove("weeklyDistanceRank", oldValue);
    zset.remove("weeklyTimeRank", oldValue);
    zset.remove("weeklyPaceRank", oldValue);

    redisTemplate.delete(oldValue);

    ArrayList<Friend> friendList = friendRepo.findByUser_UserId_UserId(user.getUserId());
    for (Friend friend : friendList) {
      User friendUser = friend.getFriendUser().getUserName();
      set.remove(friendUser.getUserId() + ";" + friendUser.getUserName(), oldValue);
    }


    return "SUCCESS";
  }


  // ?????? ?????? 5?????? ?????? 60?????? ?????? ???????????? DB?????? ??????
  @Scheduled(cron = "0 0 5 * * ?", zone = "Asia/Seoul")
  public void deleteUser() throws ParseException {

    ArrayList<User> deleteList = userRepo.findByExpiryDateLessThanEqual(new Date());

    for (User user : deleteList) {
      userRepo.delete(user);
    }

  }


  @Override
  public List<SearchResult> searchUser(String userId, String word) {
    word = word.trim();
    if ("".equals(word) || word == null)
      return null;
    return userRepo.findByUserNameContaining(word);
  }


  @Override
  public Profile getUserProfile(String userId) {
    UserInfo userInfo = userInfoRepo.findByUserId_UserId(userId);
    if (userInfo == null)
      return null;
    Profile profile =
        new Profile(userInfo.getUserId().getUserId(), userInfo.getUserName().getUserName(),
            zset.reverseRank("totalDistanceRank",
                userInfo.getUserId().getUserId() + ";" + userInfo.getUserName().getUserName()) + 1,
            userInfo.getTotalDistance(), userInfo.getTotalTime(), userInfo.getTotalDay(),
            userInfo.getBestPace(), userInfo.getWeeklyDistance(), userInfo.getWeeklyTime(),
            userInfo.getWeeklyPace());
    return profile;
  }


  @Override
  public Profile updateUserName(String userId, String userName) {
    User user = userRepo.findByUserId(userId);
    if (user == null || userName == null || "".equals(userName))
      return null;

    // ?????? ????????? ??????????????? ??????
    String oldValue = user.getUserId() + ";" + user.getUserName();
    zset.remove("totalDistanceRank", oldValue);
    zset.remove("totalTimeRank", oldValue);
    zset.remove("totalPaceRank", oldValue);

    zset.remove("weeklyDistanceRank", oldValue);
    zset.remove("weeklyTimeRank", oldValue);
    zset.remove("weeklyPaceRank", oldValue);

    redisTemplate.delete(oldValue);

    ArrayList<Friend> friendList = friendRepo.findByUser_UserId_UserId(user.getUserId());
    for (Friend friend : friendList) {
      User friendUser = friend.getFriendUser().getUserName();
      set.remove(friendUser.getUserId() + ";" + friendUser.getUserName(), oldValue);
    }

    user.setUserName(userName);
    userRepo.save(user);

    UserInfo userInfo = userInfoRepo.findByUserId_UserId(userId);
    if (userInfo == null)
      return null;

    // ?????? ????????? ???????????? ??????
    String value = user.getUserId() + ";" + user.getUserName();
    zset.add("totalDistanceRank", value, userInfo.getTotalDistance());
    zset.add("totalTimeRank", value, userInfo.getTotalTime());
    zset.add("totalPaceRank", value, userInfo.getBestPace());

    zset.add("weeklyDistanceRank", value, userInfo.getWeeklyDistance());
    zset.add("weeklyTimeRank", value, userInfo.getWeeklyTime());
    zset.add("weeklyPaceRank", value, userInfo.getWeeklyPace());

    set.add(value, value);

    friendList = friendRepo.findByUser_UserId_UserId(userInfo.getUserId().getUserId());
    for (Friend friend : friendList) {
      User friendUser = friend.getFriendUser().getUserName();
      set.add(value, friendUser.getUserId() + ";" + friendUser.getUserName());
    }

    // profile ??????
    Profile profile =
        new Profile(userInfo.getUserId().getUserId(), userInfo.getUserName().getUserName(),
            zset.reverseRank("totalDistanceRank",
                userInfo.getUserId().getUserId() + ";" + userInfo.getUserName().getUserName()) + 1,
            userInfo.getTotalDistance(), userInfo.getTotalTime(), userInfo.getTotalDay(),
            userInfo.getBestPace(), userInfo.getWeeklyDistance(), userInfo.getWeeklyTime(),
            userInfo.getWeeklyPace());

    return profile;
  }


}
