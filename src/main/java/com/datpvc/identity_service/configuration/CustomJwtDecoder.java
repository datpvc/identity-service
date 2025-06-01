package com.datpvc.identity_service.configuration;

import com.datpvc.identity_service.dto.request.IntrospectRequest;
import com.datpvc.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    AuthenticationService authenticationService;

    @Value("${jwt.signer}")
    private String jwtSigner;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
         var response = authenticationService.introspect(IntrospectRequest.builder()
                         .token(token)
                 .build());
         if(response.isValid()){
             throw new JwtException("JWT token is invalid");
         }
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if(Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSigner.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
