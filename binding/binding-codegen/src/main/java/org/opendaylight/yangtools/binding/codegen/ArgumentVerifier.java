/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An argument verification implementation. A JVM-constant implementation is selected based on the {@value #PROP_VERIFY}
 * property presence and value:
 * <ul>
 *   <li>if the property is not set, we silently default to {@link QuickVerifier}</li>
 *   <li>if the property value is {@code "quick"}, we select {@link QuickVerifier} and note that in the log</li>
 *   <li>if the property value is {@code "strict"}, we select {@link StrictVerifier} and note that in the log</li>
 *   <li>if the property value is any other string, we complain about the value and select {@link StrictVerifier}</li>
 * </ul>
 */
@NonNullByDefault
@CheckReturnValue
@VisibleForTesting
abstract sealed class ArgumentVerifier {
    /**
     * The quick verifier: we just make sure there are no {@code null}s or empty strings.
     */
    @VisibleForTesting
    static final class QuickVerifier extends ArgumentVerifier {
        private QuickVerifier() {
            // Hidden on purpose
        }

        @Override
        String verifyStr(final String arg) {
            return verifyStrNotEmpty(arg);
        }

        @Override
        String verifyNonEmptyStr(final String arg) {
            return arg;
        }

        @Override
        String verifyTxt(final String arg, final int nl) {
            return arg;
        }

        @Override
        void fullVerifyTxt(final String arg) {
            // no-op
        }
    }

    /**
     * The strict verifier: we do full argument checks.
     */
    @VisibleForTesting
    static final class StrictVerifier extends ArgumentVerifier {
        private StrictVerifier() {
            // Hidden on purpose
        }

        @Override
        String verifyStr(final String arg) {
            final var nl = arg.indexOf('\n');
            if (nl != -1) {
                throw new VerifyException("newline at offset " + nl + " of '" + arg + "'");
            }
            return verifyNonEmptyStr(arg);
        }

        @Override
        String verifyNonEmptyStr(final String arg) {
            return verifyStrNotEmpty(arg);
        }

        @Override
        String verifyTxt(final String arg, final int nl) {
            verify(nl >= 0);
            return verifyNotNull(arg);
        }

        @Override
        void fullVerifyTxt(final String arg) {
            final var nl = arg.indexOf('\n');
            if (nl == -1) {
                throw new VerifyException("no newline in '" + arg + "'");
            }
        }
    }

    /**
     * The name of the system property controlling implementation selection.
     */
    private static final String PROP_VERIFY = "odl.binding.codegen.verify";

    /**
     * The run-time constant verification.
     */
    @VisibleForTesting
    static final ArgumentVerifier INSTANCE = selectArgumentVerifier(
        LoggerFactory.getLogger(ArgumentVerifier.class), System.getProperty(PROP_VERIFY));

    @VisibleForTesting
    static ArgumentVerifier selectArgumentVerifier(final Logger log, final @Nullable String prop) {
        return switch (prop) {
            case null -> {
                log.debug("Using quick verification");
                yield new QuickVerifier();
            }
            case "quick" -> {
                log.info("Using quick verification");
                yield new QuickVerifier();
            }
            case "strict" -> {
                log.info("Using strict verification");
                yield new StrictVerifier();
            }
            default -> {
                log.warn("Bad {} value '{}', using strict verification", PROP_VERIFY, prop);
                yield new StrictVerifier();
            }
        };
    }

    /**
     * Verify the argument to {@link BlockBuilder#str(String)}.
     *
     * @param arg the argument
     * @return the argument
     */
    abstract String verifyStr(String arg);

    /**
     * Verify the argument to {@link BlockBuilder#str(String)} which is known to be non-empty.
     *
     * @param arg the argument
     * @return the argument
     */
    abstract String verifyNonEmptyStr(String arg);

    /**
     * Verify the argument to {@link BlockBuilder#txt(String)}.
     *
     * @param arg the argument
     * @return the argument
     */
    final String verifyTxt(final String arg) {
        if (arg.isEmpty()) {
            throw new VerifyException("empty txt");
        }
        fullVerifyTxt(arg);
        return arg;
    }

    /**
     * Verify the argument to {@link BlockBuilder#txt(String)} known to have a newline at specified offset.
     *
     * @param arg the argument
     * @param nl the offset
     * @return the argument
     */
    abstract String verifyTxt(String arg, int nl);

    /**
     * Run additional verification from {@link #verifyTxt(String)}.
     *
     * @param arg the argument
     */
    abstract void fullVerifyTxt(String arg);

    private static String verifyStrNotEmpty(final String arg) {
        if (arg.isEmpty()) {
            throw new VerifyException("empty str");
        }
        return arg;
    }
}