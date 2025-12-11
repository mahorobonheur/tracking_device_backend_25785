package mahoro.backend.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.RoleType;
import mahoro.backend.model.User;
import mahoro.backend.repository.UserRepository;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Loading OIDC user from {}", userRequest.getClientRegistration().getRegistrationId());
       
        OidcUser oidcUser = delegate.loadUser(userRequest);
        
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        
        log.info("OIDC user email: {}, name: {}", email, name);
        
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OIDC response");
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.info("Existing user found: {}", user.getEmail());
        } else {
         
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setLocationAssigned(false);
            newUser.setActive(false);
            newUser.setRole(RoleType.USER);            
            userRepository.save(newUser);
            log.info("Created new user: {}", newUser.getEmail());
        }
        
        return oidcUser;
    }
}