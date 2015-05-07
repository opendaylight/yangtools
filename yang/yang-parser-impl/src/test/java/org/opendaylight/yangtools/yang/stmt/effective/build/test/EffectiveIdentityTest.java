package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertNotNull;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.stmt.test.StmtTestUtils;
import org.junit.Test;

public class EffectiveIdentityTest {

    private static final YangStatementSourceImpl IDENTITY_TEST = new YangStatementSourceImpl(
            "/stmt-test/identity/identity-test.yang");

    @Test
    public void refineTest() throws SourceException, ReactorException,
            URISyntaxException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        StmtTestUtils.addSources(reactor, IDENTITY_TEST);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);
    }

}
