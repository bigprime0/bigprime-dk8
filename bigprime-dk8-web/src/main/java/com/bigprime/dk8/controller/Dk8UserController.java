package com.bigprime.dk8.controller;

import com.bigprime.das.core.handler.UserHandler;
import com.bigprime.das.core.model.AccountLogin;
import com.bigprime.das.core.model.AccountLoginToken;
import com.bigprime.das.core.model.Result;
import com.bigprime.das.core.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lyw
 * @version 1.0
 */
@RestController
@RequestMapping("auth")
@Tag(name = "认证管理")
@AllArgsConstructor
public class Dk8UserController {
    private final UserHandler userHandler;

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<AccountLoginToken> login(@RequestBody AccountLogin login) {
        return Result.ok(userHandler.login(login));
    }

    @PostMapping("logout")
    @Operation(summary = "退出")
    public Result<Boolean> logout(HttpServletRequest request) {
        return Result.ok(userHandler.logout(TokenUtils.getAccessToken(request)));
    }

    @PostMapping("check-token/{token}")
    @Operation(summary = "检查token")
    public Result<AccountLoginToken> checkToken(@PathVariable("token") String token) {
        return Result.ok(userHandler.checkToken(token));
    }
}
