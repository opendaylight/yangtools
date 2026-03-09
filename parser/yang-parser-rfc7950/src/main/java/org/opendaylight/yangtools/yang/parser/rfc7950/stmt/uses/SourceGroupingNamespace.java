/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace.Writable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class SourceGroupingNamespace {
    static final @NonNull Writable<Empty, StmtContext<QName, GroupingStatement, GroupingEffectiveStatement>> INSTANCE =
        ParserNamespace.writable("sourceGrouping");
    public static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.statementLocal(INSTANCE);

    private SourceGroupingNamespace() {
        // Hidden on purpose
    }
}
