/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective view of a {@code module} statement.
 */
public non-sealed interface ModuleEffectiveStatement
        extends DataTreeAwareEffectiveStatement<Unqualified, ModuleStatement>,
                RootEffectiveStatement<ModuleStatement>,
                TypedefAwareEffectiveStatement<Unqualified, ModuleStatement>,
                SchemaTreeRoot {
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
     * Return this statement's {@code namespace} substatement.
     *
     * @implSpec
     *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
     *      {@link VerifyException} if a matching substatement is not found.
     * @return A {@link NamespaceEffectiveStatement}
     */
    default @NonNull NamespaceEffectiveStatement namespace() {
        return DefaultMethodHelpers.verifySubstatement(this, NamespaceEffectiveStatement.class);
    }

    /**
     * Return this statement's {@code prefix} substatement.
     *
     * @implSpec
     *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
     *      {@link VerifyException} if a matching substatement is not found.
     * @return A {@link PrefixEffectiveStatement}
     */
    default @NonNull PrefixEffectiveStatement prefix() {
        return DefaultMethodHelpers.verifyPrefixSubstatement(this);
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

    /**
     * Namespace of available extensions. According to RFC7950 section 6.2.1:
     * <pre>
     *     All extension names defined in a module and its submodules share
     *     the same extension identifier namespace.
     * </pre>
     *
     * @return All {@link ExtensionEffectiveStatement}s defined in this module
     */
    default @NonNull Collection<ExtensionEffectiveStatement> extensions() {
        return collectEffectiveSubstatements(ExtensionEffectiveStatement.class);
    }

    /**
     * Lookup an {@link ExtensionEffectiveStatement} by its {@link QName}.
     *
     * @param qname identity name
     * @return Corresponding extension, or empty
     * @throws NullPointerException if {@code qname} is {@code null}
     */
    // FIXME: qname is implied to implied to have the same namespace as localQNameModule(), hence this should be driven
    //        through getLocalName()
    @NonNull Optional<ExtensionEffectiveStatement> findExtension(@NonNull QName qname);

    /**
     * Namespace of available features. According to RFC7950 section 6.2.1:
     * <pre>
     *     All feature names defined in a module and its submodules share the
     *     same feature identifier namespace.
     * </pre>
     *
     * @return All {@link FeatureEffectiveStatement}s defined in this module
     */
    default @NonNull Collection<FeatureEffectiveStatement> features() {
        return collectEffectiveSubstatements(FeatureEffectiveStatement.class);
    }

    /**
     * Lookup an {@link FeatureEffectiveStatement} by its {@link QName}.
     *
     * @param qname identity name
     * @return Corresponding feature, or empty
     * @throws NullPointerException if {@code qname} is {@code null}
     */
    // FIXME: qname is implied to implied to have the same namespace as localQNameModule(), hence this should be driven
    //        through getLocalName()
    @NonNull Optional<FeatureEffectiveStatement> findFeature(@NonNull QName qname);

    /**
     * Namespace of available identities. According to RFC7950 section 6.2.1:
     * <pre>
     *     All identity names defined in a module and its submodules share
     *     the same identity identifier namespace.
     * </pre>
     *
     * @return All {@link IdentityEffectiveStatement}s defined in this module
     */
    default @NonNull Collection<IdentityEffectiveStatement> identities() {
        return collectEffectiveSubstatements(IdentityEffectiveStatement.class);
    }

    /**
     * Lookup an {@link IdentityEffectiveStatement} by its {@link QName}.
     *
     * @param qname identity name
     * @return Corresponding identity, or empty
     * @throws NullPointerException if {@code qname} is {@code null}
     */
    // FIXME: qname is implied to implied to have the same namespace as localQNameModule(), hence this should be driven
    //        through getLocalName()
    @NonNull Optional<IdentityEffectiveStatement> findIdentity(@NonNull QName qname);

    /**
     * All submodules included in this module, directly or transitively.
     *
     * @return All included submodules
     */
    default @NonNull Collection<SubmoduleEffectiveStatement> submodules() {
        return collectEffectiveSubstatements(SubmoduleEffectiveStatement.class);
    }

    /**
     * Namespace mapping all included submodules. The namespaces is keyed by submodule name, as represented by
     * {@link SubmoduleEffectiveStatement#argument()}.
     *
     * @return submoduleName Included submodule, or empty
     * @throws NullPointerException if {@code submoduleName} is {@code null}
     */
    @NonNull Optional<SubmoduleEffectiveStatement> findSubmodule(@NonNull Unqualified submoduleName);
}
