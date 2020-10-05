/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An intermediate capture of the argument to {@code pattern} statement. It exposes both XSD regular expression, as well
 * as a {@link java.util.regex.Pattern} pattern.
 */
@Beta
@NonNullByDefault
public final class PatternExpression implements Immutable {
    private final String pattern;
    private final String regex;

    private PatternExpression(final String pattern, final String regex) {
        this.pattern = requireNonNull(pattern);
        this.regex = requireNonNull(regex);
    }

    public static PatternExpression of(final String pattern, final String regex) {
        return new PatternExpression(pattern, regex);
    }

    /**
     * Returns a Java {@link Pattern}-compatible regular expression (pattern). Returned string performs equivalent
     * matching in terms of enforcement, but it may have a structure completely different from the one in YANG model.
     *
     * @return string Java Pattern regular expression
     */
    // FIXME: should we be providing a Pattern instance? This, along with the other method is treading the fine
    //        balance between usability of the effective model, the purity of effective view model and memory
    //        overhead. We pick usability and memory footprint and expose both methods from effective model.
    public String getJavaPatternString() {
        return pattern;
    }

    /**
     * Returns a raw regular expression as it was declared in a source. This string conforms to XSD regular expression
     * syntax, which is notably different from Java's Pattern string.
     *
     * @return argument of pattern statement as it was declared in YANG model.
     */
    public String getRegularExpressionString() {
        return regex;
    }

    @Override
    public int hashCode() {
        return regex.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PatternExpression)) {
            return false;
        }
        final PatternExpression other = (PatternExpression) obj;
        return regex.equals(other.regex) && pattern.equals(other.pattern);
    }

    @Override
    public String toString() {
        return regex;
    }
}
