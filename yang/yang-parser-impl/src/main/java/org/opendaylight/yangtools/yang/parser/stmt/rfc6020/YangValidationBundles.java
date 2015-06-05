/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.HashMap;

import java.util.Arrays;
import java.util.HashSet;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public final class YangValidationBundles {

    public static final HashSet<StatementDefinition> SUPPORTED_REFINE_SUBSTATEMENTS = new HashSet<StatementDefinition>(
            Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.DEFAULT,
                    Rfc6020Mapping.DESCRIPTION, Rfc6020Mapping.REFERENCE,
                    Rfc6020Mapping.CONFIG, Rfc6020Mapping.MANDATORY,
                    Rfc6020Mapping.MUST, Rfc6020Mapping.PRESENCE,
                    Rfc6020Mapping.MIN_ELEMENTS, Rfc6020Mapping.MAX_ELEMENTS }));

    public static final HashMap<StatementDefinition, HashSet<StatementDefinition>> SUPPORTED_REFINE_TARGETS = new HashMap<StatementDefinition, HashSet<StatementDefinition>>();
    static {
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.DEFAULT, new HashSet<StatementDefinition>(
            Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.LEAF, Rfc6020Mapping.CHOICE })));
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.MANDATORY, new HashSet<StatementDefinition>(
                Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.LEAF, Rfc6020Mapping.CHOICE, Rfc6020Mapping.ANYXML })));
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.PRESENCE, new HashSet<StatementDefinition>(
                Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.CONTAINER })));
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.MUST, new HashSet<StatementDefinition>(
                Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.CONTAINER,
                        Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF,
                        Rfc6020Mapping.LEAF_LIST, Rfc6020Mapping.ANYXML, })));
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.MIN_ELEMENTS, new HashSet<StatementDefinition>(
                Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF_LIST })));
        SUPPORTED_REFINE_TARGETS.put(Rfc6020Mapping.MAX_ELEMENTS, new HashSet<StatementDefinition>(
                Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF_LIST })));
    }


    public static final HashSet<StatementDefinition> SUPPORTED_AUGMENT_TARGETS = new HashSet<StatementDefinition>(
            Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.CONTAINER,
                    Rfc6020Mapping.LIST, Rfc6020Mapping.CASE,
                    Rfc6020Mapping.INPUT, Rfc6020Mapping.OUTPUT,
                    Rfc6020Mapping.NOTIFICATION, Rfc6020Mapping.CHOICE,
                    Rfc6020Mapping.RPC }));

    public static final HashSet<StatementDefinition> SUPPORTED_CASE_SHORTHANDS = new HashSet<StatementDefinition>(
            Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.CONTAINER,
                    Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF,
                    Rfc6020Mapping.LEAF_LIST, Rfc6020Mapping.ANYXML, }));

    public static final HashSet<StatementDefinition> SUPPORTED_DATA_NODES = new HashSet<StatementDefinition>(
            Arrays.asList(new StatementDefinition[] { Rfc6020Mapping.CONTAINER,
                    Rfc6020Mapping.LIST, Rfc6020Mapping.LEAF,
                    Rfc6020Mapping.LEAF_LIST, Rfc6020Mapping.ANYXML, }));

    private YangValidationBundles() {
        throw new UnsupportedOperationException("Utility class");
    }
}
