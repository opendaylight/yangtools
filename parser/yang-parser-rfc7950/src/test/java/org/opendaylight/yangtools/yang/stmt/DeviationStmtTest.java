/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class DeviationStmtTest {

    private static final StatementStreamSource FOO_MODULE = sourceForResource("/deviation-stmt-test/foo.yang");
    private static final StatementStreamSource FOO_IMP_MODULE = sourceForResource("/deviation-stmt-test/foo-imp.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource("/deviation-stmt-test/bar.yang");
    private static final StatementStreamSource BAR_IMP_MODULE = sourceForResource("/deviation-stmt-test/bar-imp.yang");

    @Test
    void testDeviationAndDeviate() throws ReactorException {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(FOO_MODULE, FOO_IMP_MODULE, BAR_MODULE, BAR_IMP_MODULE)
            .buildEffective();
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModule("foo", Revision.of("2016-06-23")).get();
        var deviations = testModule.getDeviations();
        assertEquals(4, deviations.size());

        for (Deviation deviation : deviations) {
            final var deviates = deviation.getDeviates();

            final String targetLocalName = Iterables.getLast(deviation.getTargetPath().getNodeIdentifiers())
                .getLocalName();
            if ("test-leaf".equals(targetLocalName)) {
                assertEquals(Optional.of("test-leaf is not supported"), deviation.getDescription());
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
                        assertInstanceOf(Uint32TypeDefinition.class, deviate.getDeviatedType());
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

        testModule = schemaContext.findModule("bar", Revision.of("2016-09-22")).get();
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
            final var deviates = deviation.getDeviates();
            final String targetLocalName = Iterables.getLast(deviation.getTargetPath().getNodeIdentifiers())
                .getLocalName();

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
                if (Optional.of("desc").equals(deviation.getDescription())) {
                    deviation4 = deviation;
                } else {
                    deviation5 = deviation;
                }
            }

            if ("bar-leaf-2".equals(targetLocalName)) {
                if (Optional.of("ref").equals(deviation.getReference())) {
                    deviation6 = deviation;
                } else {
                    deviation7 = deviation;
                }
            }
        }

        assertEquals(0, deviation1.getUnknownSchemaNodes().size());
        assertEquals(1,
            deviation1.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());

        assertEquals(deviation1, deviation1);
        assertNotEquals(null, deviation1);
        assertNotEquals("str", deviation1);

        DeviateDefinition deviate = deviation1.getDeviates().iterator().next();
        assertEquals(deviate, deviate);
        assertNotEquals(null, deviate);
        assertNotEquals("str", deviate);

        assertNotEquals(deviation1, deviation2);
        assertNotEquals(deviation2, deviation3);
        assertNotEquals(deviation4, deviation5);
        assertNotEquals(deviation6, deviation7);
    }
}
