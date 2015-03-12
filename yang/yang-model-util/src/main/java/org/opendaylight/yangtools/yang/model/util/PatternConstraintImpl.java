/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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

    public PatternConstraintImpl(final String regex, final Optional<String> description,
            final Optional<String> reference) {
        super();
        this.regex = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description.orNull();
        this.reference = reference.orNull();

        this.errorAppTag = "argument-does-not-match-regex";
        this.errorMessage = "The argument does not match regular expression '" + this.regex + "'";
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
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((errorAppTag == null) ? 0 : errorAppTag.hashCode());
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
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
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (errorAppTag == null) {
            if (other.errorAppTag != null) {
                return false;
            }
        } else if (!errorAppTag.equals(other.errorAppTag)) {
            return false;
        }
        if (errorMessage == null) {
            if (other.errorMessage != null) {
                return false;
            }
        } else if (!errorMessage.equals(other.errorMessage)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        if (regex == null) {
            if (other.regex != null) {
                return false;
            }
        } else if (!regex.equals(other.regex)) {
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