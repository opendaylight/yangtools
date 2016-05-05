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
        this(regex, description, reference, "invalid-regular-expression", String.format(
                "String %s is not valid regular expression.", regex));
    }

    PatternConstraintImpl(final String regex, final Optional<String> description, final Optional<String> reference,
            final String errorAppTag, final String errorMessage) {
        this.regex = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description.orNull();
        this.reference = reference.orNull();
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
        this.errorMessage = errorMessage != null ? errorMessage : String.format(
                "String %s is not valid regular expression.", regex);
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PatternConstraintImpl other = (PatternConstraintImpl) obj;
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(errorAppTag, other.errorAppTag)) {
            return false;
        }
        if (!Objects.equals(errorMessage, other.errorMessage)) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        if (!Objects.equals(regex, other.regex)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PatternConstraintImpl [regex=");
        builder.append(regex);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(reference);
        builder.append(", errorAppTag=");
        builder.append(errorAppTag);
        builder.append(", errorMessage=");
        builder.append(errorMessage);
        builder.append("]");
        return builder.toString();
    }
}