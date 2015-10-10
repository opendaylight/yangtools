/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public abstract class BaseTypeBuilder<T extends TypeDefinition<T>> extends AbstractTypeBuilder<T>
        implements Builder<BaseType<T>> {
    BaseTypeBuilder(final SchemaPath path) {
        super(null, path);
    }
}
