/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

final class PatternConstraintImpl implements PatternConstraint, Immutable {
    private final String regEx;
    private final String rawRegEx;
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;

    private PatternConstraintImpl(final String regex, final String rawRegex, final String description,
            final String reference, final String errorAppTag, final String errorMessage, final ModifierKind modifier) {
        this.regEx = requireNonNull(regex, "regex must not be null");
        this.rawRegEx = requireNonNull(rawRegex, "raw regex must not be null");
        this.description = description;
        this.reference = reference;
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
        this.errorMessage = errorMessage;
        this.modifier = modifier;
    }

    PatternConstraintImpl(final String regex, final String rawRegex) {
        this(regex, rawRegex, null, null, null, null, null);
    }

    PatternConstraintImpl(final PatternConstraint original, final String description, final String reference,
            final String errorAppTag, final String errorMessage, final ModifierKind modifier) {
        this(original.getJavaPatternString(), original.getRegularExpressionString(), description, reference,
            errorAppTag, errorMessage, modifier);
    }

    @Override
    public String getJavaPatternString() {
        return regEx;
    }

    @Override
    public String getRegularExpressionString() {
        return rawRegEx;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getErrorAppTag() {
        return Optional.ofNullable(errorAppTag);
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public Optional<ModifierKind> getModifier() {
        return Optional.ofNullable(modifier);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, errorAppTag, errorMessage, reference, regEx, modifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PatternConstraintImpl other = (PatternConstraintImpl) obj;
        return Objects.equals(description, other.description) && Objects.equals(errorAppTag, other.errorAppTag)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(reference, other.reference)
                && Objects.equals(regEx, other.regEx) && Objects.equals(modifier, other.modifier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("regex", regEx).add("description", description)
                .add("reference", reference).add("errorAppTag", errorAppTag).add("errorMessage", errorMessage)
                .add("modifier", modifier).toString();
    }
}
