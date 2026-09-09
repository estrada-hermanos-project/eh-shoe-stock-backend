package com.estradahermanos.shoestock.utilities;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExceptionLog
{
    private ExceptionLog()
    {
    }

    public static void unexpected(String message, Exception exception)
    {
        StackTraceElement[] stack = exception.getStackTrace();
        if (stack.length > 0)
        {
            log.error("{}: {} [{}:{}]",
                    message,
                    exception.getMessage(),
                    stack[0].getFileName(),
                    stack[0].getLineNumber(),
                    exception);
            return;
        }
        log.error("{}: {}", message, exception.getMessage(), exception);
    }
}
