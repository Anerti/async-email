package com.async.mail.config;

import com.async.mail.entity.User;
import com.async.mail.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourcesAccessRules {

  public boolean grantAccessFor(User user) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String authenticatedUserId = auth.getName();
    String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

    return authenticatedUserId.equals(user.id().toString())
        || (role.equals("ADMIN") && user.role().equals(UserRole.CUSTOMER));
  }
}
