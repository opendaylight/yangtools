/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.amend.parser;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.amend.model.api.AmendConstants;
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

public class AmendTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.defaultReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AmendStatementSupport.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testResolution() throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction build = reactor.newBuild();
        build.addLibSources(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/odl-amend.yang")));
        build.addSources(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/amend-foo.yang")),
            YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")));
        final SchemaContext context = build.buildEffective();

        final Module module = context.findModule(AmendConstants.ORIGINAL_MODULE).get();
//        final DataSchemaNode nacm = module.findDataChildByName(QName.create(AmendConstants.ORIGINAL_MODULE, "nacm"))
//                .get();
//        assertTrue(DefaultDenyAllSchemaNode.findIn(nacm).isPresent());
//        assertFalse(DefaultDenyWriteSchemaNode.findIn(nacm).isPresent());
    }
}
