/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class YangTypes2StmtTest {

    private static final StatementStreamSource TYPEFILE1 = sourceForResource("/semantic-statement-parser/types2.yang");
    private static final StatementStreamSource TYPEFILE2 = sourceForResource("/semantic-statement-parser/types.yang");
    private static final StatementStreamSource TYPEFILE3 = sourceForResource(
            "/semantic-statement-parser/simple-types.yang");
    private static final StatementStreamSource TYPEFILE4 = sourceForResource(
            "/semantic-statement-parser/identityreftest.yang");

    private static final QNameModule TYPES2_MODULE = QNameModule.create(URI.create("types2"));

    private static final QName LF_DECIMAL = QName.create(TYPES2_MODULE, "lf-decimal");
    private static final QName LF_MY_STRING = QName.create(TYPES2_MODULE, "lf-my-string");
    private static final QName LF_INT8 = QName.create(TYPES2_MODULE, "lf-int8");
    private static final QName LF_INT16 = QName.create(TYPES2_MODULE, "lf-int16");
    private static final QName LF_INT32 = QName.create(TYPES2_MODULE, "lf-int32");
    private static final QName LF_INT64 = QName.create(TYPES2_MODULE, "lf-int64");
    private static final QName LF_UINT8 = QName.create(TYPES2_MODULE, "lf-uint8");
    private static final QName LF_UINT16 = QName.create(TYPES2_MODULE, "lf-uint16");
    private static final QName LF_UINT32 = QName.create(TYPES2_MODULE, "lf-uint32");
    private static final QName LF_UINT64 = QName.create(TYPES2_MODULE, "lf-uint64");
    private static final QName LF_BOOL = QName.create(TYPES2_MODULE, "lf-bool");

    @Test
    public void readAndParseYangFileTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(TYPEFILE1, TYPEFILE2, TYPEFILE3, TYPEFILE4);
        SchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final LeafSchemaNode lfDecimalNode = (LeafSchemaNode) result.getDataChildByName(LF_DECIMAL);
        assertNotNull(lfDecimalNode);

        assertTrue(lfDecimalNode.getType() instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition lfDecimalNodeType = (DecimalTypeDefinition) lfDecimalNode.getType();
        assertEquals(2, lfDecimalNodeType.getFractionDigits());

        final LeafSchemaNode lfInt8Node = (LeafSchemaNode) result.getDataChildByName(LF_INT8);
        assertNotNull(lfInt8Node);
        assertEquals(BaseTypes.int8Type().getClass(), lfInt8Node.getType().getClass());

        final LeafSchemaNode lfInt16Node = (LeafSchemaNode) result.getDataChildByName(LF_INT16);
        assertNotNull(lfInt16Node);
        assertEquals(BaseTypes.int16Type().getClass(), lfInt16Node.getType().getClass());

        final LeafSchemaNode lfInt32Node = (LeafSchemaNode) result.getDataChildByName(LF_INT32);
        assertNotNull(lfInt32Node);
        assertEquals(BaseTypes.int32Type().getClass(), lfInt32Node.getType().getClass());

        final LeafSchemaNode lfInt64Node = (LeafSchemaNode) result.getDataChildByName(LF_INT64);
        assertNotNull(lfInt64Node);
        assertEquals(BaseTypes.int64Type().getClass(), lfInt64Node.getType().getClass());

        final LeafSchemaNode lfUInt8Node = (LeafSchemaNode) result.getDataChildByName(LF_UINT8);
        assertNotNull(lfUInt8Node);
        assertEquals(BaseTypes.uint8Type().getClass(), lfUInt8Node.getType().getClass());

        final LeafSchemaNode lfUInt16Node = (LeafSchemaNode) result.getDataChildByName(LF_UINT16);
        assertNotNull(lfUInt16Node);
        assertEquals(BaseTypes.uint16Type().getClass(), lfUInt16Node.getType().getClass());

        final LeafSchemaNode lfUInt32Node = (LeafSchemaNode) result.getDataChildByName(LF_UINT32);
        assertNotNull(lfUInt32Node);
        assertEquals(BaseTypes.uint32Type().getClass(), lfUInt32Node.getType().getClass());

        final LeafSchemaNode lfUInt64Node = (LeafSchemaNode) result.getDataChildByName(LF_UINT64);
        assertNotNull(lfUInt64Node);
        assertEquals(BaseTypes.uint64Type().getClass(), lfUInt64Node.getType().getClass());

        final LeafSchemaNode lfBoolNode = (LeafSchemaNode) result.getDataChildByName(LF_BOOL);
        assertNotNull(lfBoolNode);
        assertEquals(BaseTypes.booleanType().getClass(), lfBoolNode.getType().getClass());
    }
}
