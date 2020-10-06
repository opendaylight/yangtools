/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective view of a {@link ModuleStatement}.
 */
@Beta
public interface ModuleEffectiveStatement extends DataTreeAwareEffectiveStatement<UnqualifiedQName, ModuleStatement> {
    /**
     * Namespace mapping all known prefixes in a module to their modules. Note this namespace includes the module
     * in which it is instantiated.
     */
    abstract class PrefixToEffectiveModuleNamespace
            implements IdentifierNamespace<String, @NonNull ModuleEffectiveStatement> {
        private PrefixToEffectiveModuleNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Namespace mapping all known {@link QNameModule}s to their encoding prefixes. This includes the declaration
     * from prefix/namespace/revision and all imports as they were resolved.
     */
    abstract class QNameModuleToPrefixNamespace implements IdentifierNamespace<QNameModule, @NonNull String> {
        private QNameModuleToPrefixNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Namespace mapping all included submodules. The namespaces is keyed by submodule name.
     */
    abstract class NameToEffectiveSubmoduleNamespace
            implements IdentifierNamespace<String, @NonNull SubmoduleEffectiveStatement> {
        private NameToEffectiveSubmoduleNamespace() {
            // This class should never be subclassed
        }
    }

    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.MODULE;
    }

    /**
     * Get the local QNameModule of this module. All implementations need to override this default method.
     *
     * @return Local QNameModule
     */
    @NonNull QNameModule localQNameModule();
}
