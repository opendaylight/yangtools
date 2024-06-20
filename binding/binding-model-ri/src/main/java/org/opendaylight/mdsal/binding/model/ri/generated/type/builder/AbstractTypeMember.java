/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMember;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

abstract class AbstractTypeMember implements TypeMember {

    private final String name;
    private final TypeMemberComment comment;
    private final Type returnType;
    private final List<AnnotationType> annotations;
    private final boolean isFinal;
    private final boolean isStatic;
    private final AccessModifier accessModifier;

    protected AbstractTypeMember(final String name,  final List<AnnotationType> annotations,
            final TypeMemberComment comment, final AccessModifier accessModifier, final Type returnType,
            final boolean isFinal, final boolean isStatic) {
        this.name = name;
        this.annotations = annotations;
        this.comment = comment;
        this.accessModifier = accessModifier;
        this.returnType = returnType;
        this.isFinal = isFinal;
        this.isStatic = isStatic;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return this.annotations;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TypeMemberComment getComment() {
        return comment;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return this.accessModifier;
    }

    @Override
    public Type getReturnType() {
        return this.returnType;
    }

    @Override
    public boolean isFinal() {
        return this.isFinal;
    }

    @Override
    public boolean isStatic() {
        return this.isStatic;
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
