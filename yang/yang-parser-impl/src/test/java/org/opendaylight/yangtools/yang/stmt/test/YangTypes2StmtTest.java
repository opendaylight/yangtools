/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.opendaylight.yangtools.yang.model.util.ExtendedType;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.util.BooleanType;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.Int8;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.opendaylight.yangtools.yang.model.util.Uint8;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class YangTypes2StmtTest {

    private static final YangStatementSourceImpl TYPEFILE1 = new YangStatementSourceImpl(
            "/semantic-statement-parser/types2.yang", false);
    private static final YangStatementSourceImpl TYPEFILE2 = new YangStatementSourceImpl(
            "/semantic-statement-parser/types.yang", false);
    private static final YangStatementSourceImpl TYPEFILE3 = new YangStatementSourceImpl(
            "/semantic-statement-parser/simple-types.yang", false);
    private static final YangStatementSourceImpl TYPEFILE4 = new YangStatementSourceImpl(
            "/semantic-statement-parser/identityreftest.yang", false);

    private static final QNameModule types2Module = QNameModule.create(URI.create("types2"),
            SimpleDateFormatUtil.DEFAULT_DATE_REV);

    private static final QName lfDecimal = QName.create(types2Module, "lf-decimal");
    private static final QName lfMyString = QName.create(types2Module, "lf-my-string");
    private static final QName lfInt8 = QName.create(types2Module, "lf-int8");
    private static final QName lfInt16 = QName.create(types2Module, "lf-int16");
    private static final QName lfInt32 = QName.create(types2Module, "lf-int32");
    private static final QName lfInt64 = QName.create(types2Module, "lf-int64");
    private static final QName lfUInt8 = QName.create(types2Module, "lf-uint8");
    private static final QName lfUInt16 = QName.create(types2Module, "lf-uint16");
    private static final QName lfUInt32 = QName.create(types2Module, "lf-uint32");
    private static final QName lfUInt64 = QName.create(types2Module, "lf-uint64");
    private static final QName lfBool = QName.create(types2Module, "lf-bool");

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TYPEFILE1, TYPEFILE2, TYPEFILE3, TYPEFILE4);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final LeafSchemaNode lfDecimalNode = (LeafSchemaNode) result.getDataChildByName(lfDecimal);
        assertNotNull(lfDecimalNode);
        final ExtendedType lfDecimalNodeType = (ExtendedType) lfDecimalNode.getType();
        assertEquals(ExtendedType.class, lfDecimalNodeType.getClass());
        assertEquals(2, lfDecimalNodeType.getFractionDigits().intValue());

        final LeafSchemaNode lfInt8Node = (LeafSchemaNode) result.getDataChildByName(lfInt8);
        assertNotNull(lfInt8Node);
        assertEquals(Int8.class, lfInt8Node.getType().getClass());

        final LeafSchemaNode lfInt16Node = (LeafSchemaNode) result.getDataChildByName(lfInt16);
        assertNotNull(lfInt16Node);
        assertEquals(Int16.class, lfInt16Node.getType().getClass());

        final LeafSchemaNode lfInt32Node = (LeafSchemaNode) result.getDataChildByName(lfInt32);
        assertNotNull(lfInt32Node);
        assertEquals(Int32.class, lfInt32Node.getType().getClass());

        final LeafSchemaNode lfInt64Node = (LeafSchemaNode) result.getDataChildByName(lfInt64);
        assertNotNull(lfInt64Node);
        assertEquals(Int64.class, lfInt64Node.getType().getClass());

        final LeafSchemaNode lfUInt8Node = (LeafSchemaNode) result.getDataChildByName(lfUInt8);
        assertNotNull(lfUInt8Node);
        assertEquals(Uint8.class, lfUInt8Node.getType().getClass());

        final LeafSchemaNode lfUInt16Node = (LeafSchemaNode) result.getDataChildByName(lfUInt16);
        assertNotNull(lfUInt16Node);
        assertEquals(Uint16.class, lfUInt16Node.getType().getClass());

        final LeafSchemaNode lfUInt32Node = (LeafSchemaNode) result.getDataChildByName(lfUInt32);
        assertNotNull(lfUInt32Node);
        assertEquals(Uint32.class, lfUInt32Node.getType().getClass());

        final LeafSchemaNode lfUInt64Node = (LeafSchemaNode) result.getDataChildByName(lfUInt64);
        assertNotNull(lfUInt64Node);
        assertEquals(Uint64.class, lfUInt64Node.getType().getClass());

        final LeafSchemaNode lfBoolNode = (LeafSchemaNode) result.getDataChildByName(lfBool);
        assertNotNull(lfBoolNode);
        assertEquals(BooleanType.class, lfBoolNode.getType().getClass());
    }

    private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
