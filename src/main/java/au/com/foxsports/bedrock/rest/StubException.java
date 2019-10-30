package au.com.foxsports.bedrock.rest;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.StatusType;

public class StubException extends AbstractThrowableProblem {

    public static final String DEFAULT_PROBLEM_TITLE = "api.exception.generic";

    public StubException(final int code, final String message) {
        super(null, DEFAULT_PROBLEM_TITLE, createStatus(code, message), message);
    }

    private static StatusType createStatus(final int code, final String message) {
        return new StatusType() {
            @Override
            public int getStatusCode() {
                return code;
            }

            @Override
            public String getReasonPhrase() {
                return message;
            }
        };
    }
}
