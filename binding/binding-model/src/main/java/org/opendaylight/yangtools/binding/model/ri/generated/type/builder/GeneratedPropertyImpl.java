/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
public record GeneratedPropertyImpl(String name, Type returnType, boolean isReadOnly) implements GeneratedProperty {
    public GeneratedPropertyImpl {
        requireNonNull(name);
        requireNonNull(returnType);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }
}
