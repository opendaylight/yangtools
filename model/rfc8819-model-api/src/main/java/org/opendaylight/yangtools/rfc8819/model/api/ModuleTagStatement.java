/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;


import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'module-tag' extension defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc8819">RFC8819</a>.
 */
@Beta
public interface ModuleTagStatement extends UnknownStatement<Tag> {
    @Override
    default @NonNull StatementDefinition statementDefinition() {
        return ModuleTagStatements.MODULE_TAG;
    }
}
