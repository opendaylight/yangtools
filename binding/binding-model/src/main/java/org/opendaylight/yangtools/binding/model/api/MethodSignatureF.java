/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The full implementation of a {@link MethodSignature}.
 */
@NonNullByDefault
record MethodSignatureF(
        String getName,
        Type getReturnType,
        ValueMechanics getMechanics,
        boolean isAbstract,
        boolean isDefault,
        @Nullable TypeMemberComment getComment,
        List<AttachedAnnotation> getAnnotations,
        AccessModifier getAccessModifier,
        List<Parameter> getParameters) implements MethodSignature {
    MethodSignatureF {
        requireNonNull(getName);
        requireNonNull(getReturnType);
        requireNonNull(getMechanics);
        requireNonNull(getAnnotations);
        requireNonNull(getAccessModifier);
        requireNonNull(getParameters);
    }

    @Override
    public int hashCode() {
        return MethodSignatureMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return MethodSignatureMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return MethodSignatureMethods.toString(this);
    }
}
