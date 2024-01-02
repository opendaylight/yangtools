/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;

public class YangTextSchemaContextResolverTest {
    @Test
    public void testYangTextSchemaContextResolver() throws Exception {
        final var yangTextSchemaContextResolver = YangTextSchemaContextResolver.create("test-bundle");
        assertNotNull(yangTextSchemaContextResolver);

        final var yangFile1 = getClass().getResource("/yang-text-schema-context-resolver-test/foo.yang");
        assertNotNull(yangFile1);
        final var yangFile2 = getClass().getResource("/yang-text-schema-context-resolver-test/bar.yang");
        assertNotNull(yangFile2);
        final var yangFile3 = getClass().getResource("/yang-text-schema-context-resolver-test/baz.yang");
        assertNotNull(yangFile2);

        final var registration1 = yangTextSchemaContextResolver.registerSource(yangFile1);
        assertNotNull(registration1);
        final var registration2 = yangTextSchemaContextResolver.registerSource(yangFile2);
        assertNotNull(registration2);
        final var registration3 = yangTextSchemaContextResolver.registerSource(yangFile3);
        assertNotNull(registration3);

        assertEquals(3, yangTextSchemaContextResolver.getAvailableSources().size());

        final var fooModuleId = new SourceIdentifier("foo", "2016-09-26");
        final var foo = yangTextSchemaContextResolver.getSource(fooModuleId);
        assertTrue(foo.isDone());
        assertEquals(fooModuleId, foo.get().sourceId());

        final var barModuleId = new SourceIdentifier("bar", "2016-09-26");
        final var bar = yangTextSchemaContextResolver.getSource(barModuleId);
        assertTrue(bar.isDone());
        assertEquals(barModuleId, bar.get().sourceId());

        final var bazModuleId = new SourceIdentifier("baz", "2016-09-26");
        final var baz = yangTextSchemaContextResolver.getSource(bazModuleId);
        assertTrue(baz.isDone());
        assertEquals(bazModuleId, baz.get().sourceId());

        final var foobarModuleId = new SourceIdentifier("foobar", "2016-09-26");
        final var foobar = yangTextSchemaContextResolver.getSource(foobarModuleId);
        assertTrue(foobar.isDone());

        final var cause = assertInstanceOf(MissingSchemaSourceException.class,
            assertThrows(ExecutionException.class, foobar::get).getCause());
        assertEquals("URL for SourceIdentifier [foobar@2016-09-26] not registered", cause.getMessage());

        var schemaContextOptional = yangTextSchemaContextResolver.getEffectiveModelContext();
        assertTrue(schemaContextOptional.isPresent());
        var schemaContext = schemaContextOptional.orElseThrow();
        assertEquals(3, schemaContext.getModules().size());

        registration1.close();
        registration2.close();
        registration3.close();

        assertEquals(0, yangTextSchemaContextResolver.getAvailableSources().size());

        schemaContextOptional = yangTextSchemaContextResolver.getEffectiveModelContext();
        assertTrue(schemaContextOptional.isPresent());
        schemaContext = schemaContextOptional.orElseThrow();
        assertEquals(0, schemaContext.getModules().size());
    }

    @Test
    public void testFeatureRegistration() throws Exception {
        final var yangTextSchemaContextResolver = YangTextSchemaContextResolver.create("feature-test-bundle");
        assertNotNull(yangTextSchemaContextResolver);
        final var yangFile1 = getClass().getResource("/yang-text-schema-context-resolver-test/foo-feature.yang");
        assertNotNull(yangFile1);
        final var yangFile2 = getClass().getResource("/yang-text-schema-context-resolver-test/aux-feature.yang");
        assertNotNull(yangFile2);

        final var registration1 = yangTextSchemaContextResolver.registerSource(yangFile1);
        assertNotNull(registration1);
        final var registration2 = yangTextSchemaContextResolver.registerSource(yangFile2);
        assertNotNull(registration2);

        final QName cont = QName.create("foo-feature-namespace", "2016-09-26", "bar-feature-container");
        final QName condLeaf = QName.create("foo-feature-namespace", "2016-09-26", "conditional-leaf");
        final QName uncondLeaf = QName.create("foo-feature-namespace", "2016-09-26", "unconditional-leaf");
        final QName auxCont = QName.create("aux-feature-namespace", "2016-09-26", "aux-cond-cont");

        final QName usedFeature = QName.create("foo-feature-namespace", "2016-09-26", "used-feature");
        final QName unusedFeature = QName.create("foo-feature-namespace", "2016-09-26", "unused-feature");

        final var pathToConditional = List.of(cont, condLeaf);
        final var pathToUnconditional = List.of(cont, uncondLeaf);
        final var pathToAuxiliary = List.of(auxCont);

        final var context1 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context1, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context1, uncondLeaf.getModule(), pathToUnconditional));
        assertTrue(isModulePresent(context1, auxCont.getModule(), pathToAuxiliary));

        final var featRegistration1 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(unusedFeature.getLocalName()));
        final var context2 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertFalse(isModulePresent(context2, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context2, uncondLeaf.getModule(), pathToUnconditional));
        assertTrue(isModulePresent(context2, auxCont.getModule(), pathToAuxiliary));

        final var featRegistration2 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(usedFeature.getLocalName()));
        final var context3 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context3, condLeaf.getModule(), pathToConditional));

        final var featRegistration3 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(usedFeature.getLocalName(), unusedFeature.getLocalName()));
        featRegistration1.close();
        featRegistration2.close();
        final var context4 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context4, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context4, auxCont.getModule(), pathToAuxiliary));

        featRegistration3.close();
        final var featRegistration4 = yangTextSchemaContextResolver.registerSupportedFeatures(
                auxCont.getModule(), Set.of());
        final var context5 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context5, condLeaf.getModule(), pathToConditional));
        assertFalse(isModulePresent(context5, auxCont.getModule(), pathToAuxiliary));

        featRegistration4.close();
        final var context6 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context6, auxCont.getModule(), pathToAuxiliary));
    }

    private static boolean isModulePresent(final EffectiveModelContext context, final QNameModule qnameModule,
            final List<QName> path) {
        for (var module : context.getModules()) {
            if (module.getQNameModule().equals(qnameModule)) {
                return module.findDataTreeChild(path).isPresent();
            }
        }
        throw new AssertionError("No module with given QNameModule present in the context.");
    }
}
