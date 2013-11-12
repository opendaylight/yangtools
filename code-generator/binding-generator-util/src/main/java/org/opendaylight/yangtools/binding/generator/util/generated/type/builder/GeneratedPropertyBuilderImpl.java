/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import java.util.List;

import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;

public final class GeneratedPropertyBuilderImpl extends AbstractTypeMemberBuilder<GeneratedPropertyBuilder> implements GeneratedPropertyBuilder {
    private String value;
    private boolean isReadOnly;

    public GeneratedPropertyBuilderImpl(String name) {
        super(name);
        this.isReadOnly = true;
    }

    @Override
    public GeneratedPropertyBuilderImpl setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public GeneratedPropertyBuilderImpl setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
        return this;
    }

    @Override
    protected GeneratedPropertyBuilderImpl thisInstance() {
        return this;
    }

    @Override
    public GeneratedProperty toInstance(Type definingType) {
        final List<AnnotationType> annotations = toAnnotationTypes();
        return new GeneratedPropertyImpl(definingType, getName(), annotations, getComment(), getAccessModifier(),
                getReturnType(), isFinal(), isStatic(), isReadOnly, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedPropertyImpl [name=");
        builder.append(getName());
        builder.append(", annotations=");
        builder.append(getAnnotationBuilders());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", isFinal=");
        builder.append(isFinal());
        builder.append(", isReadOnly=");
        builder.append(isReadOnly);
        builder.append(", modifier=");
        builder.append(getAccessModifier());
        builder.append("]");
        return builder.toString();
    }
}