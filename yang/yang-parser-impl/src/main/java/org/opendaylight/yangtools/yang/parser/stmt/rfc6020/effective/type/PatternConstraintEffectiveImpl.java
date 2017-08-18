/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

public class PatternConstraintEffectiveImpl implements PatternConstraint {

    private final String regEx;
    private final String rawRegEx;
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;

    public PatternConstraintEffectiveImpl(final String regex, final String rawRegex,
            final Optional<String> description, final Optional<String> reference) {
        this(regex, rawRegex, description.orElse(null), reference.orElse(null), null, null, null);
    }

    public PatternConstraintEffectiveImpl(final String regex, final String rawRegex, final String description,
            final String reference, final String errorAppTag, final String errorMessage, final ModifierKind modifier) {
        super();
        this.regEx = Preconditions.checkNotNull(regex, "regex must not be null.");
        this.rawRegEx = Preconditions.checkNotNull(rawRegex, "raw regex must not be null.");
        this.description = description;
        this.reference = reference;
        this.errorAppTag = errorAppTag != null ? errorAppTag : "invalid-regular-expression";
        this.errorMessage = errorMessage != null ? errorMessage : String.format(
                "Supplied value does not match the regular expression %s.", regex);
        this.modifier = modifier;
    }

    @Override
    public String getRegularExpression() {
        return regEx;
    }

    @Override
    public String getRawRegularExpression() {
        return rawRegEx;
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
    public ModifierKind getModifier() {
        return modifier;
    }

    @Override
    public String getReference() {
        return reference;
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
        final PatternConstraintEffectiveImpl other = (PatternConstraintEffectiveImpl) obj;
        return Objects.equals(description, other.description) && Objects.equals(errorAppTag, other.errorAppTag)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(reference, other.reference)
                && Objects.equals(regEx, other.regEx) && Objects.equals(modifier, other.modifier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("regex", regEx).add("description", description)
                .add("reference", reference).add("errorAppTag", errorAppTag).add("errorMessage", errorMessage)
                .add("modifier", modifier).toString();
    }
}
