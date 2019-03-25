/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.NETCONFConstants;
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

public class NETCONFTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.defaultReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    GetFilterElementAttributesStatementSupport.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testResolution() throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction build = reactor.newBuild();
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/ietf-netconf-acm@2012-02-22.yang")));
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/ietf-yang-types@2013-07-15.yang")));
        final SchemaContext context = build.buildEffective();

        final Module module = context.findModule(NETCONFConstants.RFC6241_MODULE).get();
        final DataSchemaNode nacm = module.findDataChildByName(QName.create(NETCONFConstants.RFC6241_MODULE, "nacm"))
                .get();
        assertTrue(GetFilterElementAttributesSchemaNode.findIn(nacm).isPresent());
        assertTrue(GetFilterElementAttributesSchemaNode.findIn(nacm).isPresent());
    }
}
