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

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

public class DeviationStmtTest {
    @Test
    public void testDeviationAndDeviate() throws Exception {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(
                    sourceForResource("/deviation-stmt-test/foo.yang"),
                    sourceForResource("/deviation-stmt-test/foo-imp.yang"),
                    sourceForResource("/deviation-stmt-test/bar.yang"),
                    sourceForResource("/deviation-stmt-test/bar-imp.yang"))
                .buildEffective();
        assertNotNull(schemaContext);

        var testModule = schemaContext.findModule("foo", Revision.of("2016-06-23")).orElseThrow();
        var deviations = testModule.getDeviations();
        assertEquals(4, deviations.size());

        for (var deviation : deviations) {
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
                        assertTrue(deviate.getDeviatedType() instanceof Uint32TypeDefinition);
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

        testModule = schemaContext.findModule("bar", Revision.of("2016-09-22")).orElseThrow();
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

        for (var deviation : deviations) {
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

        assertTrue(deviation1.equals(deviation1));
        assertFalse(deviation1.equals(null));
        assertFalse(deviation1.equals("str"));

        var deviate = deviation1.getDeviates().iterator().next();
        assertTrue(deviate.equals(deviate));
        assertFalse(deviate.equals(null));
        assertFalse(deviate.equals("str"));

        assertFalse(deviation1.equals(deviation2));
        assertFalse(deviation2.equals(deviation3));
        assertFalse(deviation4.equals(deviation5));
        assertFalse(deviation6.equals(deviation7));
    }
}
