/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;

public final class GeneratedPropertyBuilderImpl extends AbstractTypeMemberBuilder<GeneratedPropertyBuilder>
        implements GeneratedPropertyBuilder {
    private String value;
    private boolean readOnly;

    public GeneratedPropertyBuilderImpl(final String name) {
        super(name);
        this.readOnly = true;
    }

    @Override
    public GeneratedPropertyBuilderImpl setValue(final String value) {
        this.value = value;
        return this;
    }

    @Override
    public GeneratedPropertyBuilderImpl setReadOnly(final boolean isReadOnly) {
        this.readOnly = isReadOnly;
        return this;
    }

    @Override
    protected GeneratedPropertyBuilderImpl thisInstance() {
        return this;
    }

    @Override
    public GeneratedProperty toInstance() {
        final List<AnnotationType> annotations = toAnnotationTypes();
        return new GeneratedPropertyImpl(getName(), annotations, getComment(), getAccessModifier(), getReturnType(),
            isFinal(), isStatic(), this.readOnly, this.value);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("GeneratedPropertyImpl [name=").append(getName())
            .append(", annotations=").append(getAnnotationBuilders())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", isFinal=").append(isFinal())
            .append(", isReadOnly=").append(this.readOnly)
            .append(", modifier=").append(getAccessModifier())
            .append(']').toString();
    }
}