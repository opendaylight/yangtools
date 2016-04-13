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
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public final class YangValidationBundles {
    private YangValidationBundles() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final Set<StatementDefinition> SUPPORTED_REFINE_SUBSTATEMENTS = ImmutableSet.of(
        Rfc6020Mapping.DEFAULT, Rfc6020Mapping.DESCRIPTION, Rfc6020Mapping.REFERENCE, Rfc6020Mapping.CONFIG,
        Rfc6020Mapping.MANDATORY, Rfc6020Mapping.MUST, Rfc6020Mapping.PRESENCE, Rfc6020Mapping.MIN_ELEMENTS,
        Rfc6020Mapping.MAX_ELEMENTS);

    public static final Map<StatementDefinition, Set<StatementDefinition>> SUPPORTED_REFINE_TARGETS;
    static {
        final Builder<StatementDefinition, Set<StatementDefinition>> b = ImmutableMap.builder();
        b.put(Rfc6020Mapping.DEFAULT, ImmutableSet.of(Rfc6020Mapping.LEAF, Rfc6020Mapping.CHOICE));
        b.put(Rfc6020Mapping.MANDATORY, ImmutableSet.of(
                Rfc6020Mapping.LEAF, Rfc6020Mapping.CHOICE, Rfc6020Mapping.ANYXML));
        b.put(Rfc6020Mapping.PRESENCE, ImmutableSet.of(Rfc6020Mapping.CONTAINER));
        b.put(Rfc6020Mapping.MUST, ImmutableSet.of(
                Rfc6020Mapping.CONTAINER, Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF,
                Rfc6020Mapping.LEAF_LIST, Rfc6020Mapping.ANYXML));
        b.put(Rfc6020Mapping.MIN_ELEMENTS, ImmutableSet.of(
                Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF_LIST));
        b.put(Rfc6020Mapping.MAX_ELEMENTS, ImmutableSet.of(
                Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF_LIST));
        SUPPORTED_REFINE_TARGETS = b.build();
    }


    public static final Set<StatementDefinition> SUPPORTED_AUGMENT_TARGETS = ImmutableSet.of(
        Rfc6020Mapping.CONTAINER, Rfc6020Mapping.LIST, Rfc6020Mapping.CASE, Rfc6020Mapping.INPUT, Rfc6020Mapping.OUTPUT,
        Rfc6020Mapping.NOTIFICATION, Rfc6020Mapping.CHOICE, Rfc6020Mapping.RPC);

    public static final Set<StatementDefinition> SUPPORTED_CASE_SHORTHANDS = ImmutableSet.of(
        Rfc6020Mapping.CONTAINER, Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF, Rfc6020Mapping.LEAF_LIST,
        Rfc6020Mapping.ANYXML);

    public static final Set<StatementDefinition> SUPPORTED_DATA_NODES = ImmutableSet.of(
        Rfc6020Mapping.CONTAINER, Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF, Rfc6020Mapping.LEAF_LIST,
        Rfc6020Mapping.ANYXML);
}
