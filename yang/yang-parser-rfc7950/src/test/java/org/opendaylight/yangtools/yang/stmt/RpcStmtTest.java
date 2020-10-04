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

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class RpcStmtTest {

    @Test
    public void rpcTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/model/baz.yang"))
                .addSource(sourceForResource("/model/bar.yang"))
                .addSource(sourceForResource("/rpc-stmt-test/foo.yang"))
                .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("baz").iterator().next();
        assertEquals(1, testModule.getRpcs().size());

        final RpcDefinition rpc = testModule.getRpcs().iterator().next();
        assertEquals("get-config", rpc.getQName().getLocalName());

        final InputSchemaNode input = rpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());

        final ContainerSchemaNode container = (ContainerSchemaNode) input.getDataChildByName(
            QName.create(testModule.getQNameModule(), "source"));
        assertNotNull(container);
        AnyxmlSchemaNode anyXml = (AnyxmlSchemaNode) input.getDataChildByName(
            QName.create(testModule.getQNameModule(), "filter"));
        assertNotNull(anyXml);

        final OutputSchemaNode output = rpc.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());

        anyXml = (AnyxmlSchemaNode) output.getDataChildByName(QName.create(testModule.getQNameModule(), "data"));
        assertNotNull(anyXml);

        final Module fooModule = result.findModule("foo", Revision.of("2016-09-23")).get();
        final Collection<? extends RpcDefinition> rpcs = fooModule.getRpcs();
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

    @Test
    public void testImplicitInputAndOutput() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rpc-stmt-test/bar.yang");
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2016-11-25")).get();
        final Collection<? extends RpcDefinition> rpcs = barModule.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition barRpc = rpcs.iterator().next();

        final InputSchemaNode input = barRpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) input).getStatementSource());

        final OutputSchemaNode output = barRpc.getOutput();
        assertNotNull(output);
        assertEquals(2, output.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) output).getStatementSource());
    }
}
