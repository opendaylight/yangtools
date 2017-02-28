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

import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class RpcStmtTest {

    private static final YangStatementSourceImpl RPC_MODULE = new YangStatementSourceImpl("/model/baz.yang", false);
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl("/model/bar.yang",
            false);

    @Test
    public void rpcTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, RPC_MODULE, IMPORTED_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        assertEquals(1, testModule.getRpcs().size());

        RpcDefinition rpc = testModule.getRpcs().iterator().next();
        assertEquals("get-config", rpc.getQName().getLocalName());

        ContainerSchemaNode input = rpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());

        ContainerSchemaNode container = (ContainerSchemaNode) input.getDataChildByName("source");
        assertNotNull(container);
        AnyXmlSchemaNode anyXml = (AnyXmlSchemaNode) input.getDataChildByName("filter");
        assertNotNull(anyXml);

        ContainerSchemaNode output = rpc.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());

        anyXml = (AnyXmlSchemaNode) output.getDataChildByName("data");
        assertNotNull(anyXml);
    }

    @Test
    public void testImplicitInputAndOutput() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rpc-stmt-test/bar.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-11-25");

        final Module barModule = schemaContext.findModuleByName("bar", revision);
        assertNotNull(barModule);

        final Set<RpcDefinition> rpcs = barModule.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition barRpc = rpcs.iterator().next();

        final ContainerSchemaNode input = barRpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) input).getDeclared().getStatementSource());

        final ContainerSchemaNode output = barRpc.getOutput();
        assertNotNull(output);
        assertEquals(2, output.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) output).getDeclared().getStatementSource());
    }
}
