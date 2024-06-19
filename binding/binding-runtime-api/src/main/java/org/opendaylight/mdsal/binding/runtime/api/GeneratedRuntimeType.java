/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A {@link RuntimeType} associated with a {@link GeneratedType}.
 */
public interface GeneratedRuntimeType extends RuntimeType, Identifiable<JavaTypeName> {
    @Override
    GeneratedType javaType();

    @Override
    default JavaTypeName getIdentifier() {
        return javaType().getIdentifier();
    }
}
