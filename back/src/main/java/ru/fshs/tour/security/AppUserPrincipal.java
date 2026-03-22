package ru.fshs.tour.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.fshs.tour.domain.user.User;

@Getter
@RequiredArgsConstructor
public class AppUserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String name;
    private final String userType;

    public static AppUserPrincipal from(User user) {
        return new AppUserPrincipal(
                user.getId(),
                user.getLogin(),
                user.getPasswordHash(),
                user.getName(),
                user.getUserType()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.toUpperCase(Locale.ROOT)));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
