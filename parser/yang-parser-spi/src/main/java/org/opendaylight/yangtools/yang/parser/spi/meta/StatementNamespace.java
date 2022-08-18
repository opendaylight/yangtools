/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

// FIXME: is this subclass useful at all?
public class StatementNamespace<K, D extends DeclaredStatement<?>, E extends EffectiveStatement<?, D>>
        extends ParserNamespace<K, StmtContext<?, D, E>> {
    @Serial
    private static final long serialVersionUID = 1L;

    public StatementNamespace(final @NonNull String name) {
        super(name);
    }

    // FIXME: is this subclass useful at all?
    public static class TreeScoped<K, D extends DeclaredStatement<?>, E extends EffectiveStatement<?, D>>
            extends StatementNamespace<K, D, E> {
        @Serial
        private static final long serialVersionUID = 1L;

        public TreeScoped(final @NonNull String name) {
            super(name);
        }
    }
}
