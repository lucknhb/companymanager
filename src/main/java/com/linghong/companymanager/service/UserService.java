package com.linghong.companymanager.service;

import com.linghong.companymanager.constant.UrlConstant;
import com.linghong.companymanager.pojo.Company;
import com.linghong.companymanager.pojo.User;
import com.linghong.companymanager.pojo.Wallet;
import com.linghong.companymanager.repository.CompanyRepository;
import com.linghong.companymanager.repository.UserRepository;
import com.linghong.companymanager.utils.BeanUtil;
import com.linghong.companymanager.utils.FastDfsUtil;
import com.linghong.companymanager.utils.IDUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: luck_nhb
 * @Date: 2018/11/7 10:11
 * @Version 1.0
 * @Description:
 */
@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private UserRepository userRepository;
    @Resource
    private CompanyRepository companyRepository;

    public User register(User user) {
        User register = userRepository.findByMobilePhone(user.getMobilePhone());
        if (register == null) {
            ByteSource salt = ByteSource.Util.bytes(user.getMobilePhone());
            String newPassword = new SimpleHash("MD5",
                    user.getPassword(),
                    salt, 2)
                    .toHex();
            user.setPassword(newPassword);
            user.setStatus(true);
            user.setCreateTime(new Date());
            user = userRepository.save(user);
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(user.getMobilePhone(),user.getPassword());
            token.setRememberMe(true);
            subject.login(token);
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setCreateTime(new Date());
            wallet.setWalletId(IDUtil.getId());
            wallet.setBalance(new BigDecimal(0));
            return user;
        }
        return null;
    }

    public User login(User user) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()){
           UsernamePasswordToken token = new UsernamePasswordToken(user.getMobilePhone(),user.getPassword() );
           token.setRememberMe(true);
           subject.login(token);
        }
        user = userRepository.findByMobilePhone(user.getMobilePhone());
        return user;
    }

    public User findPassword(String mobilePhone, String password) {
        User user = userRepository.findByMobilePhone(mobilePhone);
        ByteSource salt = ByteSource.Util.bytes(mobilePhone);
        String newPassword = new SimpleHash("MD5", password,
                salt, 2)
                .toHex();
        user.setPassword(newPassword);
        return user;
    }

    public User perfectUserMessage(User user,Long companyId,User sessionUser) {
        Company company = companyRepository.findById(companyId).get();
        sessionUser = userRepository.findByMobilePhone(sessionUser.getMobilePhone());
        BeanUtil.copyPropertiesIgnoreNull(sessionUser, user);
        user.setFromCompany(company);
        userRepository.save(user);
        return user;
    }

    public User uploadAvatar(String base64Image, User user) {
        String url = UrlConstant.IMAGE_URL+new FastDfsUtil().uploadBase64Image(base64Image);
        user = userRepository.findByMobilePhone(user.getMobilePhone());
        user.setAvatar(url);
        return user;
    }

    public User getUserByMobilePhone(String mobilePhone) {
        return userRepository.findByMobilePhone(mobilePhone);
    }

    public User getUserById(Long userId) {
        User user = userRepository.findById(userId).get();
        return user;
    }

    public boolean pushBusinessTarget(Long userId, String target) {
        User user = userRepository.findById(userId).get();
        user.setBusinessTarget(target);
        return true;
    }
}