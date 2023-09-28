/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

abstract sealed class AbstractInstanceIdentifierCodec extends AbstractStringInstanceIdentifierCodec
        permits InstanceIdentifierDeserializer, InstanceIdentifierSerializer {
    private final @NonNull DataSchemaContextTree dataContextTree;

    AbstractInstanceIdentifierCodec(final DataSchemaContextTree dataContextTree) {
        this.dataContextTree = requireNonNull(dataContextTree);
    }

    @Override
    protected final DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }
}
