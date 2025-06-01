package com.datpvc.identity_service.service;

import com.datpvc.identity_service.dto.request.AuthenticationRequest;
import com.datpvc.identity_service.dto.request.IntrospectRequest;
import com.datpvc.identity_service.dto.request.LogoutRequest;
import com.datpvc.identity_service.dto.response.AuthenticationResponse;
import com.datpvc.identity_service.dto.response.IntrospectResponse;
import com.datpvc.identity_service.entity.InvalidatedToken;
import com.datpvc.identity_service.entity.User;
import com.datpvc.identity_service.exception.AppException;
import com.datpvc.identity_service.exception.ErrorCode;
import com.datpvc.identity_service.repository.InvalidatedTokenRepository;
import com.datpvc.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signer}")
    protected String SIGNER_KEY;

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
               .orElseThrow(()-> new AppException(ErrorCode.LOGIN_FAILED));

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if(!authenticated) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
               .id(jit)
               .expiryTime(expiryTime)
               .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean verified = signedJWT.verify(verifier);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if(!(verified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("datpvc-dang-hoc-java")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions()
                            .forEach(permission -> scopeJoiner.add(permission.getName()));
                }
            });
        }
        return scopeJoiner.toString();
    }
}
