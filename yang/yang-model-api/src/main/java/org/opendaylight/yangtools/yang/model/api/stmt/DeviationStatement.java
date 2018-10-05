/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

public interface DeviationStatement extends DocumentedDeclaredStatement<SchemaNodeIdentifier> {
    // FIXME: YANGTOOLS-908: this should not be needed here
    @Override
    @NonNull SchemaNodeIdentifier argument();

    default @NonNull SchemaNodeIdentifier getTargetNode() {
        return argument();
    }

    default @NonNull Collection<? extends DeviateStatement> getDeviateStatements() {
        return declaredSubstatements(DeviateStatement.class);
    }
}
