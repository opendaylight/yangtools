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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class MoreRevisionsTest {

    private static final YangStatementSourceImpl REVFILE = new YangStatementSourceImpl(
            "/semantic-statement-parser/revisions/more-revisions-test.yang",
            false);

    private static final YangStatementSourceImpl TED_20130712 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/ted@2013-07-12.yang",
            false);

    private static final YangStatementSourceImpl TED_20131021 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/ted@2013-10-21.yang",
            false);

    private static final YangStatementSourceImpl IETF_TYPES = new YangStatementSourceImpl(
            "/ietf/ietf-inet-types@2010-09-24.yang", false);

    private static final YangStatementSourceImpl NETWORK_TOPOLOGY_20130712 = new YangStatementSourceImpl(
            "/ietf/network-topology@2013-07-12.yang", false);

    private static final YangStatementSourceImpl NETWORK_TOPOLOGY_20131021 = new YangStatementSourceImpl(
            "/ietf/network-topology@2013-10-21.yang", false);

    private static final YangStatementSourceImpl ISIS_20130712 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/isis-topology@2013-07-12.yang",
            false);

    private static final YangStatementSourceImpl ISIS_20131021 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/isis-topology@2013-10-21.yang",
            false);

    private static final YangStatementSourceImpl L3_20130712 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/l3-unicast-igp-topology@2013-07-12.yang",
            false);

    private static final YangStatementSourceImpl L3_20131021 = new YangStatementSourceImpl(
            "/semantic-statement-parser/two-revisions/l3-unicast-igp-topology@2013-10-21.yang",
            false);

    @Test
    public void readAndParseYangFileTest() throws SourceException,
            ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSource(REVFILE);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
        final Module moduleByName = result.getModules().iterator().next();
        final QNameModule qNameModule = moduleByName.getQNameModule();
        final String formattedRevision = qNameModule.getFormattedRevision();
        assertEquals(formattedRevision, "2015-06-07");
    }

    @Test
    public void twoRevisionsTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        reactor.addSources(TED_20130712, TED_20131021, IETF_TYPES);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

    }

    @Test
    public void twoRevisionsTest2() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        reactor.addSources(NETWORK_TOPOLOGY_20130712,
                NETWORK_TOPOLOGY_20131021, IETF_TYPES);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
        Set<Module> modules = result.getModules();

        assertEquals(3, modules.size());
        assertEquals(2, StmtTestUtils.findModules(modules, "network-topology")
                .size());
    }

    @Test
    public void moreRevisionsListKeyTest() throws SourceException,
            ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        reactor.addSources(TED_20130712, TED_20131021, ISIS_20130712,
                ISIS_20131021, L3_20130712, L3_20131021, IETF_TYPES,
                NETWORK_TOPOLOGY_20130712, NETWORK_TOPOLOGY_20131021);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void multipleRevisionsTest() throws SourceException,
            ReactorException, FileNotFoundException, URISyntaxException {
        for (int i = 0; i < 25; i++) {
            SchemaContext context = StmtTestUtils
                    .parseYangSources("/semantic-statement-parser/multiple-revisions");
            assertNotNull(context);
        }
    }

}
