/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

class EffectiveBuildTest {
    private static final QNameModule SIMPLE_MODULE_QNAME = QNameModule.of("simple.yang");
    private static final YangIRSource SIMPLE_MODULE =
        sourceForResource("/stmt-test/effective-build/simple-module.yang");
    private static final YangIRSource YANG_EXT = sourceForResource("/stmt-test/extensions/yang-ext.yang");

    @Test
    void effectiveBuildTest() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild().addSource(SIMPLE_MODULE).buildEffective();

        assertNotNull(result);

        var simpleModule = result.findModules("simple-module").iterator().next();
        assertNotNull(simpleModule);

        final var q1 = QName.create(SIMPLE_MODULE_QNAME, "root-container");
        final var q2 = QName.create(SIMPLE_MODULE_QNAME, "sub-container");
        final var q3 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container");
        final var q4 = QName.create(SIMPLE_MODULE_QNAME, "root-container2");
        final var q5 = QName.create(SIMPLE_MODULE_QNAME, "sub-container2");
        final var q6 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container2");
        final var q7 = QName.create(SIMPLE_MODULE_QNAME, "grp");

        var rootCon = assertInstanceOf(ContainerSchemaNode.class, simpleModule.getDataChildByName(q1));
        var subCon = assertInstanceOf(ContainerSchemaNode.class, rootCon.getDataChildByName(q2));
        var subSubCon = assertInstanceOf(ContainerSchemaNode.class, subCon.getDataChildByName(q3));
        assertEquals(q3, subSubCon.getQName());
        var rootCon2 = assertInstanceOf(ContainerSchemaNode.class, simpleModule.getDataChildByName(q4));
        var subCon2 = assertInstanceOf(ContainerSchemaNode.class, rootCon2.getDataChildByName(q5));
        var subSubCon2 = assertInstanceOf(ContainerSchemaNode.class, subCon2.getDataChildByName(q6));
        assertEquals(q6, subSubCon2.getQName());

        var grp = simpleModule.getGroupings().iterator().next();
        assertNotNull(grp);
        assertEquals(q7, grp.getQName());

        var grpSubCon2 = assertInstanceOf(ContainerSchemaNode.class,  grp.getDataChildByName(q5));
        var grpSubSubCon2 = assertInstanceOf(ContainerSchemaNode.class, grpSubCon2.getDataChildByName(q6));
        assertEquals(q6, grpSubSubCon2.getQName());
    }

    @Test
    void extensionsTest() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild().addSource(YANG_EXT).buildEffective();
        assertNotNull(result);

        var groupings = result.getGroupings();
        assertEquals(1, groupings.size());

        var grp = groupings.iterator().next();

        var childNodes = grp.getChildNodes();
        assertEquals(1, childNodes.size());

        var leaf = assertInstanceOf(LeafSchemaNode.class, childNodes.iterator().next());

        assertNotNull(leaf.getType());
    }

    @Test
    void mockTest() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild().addSource(YANG_EXT).buildEffective();
        assertNotNull(result);
    }
}
