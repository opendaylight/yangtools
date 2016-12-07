/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public final class YangValidationBundles {
    private YangValidationBundles() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final Set<StatementDefinition> SUPPORTED_REFINE_SUBSTATEMENTS = ImmutableSet.of(
        YangStmtMapping.DEFAULT, YangStmtMapping.DESCRIPTION, YangStmtMapping.REFERENCE, YangStmtMapping.CONFIG,
        YangStmtMapping.MANDATORY, YangStmtMapping.MUST, YangStmtMapping.PRESENCE, YangStmtMapping.MIN_ELEMENTS,
        YangStmtMapping.MAX_ELEMENTS);

    public static final Map<StatementDefinition, Set<StatementDefinition>> SUPPORTED_REFINE_TARGETS;
    static {
        final Builder<StatementDefinition, Set<StatementDefinition>> b = ImmutableMap.builder();
        b.put(YangStmtMapping.DEFAULT, ImmutableSet.of(YangStmtMapping.LEAF, YangStmtMapping.CHOICE));
        b.put(YangStmtMapping.MANDATORY, ImmutableSet.of(
                YangStmtMapping.LEAF, YangStmtMapping.CHOICE, YangStmtMapping.ANYXML, YangStmtMapping.ANYDATA));
        b.put(YangStmtMapping.PRESENCE, ImmutableSet.of(YangStmtMapping.CONTAINER));
        b.put(YangStmtMapping.MUST, ImmutableSet.of(
                YangStmtMapping.CONTAINER, YangStmtMapping.LIST, YangStmtMapping.LEAF,
                YangStmtMapping.LEAF_LIST, YangStmtMapping.ANYXML, YangStmtMapping.ANYDATA));
        b.put(YangStmtMapping.MIN_ELEMENTS, ImmutableSet.of(
                YangStmtMapping.LIST, YangStmtMapping.LEAF_LIST));
        b.put(YangStmtMapping.MAX_ELEMENTS, ImmutableSet.of(
                YangStmtMapping.LIST, YangStmtMapping.LEAF_LIST));
        SUPPORTED_REFINE_TARGETS = b.build();
    }


    public static final Set<StatementDefinition> SUPPORTED_AUGMENT_TARGETS = ImmutableSet.of(
        YangStmtMapping.CONTAINER, YangStmtMapping.LIST, YangStmtMapping.CASE, YangStmtMapping.INPUT, YangStmtMapping.OUTPUT,
        YangStmtMapping.NOTIFICATION, YangStmtMapping.CHOICE, YangStmtMapping.RPC);

    public static final Set<StatementDefinition> SUPPORTED_CASE_SHORTHANDS = ImmutableSet.of(
        YangStmtMapping.CONTAINER, YangStmtMapping.LIST, YangStmtMapping.LEAF, YangStmtMapping.LEAF_LIST,
        YangStmtMapping.ANYXML, YangStmtMapping.ANYDATA);

    public static final Set<StatementDefinition> SUPPORTED_DATA_NODES = ImmutableSet.of(
        YangStmtMapping.CONTAINER, YangStmtMapping.LIST, YangStmtMapping.LEAF, YangStmtMapping.LEAF_LIST,
        YangStmtMapping.ANYXML, YangStmtMapping.ANYDATA);
}
