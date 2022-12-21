/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;

class RpcStmtTest extends AbstractYangTest {
    @Test
    void rpcTest() {
        final var result = assertEffectiveModel("/model/baz.yang", "/model/bar.yang", "/rpc-stmt-test/foo.yang");

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

        assertNotEquals(null, fooRpc1);
        assertNotEquals("str", fooRpc1);
        assertNotEquals(fooRpc1, fooRpc2);

        assertNotEquals(fooRpc1.getInput().hashCode(), fooRpc2.getInput().hashCode());
        assertNotEquals(fooRpc1.getOutput().hashCode(), fooRpc2.getOutput().hashCode());

        assertEquals(fooRpc1.getInput(), fooRpc1.getInput());
        assertNotEquals(null, fooRpc1.getInput());
        assertNotEquals("str", fooRpc1.getInput());
        assertNotEquals(fooRpc1.getInput(), fooRpc2.getInput());

        assertEquals(fooRpc1.getOutput(), fooRpc1.getOutput());
        assertNotEquals(null, fooRpc1.getOutput());
        assertNotEquals("str", fooRpc1.getOutput());
        assertNotEquals(fooRpc1.getOutput(), fooRpc2.getOutput());
    }

    @Test
    void testImplicitInputAndOutput() {
        final var context = assertEffectiveModel("/rpc-stmt-test/bar.yang");

        final Module barModule = context.findModule("bar", Revision.of("2016-11-25")).get();
        final Collection<? extends RpcDefinition> rpcs = barModule.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition barRpc = rpcs.iterator().next();

        final InputSchemaNode input = barRpc.getInput();
        assertNotNull(input);
        assertEquals(2, input.getChildNodes().size());
        assertEquals(StatementOrigin.CONTEXT, ((EffectiveStatement<?, ?>) input).statementOrigin());

        final OutputSchemaNode output = barRpc.getOutput();
        assertNotNull(output);
        assertEquals(2, output.getChildNodes().size());
        assertEquals(StatementOrigin.CONTEXT, ((EffectiveStatement<?, ?>) output).statementOrigin());
    }
}
