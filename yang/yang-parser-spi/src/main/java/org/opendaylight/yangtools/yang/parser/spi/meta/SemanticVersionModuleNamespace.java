/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;

/**
 * Namespace class for storing Maps of all modules with the same name. This namespace is
 * used only in case the semantic versioning is enabled, otherwise it is empty.
 */
@Beta
public interface SemanticVersionModuleNamespace
    extends StatementNamespace<SemVerSourceIdentifier, ModuleStatement, ModuleEffectiveStatement> {
    NamespaceBehaviour<SemVerSourceIdentifier, StmtContext<?, ModuleStatement, ModuleEffectiveStatement>,
            @NonNull SemanticVersionModuleNamespace> BEHAVIOUR =
            NamespaceBehaviour.global(SemanticVersionModuleNamespace.class);

}
