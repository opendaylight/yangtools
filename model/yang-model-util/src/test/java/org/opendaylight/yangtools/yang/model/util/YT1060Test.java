/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.ResolvedQNameStep;

class YT1060Test {
    private static final QName CONT = QName.create("parent", "cont");
    private static final QName LEAF1 = QName.create(CONT, "leaf1");

    private EffectiveModelContext context;
    private PathExpression path;

    @BeforeEach
    void before() {
        context = YangParserTestUtils.parseYangResourceDirectory("/yt1060");

        final var module = context.findModule(CONT.getModule()).orElseThrow();
        final var cont = assertInstanceOf(ContainerSchemaNode.class, module.getDataChildByName(CONT));
        final var leaf1 = assertInstanceOf(LeafSchemaNode.class, cont.getDataChildByName(LEAF1));
        path = assertInstanceOf(LeafrefTypeDefinition.class, leaf1.getType()).getPathStatement();

        // Quick checks before we get to the point
        final var pathSteps = assertInstanceOf(LocationPathSteps.class, path.getSteps());
        final var locationPath = pathSteps.getLocationPath();
        assertTrue(locationPath.isAbsolute());
        final var steps = locationPath.getSteps();
        assertEquals(2, steps.size());
        steps.forEach(step -> assertInstanceOf(ResolvedQNameStep.class, step));
    }

    @Test
    void testFindDataSchemaNodeAbsolutePathImportedModule() {
        final var foundStmt = assertInstanceOf(LeafSchemaNode.class,
            SchemaInferenceStack.ofDataTreePath(context, CONT, LEAF1).resolvePathExpression(path));
        assertEquals(QName.create(XMLNamespace.of("imported"), "leaf1"), foundStmt.getQName());

        // since this is absolute path with prefixes stack should be able to resolve it from any state
        assertSame(foundStmt, SchemaInferenceStack.of(context).resolvePathExpression(path));
    }
}
