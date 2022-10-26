/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective view of a {@code module} statement.
 */
public interface ModuleEffectiveStatement
        extends DataTreeAwareEffectiveStatement<Unqualified, ModuleStatement>, SchemaTreeRoot {
    /**
     * Namespace mapping all known prefixes in a module to their modules. Note this namespace includes the module
     * in which it is instantiated.
     */
    abstract class PrefixToEffectiveModuleNamespace
            extends IdentifierNamespace<String, @NonNull ModuleEffectiveStatement> {
        private PrefixToEffectiveModuleNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Namespace mapping all known {@link QNameModule}s to their encoding prefixes. This includes the declaration
     * from prefix/namespace/revision and all imports as they were resolved.
     */
    abstract class QNameModuleToPrefixNamespace extends IdentifierNamespace<QNameModule, @NonNull String> {
        private QNameModuleToPrefixNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Namespace mapping all included submodules. The namespaces is keyed by submodule name.
     */
    abstract class NameToEffectiveSubmoduleNamespace
            extends IdentifierNamespace<Unqualified, @NonNull SubmoduleEffectiveStatement> {
        private NameToEffectiveSubmoduleNamespace() {
            // This class should never be subclassed
        }
    }

    /**
     * Conformance type, as defined by <a href="https://datatracker.ietf.org/doc/html/rfc7895#page-9">RFC7895</a> and
     * indirectly referenced in <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-5.6.4">RFC7950</a>. The
     * NMDA revision of <a href="https://datatracker.ietf.org/doc/html/rfc8525">YANG Library</a> does not directly
     * define these, but makes a distiction on the same concept.
     */
    enum ConformanceType {
        /**
         * This module is being implemented. As per RFC7895:
         * <pre>
         *   Indicates that the server implements one or more
         *   protocol-accessible objects defined in the YANG module
         *   identified in this entry.  This includes deviation
         *   statements defined in the module.
         *
         *   For YANG version 1.1 modules, there is at most one
         *   module entry with conformance type 'implement' for a
         *   particular module name, since YANG 1.1 requires that,
         *   at most, one revision of a module is implemented.
         *
         *   For YANG version 1 modules, there SHOULD NOT be more
         *   than one module entry for a particular module name.
         * </pre>
         */
        IMPLEMENT,
        /**
         * This module is being used only for reusable constructs. As per RFC7895:
         * <pre>
         *   Indicates that the server imports reusable definitions
         *   from the specified revision of the module but does
         *   not implement any protocol-accessible objects from
         *   this revision.
         *
         *   Multiple module entries for the same module name MAY
         *   exist.  This can occur if multiple modules import the
         *   same module but specify different revision dates in
         *   the import statements.
         * </pre>
         */
        IMPORT;
    }

    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.MODULE;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     *     Default implementation defers to {@link #findSchemaTreeNode(java.util.List)}.
     */
    @Override
    default Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final SchemaNodeIdentifier path) {
        return findSchemaTreeNode(path.getNodeIdentifiers());
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
     * @return Conformance type.
     */
    @NonNull ConformanceType conformance();
}
