/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

final class MethodSignatureBuilderImpl extends AbstractTypeMemberBuilder<MethodSignatureBuilder>
        implements MethodSignatureBuilder {
    private List<MethodSignature.Parameter> parameters = Collections.emptyList();
    private List<MethodSignature.Parameter> unmodifiableParams = Collections.emptyList();
    private ValueMechanics mechanics = ValueMechanics.NORMAL;
    private boolean isAbstract;
    private boolean isDefault;

    MethodSignatureBuilderImpl(final String name) {
        super(name);
    }

    @Override
    public MethodSignatureBuilder setAbstract(final boolean newIsAbstract) {
        this.isAbstract = newIsAbstract;
        return this;
    }

    @Override
    public MethodSignatureBuilder setDefault(final boolean newIsDefault) {
        this.isDefault = newIsDefault;
        return this;
    }

    @Override
    public MethodSignatureBuilder setMechanics(final ValueMechanics newMechanics) {
        this.mechanics = requireNonNull(newMechanics);
        return this;
    }

    @Override
    public MethodSignatureBuilder addParameter(final Type type, final String name) {
        this.parameters = LazyCollections.lazyAdd(this.parameters, new MethodParameterImpl(name, type));
        this.unmodifiableParams = Collections.unmodifiableList(this.parameters);
        return this;
    }

    @Override
    protected MethodSignatureBuilder thisInstance() {
        return this;
    }

    @Override
    public MethodSignature toInstance(final Type definingType) {
        final List<AnnotationType> annotations = toAnnotationTypes();
        return new MethodSignatureImpl(getName(), annotations, getComment(), getAccessModifier(), getReturnType(),
            unmodifiableParams, isFinal(), isAbstract, isStatic(), isDefault, mechanics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), parameters, getReturnType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MethodSignatureBuilderImpl other = (MethodSignatureBuilderImpl) obj;
        return Objects.equals(getName(), other.getName())
                && Objects.equals(this.parameters, other.parameters)
                && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MethodSignatureBuilderImpl [name=").append(getName())
                .append(", returnType=").append(getReturnType())
                .append(", parameters=").append(this.parameters)
                .append(", annotationBuilders=").append(getAnnotationBuilders())
                .append(", comment=").append(getComment())
                .append(']').toString();
    }
}
