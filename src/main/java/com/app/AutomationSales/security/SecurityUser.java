package com.app.AutomationSales.security;

import com.app.AutomationSales.appUser.model.Role;
import com.app.AutomationSales.appUser.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The logged-in user as Spring Security sees it.
 * Inject it in a controller with {@code @AuthenticationPrincipal SecurityUser me}.
 */
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final Role role;
    private final Long salesPersonId;
    private final boolean enabled;

    public SecurityUser(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.salesPersonId = user.getSalesPerson() != null ? user.getSalesPerson().getId() : null;
        this.enabled = user.isEnabled();
    }

    public Long getUserId() { return userId; }

    public Role getRole() { return role; }

    /** The SalesPerson record this login belongs to (null for ADMIN). */
    public Long getSalesPersonId() { return salesPersonId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix is what hasRole('ADMIN') looks for
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return password; }

    @Override public String getUsername() { return email; }

    @Override public boolean isEnabled() { return enabled; }
}
