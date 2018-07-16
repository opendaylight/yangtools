/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface LinkageDeclaredStatement extends DeclaredStatement<String>, LinkageGroup {
    @Override
    default @Nonnull Collection<? extends ImportStatement> getImports() {
        return declaredSubstatements(ImportStatement.class);
    }

    @Override
    default @Nonnull Collection<? extends IncludeStatement> getIncludes() {
        return declaredSubstatements(IncludeStatement.class);
    }
}
