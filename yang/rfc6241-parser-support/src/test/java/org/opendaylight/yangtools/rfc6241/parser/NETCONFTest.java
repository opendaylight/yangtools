/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.NETCONFConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class NETCONFTest {
    private static final QName FILTER = QName.create(NETCONFConstants.RFC6241_MODULE, "filter");

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
        final SchemaContext context = reactor.newBuild()
                .addLibSources(YangStatementStreamSource.create(
                    YangTextSchemaSource.forResource("/ietf-inet-types@2013-07-15.yang")))
                .addSource(YangStatementStreamSource.create(
                    YangTextSchemaSource.forResource("/ietf-netconf@2011-06-01.yang")))
                .buildEffective();

        final Module module = context.findModule(NETCONFConstants.RFC6241_MODULE).get();
        final Iterator<RpcDefinition> it = module.getRpcs().iterator();
        assertExtension(false, it.next());
        assertExtension(false, it.next());
        assertExtension(true, it.next());
        assertExtension(true, it.next());
        it.forEachRemaining(def -> assertExtension(false, def));
    }

    private static void assertExtension(final boolean expected, final RpcDefinition def) {
        final Optional<DataSchemaNode> optFilter = def.getInput().findDataTreeChild(FILTER);
        assertEquals(expected, optFilter.isPresent());
        optFilter.ifPresent(filter -> {
            assertThat(filter, is(instanceOf(AnyXmlSchemaNode.class)));
            assertTrue(GetFilterElementAttributesSchemaNode.findIn((AnyXmlSchemaNode) filter).isPresent());
        });
    }
}
