/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;

/**
 * Utility class holding various groups as defined in RFC7950.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public final class StatementGroup {
    /**
     * The set of {@code data definition statement}s, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-3">RFC7950, section 3</a>.
     * <pre>{@code
     *   data definition statement: A statement that defines new data
     *   nodes.  One of "container", "leaf", "leaf-list", "list", "choice",
     *   "case", "augment", "uses", "anydata", and "anyxml".
     * }</pre>
     */
    public static final Set<StatementDefinition<?, ?, ?>> DATA_DEFINITION_STATEMENT = Set.of(
        ContainerStatement.DEF, LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF, ChoiceStatement.DEF,
        CaseStatement.DEF, AugmentStatement.DEF, UsesStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF);
    /**
     * The set of {@code data node statement}s, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-3">RFC7950, section 3</a>.
     * <pre>{@code
     *   data node: A node in the schema tree that can be instantiated in a
     *   data tree.  One of container, leaf, leaf-list, list, anydata, and
     *   anyxml.
     * }</pre>
     */
    public static final Set<StatementDefinition<?, ?, ?>> DATA_NODE = Set.of(
        ContainerStatement.DEF, LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF, AnydataStatement.DEF,
        AnyxmlStatement.DEF);

    /**
     * The set of {@code schema node statement}s, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-3">RFC7950, section 3</a>.
     * <pre>{@code
     *   schema node: A node in the schema tree.  One of action, container,
     *   leaf, leaf-list, list, choice, case, rpc, input, output,
     *   notification, anydata, and anyxml.
     * }</pre>
     */
    public static final Set<StatementDefinition<?, ?, ?>> SCHEMA_NODE = Set.of(
        ActionStatement.DEF, ContainerStatement.DEF, LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF,
        ChoiceStatement.DEF, CaseStatement.DEF, RpcStatement.DEF, InputStatement.DEF, OutputStatement.DEF,
        NotificationStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF);

    // FIXME: also document these groups

    //module-header-stmts = ;; these stmts can appear in any order
    //        yang-version-stmt
    //        namespace-stmt
    //        prefix-stmt
    public static final Set<StatementDefinition<?, ?, ?>> MODULE_HEADER_STMTS = Set.of(
        YangVersionStatement.DEF, NamespaceStatement.DEF, PrefixStatement.DEF);

    //submodule-header-stmts =
    //        ;; these stmts can appear in any order
    //        yang-version-stmt
    //        belongs-to-stmt
    public static final Set<StatementDefinition<?, ?, ?>> SUBMODULE_HEADER_STMTS = Set.of(
        YangVersionStatement.DEF, BelongsToStatement.DEF);

    //meta-stmts          = ;; these stmts can appear in any order
    //        [organization-stmt]
    //        [contact-stmt]
    //        [description-stmt]
    //        [reference-stmt]
    public static final Set<StatementDefinition<?, ?, ?>> META_STMTS = Set.of(
        OrganizationStatement.DEF, ContactStatement.DEF, DescriptionStatement.DEF, ReferenceStatement.DEF);

    //linkage-stmts       = ;; these stmts can appear in any order
    //        *import-stmt
    //        *include-stmt
    public static final Set<StatementDefinition<?, ?, ?>> LINKAGE_STMTS = Set.of(
        ImportStatement.DEF, IncludeStatement.DEF);

    private StatementGroup() {
        // Hidden on purpose
    }
}
