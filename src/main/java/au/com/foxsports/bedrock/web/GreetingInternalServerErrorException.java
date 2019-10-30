// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports.bedrock.web;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

class GreetingInternalServerErrorException extends AbstractThrowableProblem {

    GreetingInternalServerErrorException() {
        super(
                URI.create("https://github.com/zalando/problem"),
                Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                Status.INTERNAL_SERVER_ERROR,
                "This is a RCF7807 problem sample (See https://tools.ietf.org/html/rfc7807)"
        );
    }
}
