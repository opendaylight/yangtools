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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class DeviationStmtTest {

    private static final StatementStreamSource FOO_MODULE = sourceForResource("/deviation-stmt-test/foo.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource("/deviation-stmt-test/bar.yang");

    @Test
    public void testDeviationAndDeviate() throws ReactorException, ParseException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(FOO_MODULE, BAR_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-06-23");

        Module testModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(testModule);

        Set<Deviation> deviations = testModule.getDeviations();
        assertEquals(4, deviations.size());

        for (Deviation deviation : deviations) {
            final List<DeviateDefinition> deviates = deviation.getDeviates();
            final String targetLocalName = deviation.getTargetPath().getLastComponent().getLocalName();
            if ("test-leaf".equals(targetLocalName)) {
                assertEquals("test-leaf is not supported", deviation.getDescription());
                assertEquals(1, deviates.size());
                assertEquals(DeviateKind.NOT_SUPPORTED, deviates.iterator().next().getDeviateType());
            } else if ("test-leaf-2".equals(targetLocalName)) {
                assertEquals(1, deviates.size());
                assertEquals(DeviateKind.ADD, deviates.iterator().next().getDeviateType());
                assertEquals("added-def-val", deviates.iterator().next().getDeviatedDefault());
                assertFalse(deviates.iterator().next().getDeviatedConfig());
                assertTrue(deviates.iterator().next().getDeviatedMandatory());
            } else if ("test-leaf-list".equals(targetLocalName)) {
                assertEquals(3, deviates.size());
                for (DeviateDefinition deviate : deviates) {
                    if (DeviateKind.ADD.equals(deviate.getDeviateType())) {
                        assertEquals(12, deviate.getDeviatedMaxElements().intValue());
                    } else if (DeviateKind.REPLACE.equals(deviate.getDeviateType())) {
                        assertEquals(5, deviate.getDeviatedMinElements().intValue());
                        assertTrue(deviate.getDeviatedType() instanceof UnsignedIntegerTypeDefinition);
                    } else {
                        assertEquals(2, deviate.getDeviatedMusts().size());
                        assertEquals("minutes", deviate.getDeviatedUnits());
                    }
                }
            } else {
                assertEquals(1, deviation.getDeviates().size());
                assertEquals(DeviateKind.DELETE, deviates.iterator().next().getDeviateType());
                assertEquals(2, deviates.iterator().next().getDeviatedUniques().size());
            }
        }

        revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-22");
        testModule = schemaContext.findModuleByName("bar", revision);
        assertNotNull(testModule);

        deviations = testModule.getDeviations();
        assertEquals(7, deviations.size());

        Deviation deviation1 = null;
        Deviation deviation2 = null;
        Deviation deviation3 = null;
        Deviation deviation4 = null;
        Deviation deviation5 = null;
        Deviation deviation6 = null;
        Deviation deviation7 = null;

        for (Deviation deviation : deviations) {
            final List<DeviateDefinition> deviates = deviation.getDeviates();
            final String targetLocalName = deviation.getTargetPath().getLastComponent().getLocalName();

            if ("bar-container-1".equals(targetLocalName)) {
                deviation1 = deviation;
            }

            if ("bar-container-2".equals(targetLocalName)) {
                DeviateKind deviateKind = deviates.iterator().next().getDeviateType();
                if (deviateKind.equals(DeviateKind.DELETE)) {
                    deviation2 = deviation;
                } else if (deviateKind.equals(DeviateKind.ADD)) {
                    deviation3 = deviation;
                }
            }

            if ("bar-leaf-1".equals(targetLocalName)) {
                if ("desc".equals(deviation.getDescription())) {
                    deviation4 = deviation;
                } else {
                    deviation5 = deviation;
                }
            }

            if ("bar-leaf-2".equals(targetLocalName)) {
                if ("ref".equals(deviation.getReference())) {
                    deviation6 = deviation;
                } else {
                    deviation7 = deviation;
                }
            }
        }

        assertEquals(1, deviation1.getUnknownSchemaNodes().size());
        assertTrue(deviation1.equals(deviation1));
        assertFalse(deviation1.equals(null));
        assertFalse(deviation1.equals("str"));

        DeviateDefinition deviate = deviation1.getDeviates().iterator().next();
        assertTrue(deviate.equals(deviate));
        assertFalse(deviate.equals(null));
        assertFalse(deviate.equals("str"));

        assertFalse(deviation1.equals(deviation2));
        assertFalse(deviation2.equals(deviation3));
        assertFalse(deviation4.equals(deviation5));
        assertFalse(deviation6.equals(deviation7));
    }
}
