/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileStmtTest {

    private static final StatementStreamSource YIN_FILE = createSource("test.yin");
    private static final StatementStreamSource EXT_FILE = createSource("extension.yin");
    private static final StatementStreamSource EXT_USE_FILE = createSource("extension-use.yin");
    private static final StatementStreamSource INVALID_YIN_FILE = createSource("incorrect-foo.yin");
    private static final StatementStreamSource INVALID_YIN_FILE_2 = createSource("incorrect-bar.yin");

    private Set<Module> modules;

    private static StatementStreamSource createSource(final String name) {
        return new YinStatementSourceImpl("/semantic-statement-parser/yin/" + name, false);
    }

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
    }

    @Test
    public void readAndParseYinFileTestModel() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, YIN_FILE, EXT_FILE, EXT_USE_FILE);
        SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    // parsing yin file whose import statement references a module which does not exist
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void readAndParseInvalidYinFileTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_YIN_FILE);
        SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    // parsing yin file with duplicate key name in a list statement
    public void readAndParseInvalidYinFileTest2() {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_YIN_FILE_2);

        try {
            reactor.buildEffective();
            fail("Reactor exception should have been thrown");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Key argument 'testing-string testing-string' contains duplicates"));
        }
    }

    @Test
    public void testModulesSize() {
        assertEquals(modules.size(), 9);
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor,  final
    StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }

}