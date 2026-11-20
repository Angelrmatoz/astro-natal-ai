package org.astronatalai.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth")
@Getter
@Setter
public class AuthCookieProperties {

    private String cookieName = "AUTH_TOKEN";
    private boolean secure = false;
    private String sameSite = "Lax";
}
