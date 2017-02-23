/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.text.ParseException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class RpcStmtTest {

    private static final StatementStreamSource RPC_MODULE = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource FOO_MODULE = sourceForResource("/rpc-stmt-test/foo.yang");

    @Test
    public void rpcTest() throws ReactorException, ParseException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(RPC_MODULE, IMPORTED_MODULE, FOO_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        assertEquals(1, testModule.getRpcs().size());

        final RpcDefinition rpc = testModule.getRpcs().iterator().next();
        assertEquals("get-config", rpc.getQName().getLocalName());

        final ContainerSchemaNode input = rpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());

        final ContainerSchemaNode container = (ContainerSchemaNode) input.getDataChildByName(QName.create(testModule.getQNameModule(), "source"));
        assertNotNull(container);
        AnyXmlSchemaNode anyXml = (AnyXmlSchemaNode) input.getDataChildByName(QName.create(testModule.getQNameModule(), "filter"));
        assertNotNull(anyXml);

        final ContainerSchemaNode output = rpc.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());

        anyXml = (AnyXmlSchemaNode) output.getDataChildByName(QName.create(testModule.getQNameModule(), "data"));
        assertNotNull(anyXml);

        final Module fooModule = result.findModuleByName("foo", SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-23"));
        assertNotNull(fooModule);

        final Set<RpcDefinition> rpcs = fooModule.getRpcs();
        assertEquals(2, rpcs.size());

        RpcDefinition fooRpc1 = null;
        RpcDefinition fooRpc2 = null;

        for (RpcDefinition rpcDefinition : rpcs) {
            if ("foo-rpc-1".equals(rpcDefinition.getQName().getLocalName())) {
                fooRpc1 = rpcDefinition;
            } else if ("foo-rpc-2".equals(rpcDefinition.getQName().getLocalName())) {
                fooRpc2 = rpcDefinition;
            }
        }

        assertFalse(fooRpc1.equals(null));
        assertFalse(fooRpc1.equals("str"));
        assertFalse(fooRpc1.equals(fooRpc2));

        assertNotEquals(fooRpc1.getInput().hashCode(), fooRpc2.getInput().hashCode());
        assertNotEquals(fooRpc1.getOutput().hashCode(), fooRpc2.getOutput().hashCode());

        assertTrue(fooRpc1.getInput().equals(fooRpc1.getInput()));
        assertFalse(fooRpc1.getInput().equals(null));
        assertFalse(fooRpc1.getInput().equals("str"));
        assertFalse(fooRpc1.getInput().equals(fooRpc2.getInput()));

        assertTrue(fooRpc1.getOutput().equals(fooRpc1.getOutput()));
        assertFalse(fooRpc1.getOutput().equals(null));
        assertFalse(fooRpc1.getOutput().equals("str"));
        assertFalse(fooRpc1.getOutput().equals(fooRpc2.getOutput()));
    }
}
