/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

/**
 * {@link Immutable} implementation of {@link PatternConstraint}
 *
 * Creates an instance of Range constraint based on supplied parameters with
 * additional behaviour:
 *
 * <ul>
 * <li>{@link PatternConstraint#getErrorAppTag()} returns
 * <code>invalid-regular-expression</code>
 * </ul>
 *
 */
final class PatternConstraintImpl implements PatternConstraint, Immutable {

    private final String regex;
    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;

    PatternConstraintImpl(final String regex, final Optional<String> description, final Optional<String> reference) {
        this(regex, description, reference, "invalid-regular-expression", "");
    }

    PatternConstraintImpl(final String regex, final Optional<String> description, final Optional<String> reference,
            final String errorAppTag, final String errorMessage) {
        this.regex = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description.orNull();
        this.reference = reference.orNull();
        this.errorAppTag = errorAppTag;
        this.errorMessage = errorMessage;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(errorAppTag);
        result = prime * result + Objects.hashCode(errorMessage);
        result = prime * result + Objects.hashCode(reference);
        result = prime * result + regex.hashCode();
        return result;
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
                && Objects.equals(regex, other.regex);
    }

    @Override
    public String toString() {
        return "PatternConstraintImpl [regex=" +
                regex +
                ", description=" +
                description +
                ", reference=" +
                reference +
                ", errorAppTag=" +
                errorAppTag +
                ", errorMessage=" +
                errorMessage +
                "]";
    }
}