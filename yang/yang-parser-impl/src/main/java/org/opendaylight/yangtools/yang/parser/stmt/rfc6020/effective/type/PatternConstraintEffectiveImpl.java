/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class PatternConstraintEffectiveImpl implements PatternConstraint {

    private final String regEx;
    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;

    public PatternConstraintEffectiveImpl(final String regex, final Optional<String> description,
            final Optional<String> reference) {

        super();

        this.regEx = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description.orNull();
        this.reference = reference.orNull();

        errorAppTag = "invalid-regular-expression";
        errorMessage = String.format("String %s is not valid regular expression.", regex);
    }

    @Override
    public String getRegularExpression() {
        return regEx;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((errorAppTag == null) ? 0 : errorAppTag.hashCode());
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + regEx.hashCode();
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
        final PatternConstraintEffectiveImpl other = (PatternConstraintEffectiveImpl) obj;
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
        if (regEx == null) {
            if (other.regEx != null) {
                return false;
            }
        } else if (!regEx.equals(other.regEx)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PatternConstraintEffectiveImpl.class.getSimpleName());
        builder.append(" [regex=");
        builder.append(regEx);
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
