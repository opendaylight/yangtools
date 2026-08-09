/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.GetterAnnotation;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

@NonNullByDefault
public record GetterMethodN(
        SchemaTreeEffectiveStatement<?> statement,
        ReturnType type,
        List<GetterAnnotation> annotations) implements GetterMethod {

    public GetterMethodN {
        requireNonNull(statement);
        requireNonNull(type);
        requireNonNull(annotations);
    }

    @Override
    public String toString() {
        return TypeMethods.toString(this);
    }
}
