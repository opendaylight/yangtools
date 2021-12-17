/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import com.google.common.collect.ForwardingObject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Common base class for forwarding implementations of {@link ModelStatement}.
 */
public abstract class ForwardingModelStatement<D extends ModelStatement> extends ForwardingObject
        implements ModelStatement {
    @Override
    public StatementDefinition statementDefinition() {
        return delegate().statementDefinition();
    }

    @Override
    protected abstract @NonNull D delegate();
}
