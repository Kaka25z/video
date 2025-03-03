package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.video.core.entity.CustomResponse;
import com.video.core.entity.User;
import com.video.core.service.user.UserAccountService;
import com.video.core.service.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserService userService;

    /**
     * 注册接口
     * @param map 包含 username password confirmedPassword 的 map
     * @return CustomResponse对象
     */
    // 前端使用axios传递的data是Content-Type: application/json，需要用@RequestBody获取参数
    @PostMapping("/user/account/register")
    public CustomResponse register(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");;

        CustomResponse customResponse = new CustomResponse();

        try {
            return userAccountService.register(username, password, confirmedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("未知错误");
            return customResponse;
        }
    }

    /**
     * 登录接口
     * @param map 包含 username password 的 map
     * @return CustomResponse对象
     */
    @PostMapping("/user/account/login")
    public CustomResponse login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.login(username, password);
    }

    /**
     * 管理员登录接口
     * @param map 包含 username password 的 map
     * @return CustomResponse对象
     */
    @PostMapping("/admin/account/login")
    public CustomResponse adminLogin(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.adminLogin(username, password);
    }

    /**
     * 获取当前登录用户信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/user/personal/info")
    public CustomResponse personalInfo() {
        return userAccountService.personalInfo();
    }

    /**
     * 获取当前登录管理员信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/admin/personal/info")
    public CustomResponse adminPersonalInfo() {
        return userAccountService.adminPersonalInfo();
    }

    /**
     * 退出登录接口
     */
    @GetMapping("/user/account/logout")
    public void logout() {
        userAccountService.logout();
    }

    /**
     * 管理员退出登录接口
     */
    @GetMapping("/admin/account/logout")
    public void adminLogout() {
        userAccountService.adminLogout();
    }

    /**
     * 修改当前用户密码
     * @param pw    就密码
     * @param npw   新密码
     * @return  响应对象
     */
    @PostMapping("/user/password/update")
    public CustomResponse updatePassword(@RequestParam("pw") String pw, @RequestParam("npw") String npw) {
        return userAccountService.updatePassword(pw, npw);
    }

    @GetMapping("/user/account/get-all")
    public CustomResponse getAllUsers() {
        List<User> users = userService.getAllUsers();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(users);
        return customResponse;
    }

    @PostMapping("/admin/account/update-user-profile")
    public CustomResponse updateUserProfile(@RequestParam("uid") Integer uid,
                                            @RequestParam("nickname") String nickname,
                                            @RequestParam("role") Integer role,
                                            @RequestParam("state") Integer state,
                                            @RequestParam("auth") Integer auth,
                                            @RequestParam("authMsg") String authMsg) {
        try {
            return userService.updateUserProfile(uid, nickname, role, state, auth, authMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "更新失败", null);
        }
    }

    @PostMapping("/admin/account/update-password")
    public CustomResponse adminUpdatePassword(@RequestParam("uid") Integer uid, @RequestParam("npw") String npw) {
        return userAccountService.adminUpdatePassword(uid, npw);
    }
}
