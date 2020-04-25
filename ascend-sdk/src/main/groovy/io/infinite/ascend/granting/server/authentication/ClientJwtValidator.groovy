package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ClientJwtValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    @Override
    Map<String, String> validateAuthentication(Authentication authentication) {
        String ascendClientPublicKeyName = authentication.authenticationData.publicCredentials.get("ascendClientPublicKeyName")
        String clientJwt = authentication.authenticationData.privateCredentials.get("clientJwt")
        if (ascendClientPublicKeyName == null || clientJwt == null) {
            throw new AscendUnauthorizedException("Missing ascendClientPublicKeyName or clientJwt")
        }
        Optional<TrustedPublicKey> trustedAppOptional = trustedAppRepository.findByName(ascendClientPublicKeyName)
        if (!trustedAppOptional.present) {
            throw new AscendUnauthorizedException("Key $ascendClientPublicKeyName is not trusted.")
        }
        Authorization selfIssuedAuthorization = jwtService.jwt2Authorization(clientJwt, jwtService.loadPublicKeyFromHexString(trustedAppOptional.get().publicKey))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(selfIssuedAuthorization.expiryDate))
        if (selfIssuedAuthorization.expiryDate.before(new Date())) {
            throw new AscendUnauthorizedException("Expired clientJwt")
        }
        return ["ascendClientPublicKeyName": authentication.authenticationData.publicCredentials.get("ascendClientPublicKeyName")]
    }

}