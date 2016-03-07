/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

/**
 * Immutable implementation of {@link MustDefinition}
 */
public final class MustDefinitionImpl implements MustDefinition {
    private final String mustStr;
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;

    /**
     * Creates new Must Definition
     *
     * @param mustStr must string statement, Must not be null.
     * @param description Description of condition
     * @param reference Reference for condition
     * @param errorAppTag error application tag which should be used for error reporting when condition fails
     * @param errorMessage message  which should be used for error reporting when condition fails
     */
    private MustDefinitionImpl(final String mustStr, final String description, final String reference, final String errorAppTag, final String errorMessage) {
        this.mustStr = Preconditions.checkNotNull(mustStr);
        this.description = description;
        this.reference = reference;
        this.errorAppTag = errorAppTag;
        this.errorMessage = errorMessage;
    }

    /**
    *
    * Creates new Must Definition
    *
    * @param mustStr must string statement, Must not be null.
    * @param description Description of condition
    * @param reference Reference for condition
    * @param errorAppTag error application tag which should be used for error reporting when condition fails
    * @param errorMessage message  which should be used for error reporting when condition fails
    */
    public static MustDefinitionImpl create(final String mustStr, final Optional<String> description,
            final Optional<String> reference, final Optional<String> errorAppTag, final Optional<String> errorMessage) {
        return new MustDefinitionImpl(mustStr, description.orNull(), reference.orNull(), errorAppTag.orNull(), errorMessage.orNull());
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
    public RevisionAwareXPath getXpath() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mustStr.hashCode();
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(reference);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MustDefinitionImpl)) {
            return false;
        }
        final MustDefinitionImpl other = (MustDefinitionImpl) obj;
        return mustStr.equals(other.mustStr) && Objects.equals(description, other.description)
                && Objects.equals(reference, other.reference);
    }

    @Override
    public String toString() {
        return mustStr;
    }
}
