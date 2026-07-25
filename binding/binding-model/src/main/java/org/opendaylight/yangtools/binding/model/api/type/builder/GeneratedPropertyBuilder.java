/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.GeneratedPropertyImpl;

/**
 * Generated Property Builder is interface that contains methods to build and instantiate Generated Property definition.
 *
 * @see GeneratedProperty
 */
public final class GeneratedPropertyBuilder extends TypeMemberBuilder<GeneratedPropertyBuilder> {
    private boolean readOnly = true;
    private String value;

    public GeneratedPropertyBuilder(final String name) {
        super(name);
    }

    @NonNullByDefault
    public GeneratedPropertyBuilder setValue(final String newValue) {
        value = requireNonNull(newValue);
        return this;
    }

    /**
     * Sets isReadOnly flag for property. If property is marked as read only it is the same as set property in Java
     * as final.
     *
     * @param newReadOnly Read Only property flag.
     */
    @NonNullByDefault
    public GeneratedPropertyBuilder setReadOnly(final boolean newReadOnly) {
        readOnly = newReadOnly;
        return this;
    }

    /**
     * Returns <code>new</code> <i>immutable</i> instance of Generated Property. <br>
     * The <code>definingType</code> param cannot be <code>null</code>. The
     * every member in Java MUST be declared and defined inside the scope of
     * <code>class</code> definition. In case that defining Type will be passed
     * as <code>null</code> reference the method SHOULD thrown
     * {@link IllegalArgumentException}.
     *
     * @return <code>new</code> <i>immutable</i> instance of Generated Property.
     */
    @NonNullByDefault
    public GeneratedProperty toInstance() {
        final var annotations = toAnnotationTypes();
        return new GeneratedPropertyImpl(getName(), annotations, getComment(), getAccessModifier(), getReturnType(),
            readOnly, value);
    }

    @Override
    GeneratedPropertyBuilder thisInstance() {
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("GeneratedPropertyBuilder [name=").append(getName())
            .append(", annotations=").append(getAnnotationBuilders())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", isReadOnly=").append(readOnly)
            .append(", modifier=").append(getAccessModifier())
            .append(']').toString();
    }
}
