/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * namespace class similar to {@link org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace} for storing modules
 * into Yang model storage but keyed by plain name.
 */
public interface ModuleNamespaceForBelongsTo extends
        StatementNamespace<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {
    NamespaceBehaviour<String, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>,
            @NonNull ModuleNamespaceForBelongsTo> BEHAVIOUR = NamespaceBehaviour.global(
                ModuleNamespaceForBelongsTo.class);
}
