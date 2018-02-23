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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Effective view of a {@link ModuleStatement}.
 */
@Beta
public interface ModuleEffectiveStatement extends EffectiveStatement<String, ModuleStatement> {
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

    /**
     * Namespace mapping all identities defined in this module (and its submodules). Identities are keyed by their
     * QNameModule.
     */
    abstract class QNameToEffectiveIdentityNamespace
            implements IdentifierNamespace<QName, @NonNull SubmoduleEffectiveStatement> {
        private QNameToEffectiveIdentityNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Get the local QNameModule of this module. All implementations need to override this default method.
     *
     * @return Local QNameModule
     */
    // FIXME: 3.0.0 make this method non-default
    default @NonNull QNameModule localQNameModule() {
        throw new UnsupportedOperationException(getClass() + " is missing an implementation");
    }
}
