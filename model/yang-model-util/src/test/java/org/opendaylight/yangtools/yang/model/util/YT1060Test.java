/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
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

public class YT1060Test {
    private static final QName CONT = QName.create("parent", "cont");
    private static final QName LEAF1 = QName.create(CONT, "leaf1");

    private EffectiveModelContext context;
    private PathExpression path;

    @Before
    public void before() {
        context = YangParserTestUtils.parseYangResourceDirectory("/yt1060");

        final var module = context.findModule(CONT.getModule()).orElseThrow();
        final var cont = (ContainerSchemaNode) module.getDataChildByName(CONT);
        final var leaf1 = (LeafSchemaNode) cont.getDataChildByName(LEAF1);
        path = ((LeafrefTypeDefinition) leaf1.getType()).getPathStatement();

        // Quick checks before we get to the point
        final var pathSteps = path.getSteps();
        assertThat(pathSteps, isA(LocationPathSteps.class));
        final var locationPath = ((LocationPathSteps) pathSteps).getLocationPath();
        assertTrue(locationPath.isAbsolute());
        final var steps = locationPath.getSteps();
        assertEquals(2, steps.size());
        steps.forEach(step -> assertThat(step, isA(ResolvedQNameStep.class)));
    }

    @Test
    public void testFindDataSchemaNodeAbsolutePathImportedModule() {
        final var foundStmt = SchemaInferenceStack.ofDataTreePath(context, CONT, LEAF1).resolvePathExpression(path);
        assertThat(foundStmt, isA(LeafSchemaNode.class));
        assertEquals(QName.create(XMLNamespace.of("imported"), "leaf1"), ((LeafSchemaNode) foundStmt).getQName());

        // since this is absolute path with prefixes stack should be able to resolve it from any state
        final var foundStmtSecond = SchemaInferenceStack.of(context).resolvePathExpression(path);
        assertSame(foundStmt, foundStmtSecond);
    }
}
