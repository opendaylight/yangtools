/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.io.Serial;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Projection of {@link StatementSupport}s available within a particular source. This namespace is purely virtual and
 * its behaviour corresponds to {@link NamespaceBehaviour#rootStatementLocal(ParserNamespace)} and is always available.
 * Its contents are derived from {@link StatementSupportBundle}s active in the current {@link ModelProcessingPhase} as
 * well as {@link StatementDefinitions} and {@link StmtContext#yangVersion()} of the source root statement.
 */
@Beta
public final class StatementSupportNamespace extends ParserNamespace<QName, StatementSupport<?, ?, ?>> {
    @Serial
    private static final long serialVersionUID = 1L;

    StatementSupportNamespace() {
        // FIXME: is this really used?!
    }
}
