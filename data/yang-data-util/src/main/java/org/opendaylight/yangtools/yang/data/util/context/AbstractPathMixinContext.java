/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.PathMixin;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public abstract sealed class AbstractPathMixinContext extends AbstractContext implements PathMixin
        permits AbstractListLikeContext, ChoiceContext {
    AbstractPathMixinContext(final DataSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
    }
}