/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

public class PatternConstraintEffectiveImpl implements PatternConstraint {

    private final String regEx;
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;

    public PatternConstraintEffectiveImpl(final String regex, final Optional<String> description,
            final Optional<String> reference) {
        this(regex, description.orNull(), reference.orNull(), "invalid-regular-expression", String.format(
                "String %s is not valid regular expression.", regex));
    }

    public PatternConstraintEffectiveImpl(final String regex, final String description, final String reference,
            final String errorAppTag, final String errorMessage) {
        super();
        this.regEx = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.description = description;
        this.reference = reference;
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
        this.errorMessage = errorMessage != null ? errorMessage : String.format(
                "String %s is not valid regular expression.", regex);
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
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(errorAppTag);
        result = prime * result + Objects.hashCode(errorMessage);
        result = prime * result + Objects.hashCode(reference);
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
        if (!Objects.equals(regEx, other.regEx)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return PatternConstraintEffectiveImpl.class.getSimpleName() + " [regex=" + regEx + ", description="
                + description + ", reference=" + reference + ", errorAppTag=" + errorAppTag + ", errorMessage="
                + errorMessage + "]";
    }
}
