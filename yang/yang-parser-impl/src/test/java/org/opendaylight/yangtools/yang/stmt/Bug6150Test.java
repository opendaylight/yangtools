/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class AugmentArgumentParsingTest {

    private static final YangStatementSourceImpl TARGET = new YangStatementSourceImpl(
            "/semantic-statement-parser/bug6150/target.yang", false);
    private static YangStatementSourceImpl AUGMENT_FIRST = new YangStatementSourceImpl(
            "/semantic-statement-parser/bug6150/aug-first.yang", false);
    private static YangStatementSourceImpl AUGMENT_SECOND = new YangStatementSourceImpl(
            "/semantic-statement-parser/bug6150/aug-second.yang", false);

    private static void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

    @Test
    public void effectiveAugmentFirstTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TARGET, AUGMENT_FIRST);

        final EffectiveModelContext result = reactor.Effectivebuild();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentSecondTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TARGET, AUGMENT_SECOND);

        final EffectiveModelContext result = reactor.Effectivebuild();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentBothTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TARGET, AUGMENT_FIRST, AUGMENT_SECOND);

        final EffectiveModelContext result = reactor.Effectivebuild();
        assertNotNull(result);
    }
}
