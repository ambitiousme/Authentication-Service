package com.auth.Auth.Service.Security;

import com.auth.Auth.Service.Entity.User;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


// This class is used to add userid so that it can be places in jwt to avoid DB call while validation.
@Getter
@ToString
public class CustomUserDetails implements UserDetails {

    private  String userId;
    private  String username;
    private  String password;
    private  Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<GrantedAuthority> authorities) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        //this.authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        this.authorities = authorities;
    }

//    @Override
//    public boolean isAccountNonExpired() { return true; }
//
//    @Override
//    public boolean isAccountNonLocked() { return true; }
//
//    @Override
//    public boolean isCredentialsNonExpired() { return true; }
//
//    @Override
//    public boolean isEnabled() { return true; }
//

}
