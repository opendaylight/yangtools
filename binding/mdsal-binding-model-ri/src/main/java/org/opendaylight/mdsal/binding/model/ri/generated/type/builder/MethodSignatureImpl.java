/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;

class MethodSignatureImpl extends AbstractTypeMember implements MethodSignature {
    private final List<Parameter> params;
    private final @NonNull ValueMechanics mechanics;
    private final boolean isAbstract;
    private final boolean isDefault;

    @VisibleForTesting
    MethodSignatureImpl(final String name, final List<AnnotationType> annotations,
            final TypeMemberComment comment, final AccessModifier accessModifier, final Type returnType,
            final List<Parameter> params, final boolean isFinal, final boolean isAbstract, final boolean isStatic) {
        this(name, annotations, comment, accessModifier, returnType, params, isFinal, isAbstract, isStatic, false,
            ValueMechanics.NORMAL);
    }

    MethodSignatureImpl(final String name, final List<AnnotationType> annotations,
            final TypeMemberComment comment, final AccessModifier accessModifier, final Type returnType,
            final List<Parameter> params, final boolean isFinal, final boolean isAbstract, final boolean isStatic,
            final boolean isDefault, final ValueMechanics mechanics) {
        super(name, annotations, comment, accessModifier, returnType, isFinal, isStatic);
        this.params = params;
        this.isAbstract = isAbstract;
        this.isDefault = isDefault;
        this.mechanics = requireNonNull(mechanics);
    }

    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public List<Parameter> getParameters() {
        return this.params;
    }

    @Override
    public ValueMechanics getMechanics() {
        return mechanics;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(this.params);
        result = prime * result + Objects.hashCode(getReturnType());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MethodSignatureImpl other = (MethodSignatureImpl) obj;
        return Objects.equals(getName(), other.getName())
                && Objects.equals(this.params, other.params)
                && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MethodSignatureImpl [name=").append(getName())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", params=").append(this.params)
            .append(", annotations=").append(getAnnotations())
            .append(']')
            .toString();
    }
}
