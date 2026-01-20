/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Effective statement representation of 'mount-point' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8528">RFC8528</a>.
 */
public interface MountPointEffectiveStatement
        extends UnknownEffectiveStatement<MountPointLabel, @NonNull MountPointStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return MountPointStatement.DEFINITION;
    }
}
