package com.advancedweb2022groupylt.finalpj.service.logic.http;

import com.advancedweb2022groupylt.finalpj.bean.entity.User;
import com.advancedweb2022groupylt.finalpj.bean.http.request.LoginRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.request.RegisterRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.response.LoginResponse;
import com.advancedweb2022groupylt.finalpj.bean.http.response.Message;
import com.advancedweb2022groupylt.finalpj.bean.http.response.RegisterResponse;
import com.advancedweb2022groupylt.finalpj.repository.UserRepository;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService
{
    public static final int OK=0,TOO_SHORT=1,TOO_LONG=2,UNMATCH=4;

    @Resource
    private UserRepository userRepository;

    @Resource
    private JWTService jwtService;


    @Override
    public LoginResponse login(LoginRequest request)
    {
        String username = request.getUsername();
        String password = request.getPassword();
        LoginResponse response = null;
        if (username==null||username.equals(""))
            response = new LoginResponse("",new Message(false,"用户名错误"));
        else
        {
            User user = userRepository.findUserByUsername(username);
            if (user==null)
                response = new LoginResponse("",new Message(false,"用户名错误"));
            else
            {
                if (user.getPassword().equals(password))
                {
                    String token = jwtService.generateToken(username);
                    response = new LoginResponse(token,new Message(true,"登陆成功"));
                    user.setLastLoginTime(user.getCurrentLoginTime());
                    user.setCurrentLoginTime(new Date().getTime());
                    userRepository.save(user);
                }
                else
                {
                    response = new LoginResponse("",new Message(false,"密码错误"));
                }
            }
        }
        return response;
    }

    @Override
    public RegisterResponse register(RegisterRequest request)
    {
        String username = request.getUsername();
        String password = request.getPassword();
        int usernameStatus = checkStatus(username);
        int passwordStatus = checkStatus(password);
        RegisterResponse response = null;
        if (usernameStatus==OK&&passwordStatus==OK)
        {
            String email = request.getEmail();
            if(mailCheck(email))
            {
                User existingUser = userRepository.findUserByUsername(username);
                if (existingUser != null)
                    response = new RegisterResponse(new Message(false, "用户名已存在"));
                else
                {
                    User user = new User(0L, username, email, password);
                    userRepository.save(user);
                    response = new RegisterResponse(new Message(true, "注册成功"));
                }
            }
            else response = new RegisterResponse(new Message(false,"邮箱地址无效"));
        }
        else if (usernameStatus==OK) // password not ok
        {
            String msg = "";
            switch (passwordStatus)
            {
                case TOO_SHORT:
                    msg = "密码过短，需要至少6位";
                    break;
                case TOO_LONG:
                    msg = "密码过长，需要至多20位";
                    break;
                case UNMATCH:
                    msg = "密码只能包含数字和字母";
                    break;
            }
            response = new RegisterResponse(new Message(false,msg));
        }
        else
        {            
            String msg = "";
            switch (usernameStatus)
            {
                case TOO_SHORT:
                    msg = "用户名过短，需要至少6位";
                    break;
                case TOO_LONG:
                    msg = "用户名过长，需要至多20位";
                    break;
                case UNMATCH:
                    msg = "用户名只能包含数字和字母";
                    break;
            }
            response = new RegisterResponse(new Message(false,msg));
        }
        return response;
    }

    private static int checkStatus(String string)
    {
        if (string.length()<6)
            return TOO_SHORT;
        if (string.length()>20)
            return TOO_LONG;
        if (string.matches("[0-9a-zA-Z]*"))
            return OK;
        return UNMATCH;
    }

    private static boolean mailCheck(String emailAddr)
    {
        return emailAddr.matches("([^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22([^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22)(\\x2e([^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22([^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22))*\\x40([^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b([^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d)(\\x2e([^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b([^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d))*(\\.\\w{2,})+");
    }
}
