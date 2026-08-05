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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
record MethodSignature0(
        EffectiveStatement<?, ?> statement,
        String name,
        Type returnType) implements MethodSignature {
    MethodSignature0 {
        requireNonNull(statement);
        requireNonNull(name);
        requireNonNull(returnType);
    }

    @Override
    public List<AttachedAnnotation.ToMethod> annotations() {
        return List.of();
    }

    @Override
    public String toString() {
        return TypeMethods.toString(this);
    }
}
