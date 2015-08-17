package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.*;
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

}
