/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

/**
 * {@link Immutable} implementation of {@link PatternConstraint}.
 *
 * <p>
 * Creates an instance of Range constraint based on supplied parameters with
 * additional behaviour:
 * <ul>
 * <li>{@link PatternConstraint#getErrorAppTag()} returns
 * <code>invalid-regular-expression</code>
 * </ul>
 */
final class PatternConstraintImpl implements PatternConstraint, Immutable {

    private final String regex;
    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;

    PatternConstraintImpl(final String regex, final Optional<String> description, final Optional<String> reference) {
        this(regex, description, reference, null, null, Optional.absent());
    }

    PatternConstraintImpl(final String regex, final Optional<String> description, final Optional<String> reference,
            final String errorAppTag, final String errorMessage, final Optional<ModifierKind> modifier) {
        this.regex = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description.orNull();
        this.reference = reference.orNull();
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
        this.errorMessage = errorMessage != null ? errorMessage : String.format(
                "Supplied value does not match the regular expression %s.", regex);
        this.modifier = modifier.orNull();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getErrorAppTag() {
        return errorAppTag;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getRegularExpression() {
        return regex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, errorAppTag, errorMessage, reference, regex, modifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PatternConstraintImpl)) {
            return false;
        }
        final PatternConstraintImpl other = (PatternConstraintImpl) obj;
        return Objects.equals(description, other.description) && Objects.equals(errorAppTag, other.errorAppTag)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(reference, other.reference)
                && Objects.equals(regex, other.regex) && Objects.equals(modifier, other.modifier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("regex", regex).add("description", description)
                .add("reference", reference).add("errorAppTag", errorAppTag).add("errorMessage", errorMessage)
                .add("modifier", modifier).toString();
    }
}
