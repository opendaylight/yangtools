/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class EmptyContainerEffectiveStatement extends AbstractContainerStatement {
    EmptyContainerEffectiveStatement(final ContainerStatement declared, final SchemaPath path, final int flags,
            final StatementSourceReference ref, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ContainerSchemaNode original) {
        super(declared, path, flags, ref, substatements, original);
    }
}
