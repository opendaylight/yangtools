/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.reactor;

import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1;
import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1_1;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;

public final class YangValidationBundles {
    public static final Set<StatementDefinition> SUPPORTED_REFINE_SUBSTATEMENTS = ImmutableSet.of(
        DefaultStatement.DEF, DescriptionStatement.DEF, ReferenceStatement.DEF,
        ConfigStatement.DEF, MandatoryStatement.DEF, MustStatement.DEF,
        PresenceStatement.DEF, MinElementsStatement.DEF, MaxElementsStatement.DEF,
        IfFeatureStatement.DEF);

    public static final Map<StatementDefinition, Set<StatementDefinition>> SUPPORTED_REFINE_TARGETS =
        ImmutableMap.<StatementDefinition, Set<StatementDefinition>>builder()
            .put(DefaultStatement.DEF, ImmutableSet.of(
                LeafStatement.DEF, ChoiceStatement.DEF, LeafListStatement.DEF))
            .put(MandatoryStatement.DEF, ImmutableSet.of(
                LeafStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF,
                ChoiceStatement.DEF))
            .put(MaxElementsStatement.DEF, ImmutableSet.of(ListStatement.DEF,
                LeafListStatement.DEF))
            .put(MinElementsStatement.DEF, ImmutableSet.of(ListStatement.DEF,
                LeafListStatement.DEF))
            .put(MustStatement.DEF, ImmutableSet.of(
                LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF,
                ContainerStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF))
            .put(PresenceStatement.DEF, ImmutableSet.of(ContainerStatement.DEF))
            .build();

    private YangValidationBundles() {
        // Hidden on purpose
    }

    /**
     * Supported deviation target statements for specific deviate substatements in specific yang-version.
     * Example: deviate 'add' adds a 'default' substatement. In YANG 1.0, the target node of such deviation can be
     * only a leaf or a choice statement. IN YANG 1.1, the target node of such deviation can be a leaf, a leaf-list or
     * a choice.
     *
     * @deprecated This is an implementation leak from statement support and will be removed in next major version.
     */
    // FIXME: 7.0.0: move this to AbstractDeviateStatementSupport. This is not resolved from validation bundles at all,
    //               hence it makes sense to co-locate this with its user.
    @Deprecated
    public static final Table<YangVersion, StatementDefinition, Set<StatementDefinition>> SUPPORTED_DEVIATION_TARGETS =
            ImmutableTable.<YangVersion, StatementDefinition, Set<StatementDefinition>>builder()
        .put(VERSION_1, ConfigStatement.DEF, ImmutableSet.of(ContainerStatement.DEF,
                LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF,
                ChoiceStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF))
        .put(VERSION_1, DefaultStatement.DEF, ImmutableSet.of(LeafStatement.DEF,
                ChoiceStatement.DEF))
        .put(VERSION_1_1, DefaultStatement.DEF, ImmutableSet.of(LeafStatement.DEF,
                LeafListStatement.DEF, ChoiceStatement.DEF))
        .put(VERSION_1, MandatoryStatement.DEF, ImmutableSet.of(LeafStatement.DEF,
                ChoiceStatement.DEF, AnydataStatement.DEF, AnyxmlStatement.DEF))
        .put(VERSION_1, MaxElementsStatement.DEF, ImmutableSet.of(ListStatement.DEF,
                LeafListStatement.DEF))
        .put(VERSION_1, MinElementsStatement.DEF, ImmutableSet.of(ListStatement.DEF,
                LeafListStatement.DEF))
        .put(VERSION_1, MustStatement.DEF, ImmutableSet.of(ContainerStatement.DEF,
                LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF,
                AnyxmlStatement.DEF))
        .put(VERSION_1_1, MustStatement.DEF, ImmutableSet.of(ContainerStatement.DEF,
                LeafStatement.DEF, LeafListStatement.DEF, ListStatement.DEF,
                AnydataStatement.DEF, AnyxmlStatement.DEF, InputStatement.DEF,
                OutputStatement.DEF, NotificationStatement.DEF))
        .put(VERSION_1, TypeStatement.DEF, ImmutableSet.of(LeafStatement.DEF,
                LeafListStatement.DEF))
        .put(VERSION_1, UniqueStatement.DEF, ImmutableSet.of(ListStatement.DEF))
        .put(VERSION_1, UnitsStatement.DEF, ImmutableSet.of(LeafStatement.DEF,
                LeafListStatement.DEF))
        .build();

    public static final Set<StatementDefinition> SUPPORTED_AUGMENT_TARGETS = ImmutableSet.of(
        ContainerStatement.DEF, ListStatement.DEF, CaseStatement.DEF, InputStatement.DEF,
        OutputStatement.DEF, NotificationStatement.DEF, ChoiceStatement.DEF,
        RpcStatement.DEF);

    // FIXME: 7.0.0: consider hiding this list, as choice nodes are handling creation of implied shorthands themselves.
    //               This has implications on other members of this class, as they really seem like something which
    //               should live in corresponding StatementSupport classes.
    @Deprecated(forRemoval = true, since = "8.0.2")
    public static final Set<StatementDefinition> SUPPORTED_CASE_SHORTHANDS = ImmutableSet.of(
        ContainerStatement.DEF, ListStatement.DEF, LeafStatement.DEF, LeafListStatement.DEF,
        AnyxmlStatement.DEF, AnydataStatement.DEF);

    public static final Set<StatementDefinition> SUPPORTED_DATA_NODES = ImmutableSet.of(
        ContainerStatement.DEF, ListStatement.DEF, LeafStatement.DEF, LeafListStatement.DEF,
        AnyxmlStatement.DEF, AnydataStatement.DEF);
}
