/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.util.ConstraintDefinitions;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class ConstraintDefinitionsTest {

    @Test
    public void testConstraintDefinitions() throws ParseException, ReactorException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        reactor.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/constraint-definitions-test/foo.yang")));
        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModuleByName("foo",
            SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-20"));
        assertNotNull(testModule);

        final LeafSchemaNode mandatoryLeaf1 = (LeafSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "mandatory-leaf-1"));
        assertNotNull(mandatoryLeaf1);
        ConstraintDefinition constraints1 = mandatoryLeaf1.getConstraints();

        final LeafSchemaNode mandatoryLeaf2 = (LeafSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "mandatory-leaf-2"));
        assertNotNull(mandatoryLeaf2);
        ConstraintDefinition constraints2 = mandatoryLeaf2.getConstraints();

        assertEquals(ConstraintDefinitions.hashCode(constraints1), ConstraintDefinitions.hashCode(constraints2));
        assertTrue(ConstraintDefinitions.equals(constraints1, constraints2));

        assertTrue(ConstraintDefinitions.equals(constraints1, constraints1));
        assertFalse(ConstraintDefinitions.equals(constraints1, "str"));

        final LeafSchemaNode mandatoryLeaf3 = (LeafSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "mandatory-leaf-3"));
        assertNotNull(mandatoryLeaf3);
        ConstraintDefinition constraints3 = mandatoryLeaf3.getConstraints();

        assertNotEquals(ConstraintDefinitions.hashCode(constraints2), ConstraintDefinitions.hashCode(constraints3));
        assertFalse(ConstraintDefinitions.equals(constraints2, constraints3));

        final LeafSchemaNode mandatoryLeaf4 = (LeafSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "mandatory-leaf-4"));
        assertNotNull(mandatoryLeaf4);
        ConstraintDefinition constraints4 = mandatoryLeaf4.getConstraints();

        assertNotEquals(ConstraintDefinitions.hashCode(constraints3), ConstraintDefinitions.hashCode(constraints4));
        assertFalse(ConstraintDefinitions.equals(constraints3, constraints4));

        final LeafSchemaNode mandatoryLeaf5 = (LeafSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "mandatory-leaf-5"));
        assertNotNull(mandatoryLeaf5);
        final ConstraintDefinition constraints5 = mandatoryLeaf5.getConstraints();

        assertNotEquals(ConstraintDefinitions.hashCode(constraints4), ConstraintDefinitions.hashCode(constraints5));
        assertFalse(ConstraintDefinitions.equals(constraints4, constraints5));

        final LeafListSchemaNode constrainedLeafList1 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-1"));
        assertNotNull(constrainedLeafList1);
        constraints1 = constrainedLeafList1.getConstraints();

        final LeafListSchemaNode constrainedLeafList2 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-2"));
        assertNotNull(constrainedLeafList2);
        constraints2 = constrainedLeafList2.getConstraints();

        assertEquals(ConstraintDefinitions.hashCode(constraints1), ConstraintDefinitions.hashCode(constraints2));
        assertTrue(ConstraintDefinitions.equals(constraints1, constraints2));

        final LeafListSchemaNode constrainedLeafList3 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-3"));
        assertNotNull(constrainedLeafList3);
        constraints3 = constrainedLeafList3.getConstraints();

        assertNotEquals(ConstraintDefinitions.hashCode(constraints2), ConstraintDefinitions.hashCode(constraints3));
        assertFalse(ConstraintDefinitions.equals(constraints2, constraints3));

        final LeafListSchemaNode constrainedLeafList4 = (LeafListSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "constrained-leaf-list-4"));
        assertNotNull(constrainedLeafList4);
        constraints4 = constrainedLeafList4.getConstraints();

        assertNotEquals(ConstraintDefinitions.hashCode(constraints3), ConstraintDefinitions.hashCode(constraints4));
        assertFalse(ConstraintDefinitions.equals(constraints3, constraints4));

        final String constraintsString = ConstraintDefinitions.toString(constraints4);
        assertEquals("EffectiveConstraintDefinitionImpl{whenCondition=foo = 'bar', mustConstraints=[bar != 'foo'], "
                + "mandatory=true, minElements=50, maxElements=100}", constraintsString);
    }
}