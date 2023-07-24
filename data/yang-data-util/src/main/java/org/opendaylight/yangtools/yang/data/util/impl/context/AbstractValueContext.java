/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import static com.google.common.base.Verify.verifyNotNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.SimpleValue;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;

public abstract sealed class AbstractValueContext extends AbstractContext implements SimpleValue
        permits LeafContext, LeafListItemContext {
    AbstractValueContext(final NodeIdentifier pathStep, final TypedDataSchemaNode dataSchemaNode) {
        super(pathStep, dataSchemaNode);
    }

    @Override
    public final TypeDefinition<?> type() {
        return verifyNotNull(((TypedDataSchemaNode) dataSchemaNode()).getType());
    }
}
