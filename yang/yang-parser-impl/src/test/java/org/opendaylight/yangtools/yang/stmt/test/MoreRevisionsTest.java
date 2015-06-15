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

    private static final YangStatementSourceImpl REVFILE = new YangStatementSourceImpl
            ("/semantic-statement-parser/revisions/more-revisions-test.yang",false);

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(REVFILE);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
        final Module moduleByName = result.getModules().iterator().next();
        final QNameModule qNameModule = moduleByName.getQNameModule();
        final String formattedRevision = qNameModule.getFormattedRevision();
        assertEquals(formattedRevision, "2015-06-07");
    }
}
