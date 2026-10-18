package com.swetonyancelmo.event_registration.service;

import com.swetonyancelmo.event_registration.config.JwtProperties;
import com.swetonyancelmo.event_registration.config.SecurityConfig;
import com.swetonyancelmo.event_registration.dtos.LoginRequestDTO;
import com.swetonyancelmo.event_registration.dtos.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.email(), dto.password()));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("event-registration")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .claim(SecurityConfig.ROLES_CLAIM, roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponseDTO(token, "Bearer", jwtProperties.expiration().toSeconds());
    }

}
