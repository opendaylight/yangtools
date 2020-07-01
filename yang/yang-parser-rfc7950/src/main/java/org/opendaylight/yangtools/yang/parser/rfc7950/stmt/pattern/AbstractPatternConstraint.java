/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

abstract class AbstractPatternConstraint implements PatternConstraint, Immutable {
    private final String regEx;
    private final String rawRegEx;
    private final String errorAppTag;

    AbstractPatternConstraint(final String regex, final String rawRegex, final String errorAppTag) {
        this.regEx = requireNonNull(regex, "regex must not be null");
        this.rawRegEx = requireNonNull(rawRegex, "raw regex must not be null");
        // FIXME: 6.0.0: explain this assignment
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
    }

    @Override
    public final String getJavaPatternString() {
        return regEx;
    }

    @Override
    public final String getRegularExpressionString() {
        return rawRegEx;
    }

    @Override
    public final Optional<String> getDescription() {
        return Optional.ofNullable(description());
    }

    @Override
    public final Optional<String> getErrorAppTag() {
        return Optional.ofNullable(errorAppTag);
    }

    @Override
    public final Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage());
    }

    @Override
    public final Optional<ModifierKind> getModifier() {
        return Optional.ofNullable(modifier());
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.ofNullable(reference());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(description(), errorAppTag, errorMessage(), reference(), regEx, modifier());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractPatternConstraint)) {
            return false;
        }
        final AbstractPatternConstraint other = (AbstractPatternConstraint) obj;
        return Objects.equals(description(), other.description()) && Objects.equals(errorAppTag, other.errorAppTag)
                && Objects.equals(errorMessage(), other.errorMessage())
                && Objects.equals(reference(), other.reference()) && Objects.equals(regEx, other.regEx)
                && Objects.equals(modifier(), other.modifier());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    abstract @Nullable String description();

    abstract @Nullable String reference();

    abstract @Nullable String errorMessage();

    abstract @Nullable ModifierKind modifier();

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("regex", regEx).add("errorAppTag", errorAppTag);
    }
}
