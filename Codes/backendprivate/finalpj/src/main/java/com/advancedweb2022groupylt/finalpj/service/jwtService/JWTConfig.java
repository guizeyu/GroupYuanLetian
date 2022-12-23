package com.advancedweb2022groupylt.finalpj.service.jwtService;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

// note : this class should have been put under "config" package
// but for convenience , it is put here
@Component
@ConfigurationProperties(prefix = "jwtconfig")
@PropertySource(value = {"classpath:application.yml"})
public class JWTConfig
{
    private String secret;
    private long timeout;

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
}
