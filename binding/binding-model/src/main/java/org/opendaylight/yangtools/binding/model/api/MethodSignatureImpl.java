/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

// FIXME: hide this class and specialize it for optional bits
public final class MethodSignatureImpl implements MethodSignature {
    private final @NonNull String name;
    private final @NonNull Type returnType;
    private final @NonNull ValueMechanics mechanics;
    @NonNullByDefault
    private final List<AttachedAnnotation.ToMethod> annotations;
    private final boolean isDefault;
    private final @Nullable TypeMemberComment comment;

    public MethodSignatureImpl(final @NonNull String name,
            final @NonNull List<AttachedAnnotation.@NonNull ToMethod> annotations,
            final @Nullable TypeMemberComment comment, final @NonNull Type returnType, final boolean isDefault,
            final @NonNull ValueMechanics mechanics) {
        this.name = requireNonNull(name);
        this.returnType = requireNonNull(returnType);
        this.comment = comment;
        this.annotations = requireNonNull(annotations);
        this.isDefault = isDefault;
        this.mechanics = requireNonNull(mechanics);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeMemberComment getComment() {
        return comment;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public ValueMechanics getMechanics() {
        return mechanics;
    }

    @Override
    public List<AttachedAnnotation.ToMethod> getAnnotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getReturnType());
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof MethodSignatureImpl other
            && Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MethodSignatureImpl [name=").append(getName())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", annotations=").append(getAnnotations())
            .append(']')
            .toString();
    }
}
