package com.advancedweb2022groupylt.finalpj.service.jwtService;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class JWTServiceImpl implements JWTService
{
    private static final String ENCRYPT_METHOD = "HS256";

    @Resource
    private JWTConfig jwtConfig;

    @Override
    public String generateToken(String username)
    {
        Map<String, String> header = new LinkedHashMap<>();
        header.put("typ", "jwt");
        header.put("alg", ENCRYPT_METHOD);
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("username", username);
        payload.put("exptime", Long.valueOf(new Date().getTime() + jwtConfig.getTimeout()).toString());
        String headerJsonStr = JSON.toJSONString(header);
        String payloadJsonStr = JSON.toJSONString(payload);
        String signature = generateSignature(payloadJsonStr);
        return Base64.encode(headerJsonStr) + "." + Base64.encode(payloadJsonStr) + "." + Base64.encode(signature);
    }

    @Override
    public JWTStatus checkStatus(String token)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(token,".");
            String headerJsonStr = Base64.decodeStr(st.nextToken());
            String payloadJsonStr = Base64.decodeStr(st.nextToken());
            String signature = Base64.decodeStr(st.nextToken());
            Map<String,String> header = (HashMap<String,String>)(JSONObject.parseObject(headerJsonStr,HashMap.class));
            if (!header.containsKey("alg")||!(header.get("alg").equals(ENCRYPT_METHOD)))
                return JWTStatus.WRONG;
            if (generateSignature(payloadJsonStr).equals(signature))
            {
                if (getExpTime(token)>=new Date().getTime())
                    return JWTStatus.VALID;
                else
                    return JWTStatus.EXPIRED;
            }
            else
            {
                return JWTStatus.WRONG;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return JWTStatus.WRONG;
        }
    }

    @Override
    public Map<String, String> getPayload(String token)
    {
        StringTokenizer st = new StringTokenizer(token,".");
        String headerJsonStr = Base64.decodeStr(st.nextToken());
        String payloadJsonStr = Base64.decodeStr(st.nextToken());
        Map<String,String> payload = (HashMap<String, String>)(JSONObject.parseObject(payloadJsonStr,HashMap.class));
        return payload;
    }

    public long getExpTime(String token)
    {
        Map<String,String> payload = getPayload(token);
        return Long.parseLong(payload.get("exptime"));
    }


    private String generateSignature(String payload)
    {
        return sha256(payload+jwtConfig.getSecret());
    }

    private static String sha256(String string)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
            return new String(messageDigest.digest(), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
