/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import org.slf4j.Logger;

/**
 * Thread's UncaughtExceptionHandler which logs to slf4j.
 *
 * @author Michael Vorburger.ch
 */
@SuppressWarnings({"checkstyle:LoggerVariableName", "LoggerVariableModifiers"})
public final class LoggingThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {

    // This class is also available in infrautils (but yangtools cannot depend on infrautils)
    // as org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler

    /**
     * Factory method to obtain an instance of this bound to the passed slf4j Logger.
     */
    public static UncaughtExceptionHandler toLogger(Logger logger) {
        return new LoggingThreadUncaughtExceptionHandler(logger);
    }

    private final Logger logger;

    private LoggingThreadUncaughtExceptionHandler(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logger.error("Thread terminated due to uncaught exception: {}", thread.getName(), throwable);
    }
}
