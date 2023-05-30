/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMember;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

abstract class AbstractTypeMember implements TypeMember {
    private final @NonNull String name;
    private final TypeMemberComment comment;
    private final @NonNull Type returnType;
    private final @NonNull List<AnnotationType> annotations;
    private final boolean isFinal;
    private final boolean isStatic;
    private final AccessModifier accessModifier;

    AbstractTypeMember(final String name, final List<AnnotationType> annotations, final TypeMemberComment comment,
            final AccessModifier accessModifier, final Type returnType, final boolean isFinal, final boolean isStatic) {
        this.name = requireNonNull(name);
        this.annotations = requireNonNull(annotations);
        this.comment = comment;
        this.accessModifier = accessModifier;
        this.returnType = requireNonNull(returnType);
        this.isFinal = isFinal;
        this.isStatic = isStatic;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return annotations;
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
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getReturnType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractTypeMember other = (AbstractTypeMember) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("AbstractTypeMember [name=").append(getName())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", annotations=").append(getAnnotations())
            .append(']')
            .toString();
    }
}
