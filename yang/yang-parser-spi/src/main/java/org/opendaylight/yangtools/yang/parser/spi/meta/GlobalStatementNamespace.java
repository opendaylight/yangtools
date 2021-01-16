/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A namespace of {@link StatementNamespace} which also has a global visibility contract as implied by
 * {@link GlobalParserNamespace}.
 */
public interface GlobalStatementNamespace<K, D extends DeclaredStatement<?>, E extends EffectiveStatement<?, D>>
        extends StatementNamespace<K, D, E>, GlobalParserNamespace<K, StmtContext<?, D, E>> {

}
