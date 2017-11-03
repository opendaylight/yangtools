/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

public class NACMTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.defaultReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyAllStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyWriteStatementSupport.getInstance())
                .build();
    }

    @Test
    public void testResolution() throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction build = reactor.newBuild();
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/ietf-netconf-acm@2012-02-22.yang")));
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/ietf-yang-types@2013-07-15.yang")));
        final SchemaContext context = build.buildEffective();

        final Module module = context.findModule(NACMConstants.RFC6536_MODULE).get();
        final DataSchemaNode nacm = module.findDataChildByName(QName.create(NACMConstants.RFC6536_MODULE, "nacm"))
                .get();
        assertTrue(DefaultDenyAllSchemaNode.findIn(nacm).isPresent());
        assertFalse(DefaultDenyWriteSchemaNode.findIn(nacm).isPresent());
    }
}
