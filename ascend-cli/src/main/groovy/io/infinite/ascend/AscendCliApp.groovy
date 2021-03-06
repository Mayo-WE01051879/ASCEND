package io.infinite.ascend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.granting.client.services.ClientAuthorizationGrantingService
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import picocli.CommandLine

import java.util.concurrent.Callable

@BlackBox
@Slf4j
@SpringBootApplication
@CommandLine.Command(name = "ASCEND-CLI", mixinStandardHelpOptions = true, description = "App to app authorization command line client.", version = "1.0.0")
class AscendCliApp implements Callable<Integer>, CommandLineRunner {

    @CommandLine.Option(names = ["--ascendClientPublicKeyName"], paramLabel = "YOUR_APP", description = "Ascend Client Application Key Name, as trusted in Ascend Granting Server.", required = true)
    String ascendClientPublicKeyName

    @CommandLine.Option(names = ["--ascendGrantingUrl"], paramLabel = "URL", description = "Ascend Granting Server URL", required = true)
    String ascendGrantingUrl

    @CommandLine.Option(names = ["--scopeName"], paramLabel = "e.g. READ_WRITE", description = "Desired authorization scope name.", required = true)
    String scopeName

    @CommandLine.Option(names = ["--authorizationServerNamespace"], paramLabel = "ORBIT", description = "Resource Server Group Name.", required = true)
    String authorizationServerNamespace

    @CommandLine.Option(names = ["--authorizationClientNamespace"], paramLabel = "ORBIT", description = "Application Name.", required = true)
    String authorizationClientNamespace

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    static void main(String[] args) {
        log.info("Welcome to Infinite Technology Ascend app to app authorization command line client.")
        log.info("This is Open Source software, free for usage and modification.")
        log.info("By using this software you accept License and User Agreement.")
        log.info("Visit our web site: https://i-t.io/Ascend")
        System.setProperty("jwtAccessKeyPublic", "")
        System.setProperty("jwtAccessKeyPrivate", "")
        System.setProperty("jwtRefreshKeyPublic", "")
        System.setProperty("jwtRefreshKeyPrivate", "")
        System.setProperty("ascendValidationUrl", "")
        System.setProperty("orbitUrl", "")
        SpringApplication.run(AscendCliApp.class, args)
    }

    Integer call() throws Exception {
        Authorization orbitAuthorization = clientAuthorizationGrantingService.grantByScope(scopeName, ascendGrantingUrl, authorizationClientNamespace, authorizationServerNamespace)
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(System.out, orbitAuthorization)
        return 0
    }

    @Override
    void run(String... args) throws Exception {
        int exitCode = new CommandLine(this).execute(args)
        System.exit(exitCode)
    }

}
