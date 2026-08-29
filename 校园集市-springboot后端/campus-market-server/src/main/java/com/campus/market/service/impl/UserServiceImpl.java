package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.JwtUtil;
import com.campus.market.common.PageResult;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.dto.UserAuthDTO;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;
import com.campus.market.entity.UserAuth;
import com.campus.market.mapper.CreditLogMapper;
import com.campus.market.mapper.UserAuthMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户与认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserAuthMapper userAuthMapper;
    private final CreditLogMapper creditLogMapper;

    /**
     * 内存验证码存储：phone -> (code, 过期时间戳)
     * TODO: 骨架阶段模拟发送（日志打印），上线前接真实短信服务商（阿里云/腾讯云），
     *       并改用 Redis 存储验证码以支持多实例部署
     */
    private static final Map<String, SmsCode> SMS_CODES = new ConcurrentHashMap<>();
    private static final long SMS_EXPIRE_MS = 5 * 60 * 1000L;

    /** 验证码记录 */
    private record SmsCode(String code, long expireAt) {
    }

    @Override
    public void sendSms(String phone) {
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        SMS_CODES.put(phone, new SmsCode(code, System.currentTimeMillis() + SMS_EXPIRE_MS));
        // TODO: 接入真实短信服务
        log.info("【模拟短信】向 {} 发送验证码：{}（5 分钟内有效）", phone, code);
    }

    @Override
    public Map<String, Object> register(RegisterDTO dto) {
        // 1. 校验短信验证码
        SmsCode sc = SMS_CODES.get(dto.getPhone());
        if (sc == null || sc.expireAt() < System.currentTimeMillis() || !sc.code().equals(dto.getSmsCode())) {
            throw new BizException("验证码错误或已过期");
        }
        // 2. 手机号唯一校验（uk_phone 唯一索引兜底）
        long cnt = count(new QueryWrapper<User>().eq("phone", dto.getPhone()));
        if (cnt > 0) {
            throw new BizException("该手机号已注册，请直接登录");
        }
        // 3. 创建用户（密码 BCrypt 加密，信用分默认 100）
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname())
                ? dto.getNickname()
                : "校园用户" + dto.getPhone().substring(7));
        user.setGender(0);
        user.setAuthStatus(0);
        user.setIsRunner(0);
        user.setCreditScore(100);
        user.setStatus(0);
        save(user);
        SMS_CODES.remove(dto.getPhone());
        // 注册成功直接返回 token 自动登录
        return buildLoginResult(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = getOne(new QueryWrapper<User>().eq("phone", dto.getPhone()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BizException("账号已被封禁，请联系管理员");
        }
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        return buildLoginResult(user);
    }

    @Override
    public User info(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user; // password 字段已用 @JsonIgnore 屏蔽
    }

    @Override
    public void submitAuth(Long userId, UserAuthDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 学号查重：一个学号只能认证一个账号（uk_student_no 唯一索引兜底）
        Long exist = userAuthMapper.selectCount(new QueryWrapper<UserAuth>()
                .eq("student_no", dto.getStudentNo())
                .in("audit_status", 0, 1));
        if (exist != null && exist > 0) {
            throw new BizException("该学号已提交过认证，请勿重复提交");
        }
        UserAuth auth = new UserAuth();
        auth.setUserId(userId);
        auth.setType(dto.getType()); // 1普通认证 2跑男认证（管理端审核通过后同步置 user.is_runner=1）
        auth.setStudentNo(dto.getStudentNo());
        auth.setRealName(dto.getRealName());
        auth.setCollege(dto.getCollege());
        auth.setGrade(dto.getGrade());
        auth.setDormBuilding(dto.getDormBuilding());
        auth.setMaterialUrl(dto.getMaterialUrl());
        auth.setAuditStatus(0); // 待审核
        userAuthMapper.insert(auth);
        // 用户表认证状态置为"审核中"
        user.setAuthStatus(1);
        updateById(user);
    }

    @Override
    public PageResult<CreditLog> creditLogs(Long userId, int page, int size) {
        Page<CreditLog> p = creditLogMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<CreditLog>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
                        .orderByDesc("id"));
        return PageResult.of(p);
    }

    /** 构造登录/注册返回：{token, userInfo}，与前端约定一致 */
    private Map<String, Object> buildLoginResult(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("gender", user.getGender());
        userInfo.put("authStatus", user.getAuthStatus());
        userInfo.put("isRunner", user.getIsRunner());
        userInfo.put("creditScore", user.getCreditScore());

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generate(user.getId()));
        result.put("userInfo", userInfo);
        return result;
    }
}
