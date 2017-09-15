/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractEffectiveContainerSchemaNode<D extends DeclaredStatement<QName>> extends
        AbstractEffectiveSimpleDataNodeContainer<D> implements ContainerSchemaNode {

    private final boolean presence;

    AbstractEffectiveContainerSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        this.presence = firstEffective(PresenceEffectiveStatementImpl.class) != null;
    }

    @Override
    public boolean isPresenceContainer() {
        return presence;
    }
}
