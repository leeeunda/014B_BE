package com.html.cifarm.security.info;


import com.html.cifarm.constants.Constants;
import com.html.cifarm.dto.global.ExceptionDto;
import com.html.cifarm.dto.response.JwtTokenDto;
import com.html.cifarm.exception.ErrorCode;
import com.html.cifarm.utility.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONValue;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationResponse {
    public static void makeLoginSuccessResponse(HttpServletResponse response, JwtTokenDto jwtTokenDto, Integer refreshExpiration){
        CookieUtil.addCookie(response, Constants.ACCESS_COOKIE_NAME, jwtTokenDto.accessToken());
        CookieUtil.addSecureCookie(response, Constants.REFRESH_COOKIE_NAME, jwtTokenDto.refreshToken(), refreshExpiration);
    }
    public static void makeLogoutSuccessResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        Map<String, Object> body = new HashMap<>();
        body.put("success", "true");
        body.put("data", null);
        body.put("error", null);

        response.getWriter().write(JSONValue.toJSONString(body));
    }

    public static void makeFailureResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        Map<String, Object> body= new HashMap<>();
        body.put("success", false);
        body.put("data", null);
        body.put("error", ExceptionDto.of(errorCode));

        response.getWriter().write(JSONValue.toJSONString(body));

    }
}