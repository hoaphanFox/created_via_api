// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports.bedrock.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.webflux.advice.general.GeneralAdviceTrait;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;
import org.zalando.problem.spring.webflux.advice.network.NetworkAdviceTrait;
import org.zalando.problem.spring.webflux.advice.validation.ValidationAdviceTrait;

/**
 * This controller advice will translate exceptions to Problems (RFC7807).
 */
@ControllerAdvice
public class ExceptionHandler implements GeneralAdviceTrait, HttpAdviceTrait, NetworkAdviceTrait,
        ValidationAdviceTrait {
}
