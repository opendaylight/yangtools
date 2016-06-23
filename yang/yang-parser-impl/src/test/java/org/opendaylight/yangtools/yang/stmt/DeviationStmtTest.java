/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class DeviationStmtTest {

    private static final StatementStreamSource FOO_MODULE =
            new YangStatementSourceImpl("/deviation-stmt-test/foo.yang", false);

    @Test
    public void testDeviationAndDeviate() throws ReactorException, ParseException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(FOO_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-06-23");

        final Module testModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(testModule);

        final Set<Deviation> deviations = testModule.getDeviations();
        assertEquals(4, deviations.size());

        for (Deviation deviation : deviations) {
            final Set<DeviateDefinition> deviates = deviation.getDeviates();
            final String targetLocalName = deviation.getTargetPath().getLastComponent().getLocalName();
            if ("test-leaf".equals(targetLocalName)) {
                assertEquals("test-leaf is not supported", deviation.getDescription());
                assertEquals(1, deviates.size());
                assertEquals(DeviateDefinition.Deviate.NOT_SUPPORTED, deviates.iterator().next().getDeviateType());
            } else if ("test-leaf-2".equals(targetLocalName)) {
                assertEquals(1, deviates.size());
                assertEquals(DeviateDefinition.Deviate.ADD, deviates.iterator().next().getDeviateType());
                assertEquals("added-def-val", deviates.iterator().next().getDeviatedDefault());
                assertFalse(deviates.iterator().next().getDeviatedConfig());
            } else if ("test-leaf-list".equals(targetLocalName)) {
                assertEquals(3, deviates.size());
                for (DeviateDefinition deviate : deviates) {
                    if (DeviateDefinition.Deviate.ADD.equals(deviate.getDeviateType())) {
                        assertEquals(12, deviate.getDeviatedMaxElements().intValue());
                        assertTrue(deviate.getDeviatedMandatory());
                    } else if (DeviateDefinition.Deviate.REPLACE.equals(deviate.getDeviateType())) {
                        assertEquals(5, deviate.getDeviatedMinElements().intValue());
                        assertTrue(deviate.getDeviatedType() instanceof UnsignedIntegerTypeDefinition);
                    } else {
                        assertEquals(2, deviate.getDeviatedMusts().size());
                        assertEquals("minutes", deviate.getDeviatedUnits());
                    }
                }
            } else {
                assertEquals(1, deviation.getDeviates().size());
                assertEquals(DeviateDefinition.Deviate.DELETE, deviates.iterator().next().getDeviateType());
                assertEquals(2, deviates.iterator().next().getDeviatedUniques().size());
            }
        }
    }
}
