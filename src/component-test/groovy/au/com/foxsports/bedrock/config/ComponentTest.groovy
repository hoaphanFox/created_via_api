package au.com.foxsports.bedrock.config


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest(webEnvironment = NONE, classes = [ComponentTestConfiguration])
@ActiveProfiles("componentTest")
@interface ComponentTest {
}
