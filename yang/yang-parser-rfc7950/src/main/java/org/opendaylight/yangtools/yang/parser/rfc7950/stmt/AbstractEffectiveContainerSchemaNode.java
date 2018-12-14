/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveContainerSchemaNode<D extends DeclaredStatement<QName>>
        extends AbstractEffectiveSimpleDataNodeContainer<D> implements ContainerSchemaNode {
    private final ImmutableSet<MustDefinition> mustConstraints;

    protected AbstractEffectiveContainerSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        mustConstraints = ImmutableSet.copyOf(allSubstatementsOfType(MustDefinition.class));
    }

    @Override
    public final Collection<MustDefinition> getMustConstraints() {
        return mustConstraints;
    }
}
