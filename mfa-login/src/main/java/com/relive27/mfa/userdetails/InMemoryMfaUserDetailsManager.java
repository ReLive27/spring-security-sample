package com.relive27.mfa.userdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/2/3 19:58
 */
@Slf4j
public class InMemoryMfaUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {
    private final Map<String, UserDetails> users = new HashMap<>();
    private AuthenticationManager authenticationManager;


    public InMemoryMfaUserDetailsManager() {
    }

    public InMemoryMfaUserDetailsManager(UserDetails... users) {
        UserDetails[] userDetails = users;
        int length = users.length;

        for (int i = 0; i < length; ++i) {
            UserDetails user = userDetails[i];
            this.createUser(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MfaUserDetails user = (MfaUserDetails) this.users.get(username.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException(username);
        } else {
            return new MfaUserDetails(user.getUsername(), user.getPassword(), user.isEnableMfa(), user.getSecret(), user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities());
        }
    }

    @Override
    public void createUser(UserDetails user) {
        Assert.isTrue(!this.userExists(user.getUsername()), "user should not exist");
        this.users.put(user.getUsername().toLowerCase(), user);
    }

    @Override
    public void updateUser(UserDetails user) {
        Assert.isTrue(this.userExists(user.getUsername()), "user should exist");
        this.users.put(user.getUsername().toLowerCase(), user);
    }

    @Override
    public void deleteUser(String username) {
        this.users.remove(username.toLowerCase());
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (currentUser == null) {
            throw new AccessDeniedException("Can't change password as no Authentication object found in context for current user.");
        } else {
            String username = currentUser.getName();
            log.debug(String.format("Changing password for user '%s'", username));
            if (this.authenticationManager != null) {
                log.debug(String.format("Reauthenticating user '%s' for password change request.", username));
                this.authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
            } else {
                log.debug("No authentication manager set. Password won't be re-checked.");
            }

            MfaUserDetails user = (MfaUserDetails) this.users.get(username);
            Assert.state(user != null, "Current user doesn't exist in database.");
            user.setPassword(newPassword);
        }
    }

    @Override
    public boolean userExists(String username) {
        return this.users.containsKey(username.toLowerCase());
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        String username = user.getUsername();
        MfaUserDetails mfaUserDetails = (MfaUserDetails) this.users.get(username.toLowerCase());
        mfaUserDetails.setPassword(newPassword);
        return mfaUserDetails;
    }
}
