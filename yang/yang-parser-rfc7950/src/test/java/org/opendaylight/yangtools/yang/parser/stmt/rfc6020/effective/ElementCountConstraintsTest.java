/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class ElementCountConstraintsTest {
    @Test
    public void testElementCountConstraints() throws ParseException, ReactorException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        final SchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(StmtTestUtils.sourceForResource("/constraint-definitions-test/foo.yang"))
                .buildEffective();
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModule("foo", Revision.of("2016-09-20")).get();
        final LeafListSchemaNode constrainedLeafList1 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-1"));
        assertNotNull(constrainedLeafList1);
        ElementCountConstraint constraints1 = constrainedLeafList1.getElementCountConstraint().get();

        final LeafListSchemaNode constrainedLeafList2 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-2"));
        assertNotNull(constrainedLeafList2);
        ElementCountConstraint constraints2 = constrainedLeafList2.getElementCountConstraint().get();

        assertEquals(constraints1.hashCode(), constraints2.hashCode());
        assertEquals(constraints1, constraints2);

        final LeafListSchemaNode constrainedLeafList3 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-3"));
        assertNotNull(constrainedLeafList3);
        ElementCountConstraint constraints3 = constrainedLeafList3.getElementCountConstraint().get();

        assertNotEquals(constraints2.hashCode(), constraints3.hashCode());
        assertNotEquals(constraints2, constraints3);

        final LeafListSchemaNode constrainedLeafList4 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-4"));
        assertNotNull(constrainedLeafList4);
        ElementCountConstraint constraints4 = constrainedLeafList4.getElementCountConstraint().get();

        assertNotEquals(constraints3.hashCode(), constraints4.hashCode());
        assertNotEquals(constraints3, constraints4);

        assertEquals("ElementCountConstraint{minElements=50, maxElements=100}", constraints4.toString());
    }
}