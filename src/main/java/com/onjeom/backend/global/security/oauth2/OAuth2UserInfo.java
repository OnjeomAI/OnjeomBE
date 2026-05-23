package com.onjeom.backend.global.security.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
}
