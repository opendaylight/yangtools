/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.List;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

public final class GeneratedPropertyImpl extends AbstractTypeMember implements GeneratedProperty {
    private final String value;
    private final boolean readOnly;

    public GeneratedPropertyImpl(final String name, final List<AttachedAnnotation> annotations,
            final TypeMemberComment comment, final AccessModifier accessModifier, final Type returnType,
            final boolean isReadOnly, final String value) {
        super(name, annotations, comment, accessModifier, returnType);
        this.value = value;
        readOnly = isReadOnly;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("GeneratedPropertyImpl [name=").append(getName())
            .append(", annotations=").append(getAnnotations())
            .append(", comment=").append(getComment())
            .append(", returnType=").append(getReturnType())
            .append(", isReadOnly=").append(readOnly)
            .append(", modifier=").append(getAccessModifier())
            .append(']')
            .toString();
    }
}
