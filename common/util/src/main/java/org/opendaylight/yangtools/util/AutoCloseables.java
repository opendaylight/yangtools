/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.io.Closeables;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with {@link AutoCloseable} objects.
 *
 * <p>Neither Google Guava nor Apache Commons IO have this.
 *
 * @see Closeables
 *
 * @author Michael Vorburger
 */
@Beta
@SuppressWarnings("checkstyle:IllegalCatch")
public final class AutoCloseables {

    private static final Logger LOG = LoggerFactory.getLogger(AutoCloseables.class);

    private AutoCloseables() { }

    /**
     * Close, and log any exception that occurred as a warning. This method
     * variant instead of {@link AutoCloseables#closeQuietly(AutoCloseable)}
     * should be used in most of cases.  If you are closing a field which
     * should be set to null after being closed, then use
     * {@link AutoCloseables#closeAndNullifyOrWarn(AutoCloseable)}.
     *
     * @param closeable an AutoCloseable to close()
     */
    public static void closeOrWarn(@Nullable AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            LOG.warn("Failed to close() AutoCloseable {}", safeToString(closeable), e);
        }
    }

    /**
     * Close, and return null.  This is typically used like this:
     * <pre>
     *   class Something {
     *       SomethingAutoCloseable closeable;
     *       void close() {
     *         closeable = AutoCloseables.closeAndNullifyOrWarn(closeable);
     *       }
     *   }
     * </pre>
     * @param closeable an AutoCloseable to close()
     * @return null (or the closeable if it could not be closed)
     */
    public static <T extends AutoCloseable> T closeAndNullifyOrWarn(@Nullable T closeable) {
        if (closeable == null) {
            return closeable;
        }
        try {
            closeable.close();
            return null;
        } catch (Exception e) {
            LOG.warn("Failed to close() AutoCloseable {}", safeToString(closeable), e);
            return closeable;
        }
    }

    /**
     * Close, and log any exception that occurred as only a debug instead of a warn. This method
     * variant should be used only in rare exceptional (pun intended) cases,
     * when a close() is known to cause completely harmless Exceptions (are you
     * sure?) which when shown in logging would certainly only confuse
     * end-users, and so must be 100% suppressed. This is rare.
     *
     * @param closeable an AutoCloseable to close()
     */
    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            LOG.debug("Failed to closeQuietly() AutoCloseable {}", safeToString(closeable), e);
        }
    }

    private static String safeToString(Object object) {
        String objectToString = "";
        try {
            objectToString = ": " + object.toString();
        } catch (Throwable t) {
            // Ignore this
        }
        return objectToString;
    }
}
