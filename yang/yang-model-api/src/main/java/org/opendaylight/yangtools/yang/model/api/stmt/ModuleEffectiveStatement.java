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
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Effective view of a {@link ModuleStatement}.
 */
@Beta
// FIXME: 4.0.0: we should reshuffle the String here, as module name is in reality a YANG identifier, e.g. not just
//               an ordinary String. We really want this to be a QName, so that we do not need the localQNameModule
//               bit, but that may be problematic with ModuleStatement, which is getting created before we even know
//               the namespace :( A type capture of the string may just be sufficient.
public interface ModuleEffectiveStatement extends DataTreeAwareEffectiveStatement<String, ModuleStatement> {
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
     * Namespace containing all effective modules which are applying deviations to this module.
     */
    abstract class DeviatingEffectiveModuleNamespace
            implements IdentifierNamespace<@NonNull QNameModule, @NonNull ModuleEffectiveStatement> {
        private DeviatingEffectiveModuleNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Get the local QNameModule of this module. All implementations need to override this default method.
     *
     * @return Local QNameModule
     */
    @NonNull QNameModule localQNameModule();

    /**
     * Return the conformance type of this module.
     *
     * @return conformance type of this module.
     */
    // FIXME: 3.0.0 make this method non-default
    default @NonNull ConformanceType conformanceLevel() {
        return ConformanceType.IMPLEMENT;
    }
}
