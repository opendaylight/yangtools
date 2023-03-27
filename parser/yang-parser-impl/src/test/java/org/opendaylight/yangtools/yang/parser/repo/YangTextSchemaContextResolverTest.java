/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

public class YangTextSchemaContextResolverTest {

    @Test
    public void testYangTextSchemaContextResolver() throws SchemaSourceException, IOException, YangSyntaxErrorException,
            InterruptedException, ExecutionException {
        final YangTextSchemaContextResolver yangTextSchemaContextResolver =
                YangTextSchemaContextResolver.create("test-bundle");
        assertNotNull(yangTextSchemaContextResolver);

        final URL yangFile1 = getClass().getResource("/yang-text-schema-context-resolver-test/foo.yang");
        assertNotNull(yangFile1);
        final URL yangFile2 = getClass().getResource("/yang-text-schema-context-resolver-test/bar.yang");
        assertNotNull(yangFile2);
        final URL yangFile3 = getClass().getResource("/yang-text-schema-context-resolver-test/baz.yang");
        assertNotNull(yangFile2);

        final YangTextSchemaSourceRegistration registration1 =
                yangTextSchemaContextResolver.registerSource(yangFile1);
        assertNotNull(registration1);
        final YangTextSchemaSourceRegistration registration2 =
                yangTextSchemaContextResolver.registerSource(yangFile2);
        assertNotNull(registration2);
        final YangTextSchemaSourceRegistration registration3 =
                yangTextSchemaContextResolver.registerSource(yangFile3);
        assertNotNull(registration3);

        assertEquals(3, yangTextSchemaContextResolver.getAvailableSources().size());

        final SourceIdentifier fooModuleId = new SourceIdentifier("foo", "2016-09-26");
        final ListenableFuture<YangTextSchemaSource> foo = yangTextSchemaContextResolver.getSource(fooModuleId);
        assertTrue(foo.isDone());
        assertEquals(fooModuleId, foo.get().getIdentifier());

        final SourceIdentifier barModuleId = new SourceIdentifier("bar", "2016-09-26");
        final ListenableFuture<YangTextSchemaSource> bar = yangTextSchemaContextResolver.getSource(barModuleId);
        assertTrue(bar.isDone());
        assertEquals(barModuleId, bar.get().getIdentifier());

        final SourceIdentifier bazModuleId = new SourceIdentifier("baz", "2016-09-26");
        final ListenableFuture<YangTextSchemaSource> baz =
                yangTextSchemaContextResolver.getSource(bazModuleId);
        assertTrue(baz.isDone());
        assertEquals(bazModuleId, baz.get().getIdentifier());

        final SourceIdentifier foobarModuleId = new SourceIdentifier("foobar", "2016-09-26");
        final ListenableFuture<YangTextSchemaSource> foobar = yangTextSchemaContextResolver.getSource(foobarModuleId);
        assertTrue(foobar.isDone());

        final Throwable cause = assertThrows(ExecutionException.class, foobar::get).getCause();
        assertThat(cause, instanceOf(MissingSchemaSourceException.class));
        assertEquals("URL for SourceIdentifier [foobar@2016-09-26] not registered", cause.getMessage());

        Optional<? extends SchemaContext> schemaContextOptional =
            yangTextSchemaContextResolver.getEffectiveModelContext();
        assertTrue(schemaContextOptional.isPresent());
        SchemaContext schemaContext = schemaContextOptional.orElseThrow();
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
    public void testFeatureRegistration()
            throws YangSyntaxErrorException, SchemaSourceException, IOException {
        final YangTextSchemaContextResolver yangTextSchemaContextResolver =
                YangTextSchemaContextResolver.create("feature-test-bundle");
        assertNotNull(yangTextSchemaContextResolver);
        final URL yangFile1 = getClass().getResource("/yang-text-schema-context-resolver-test/foo-feature.yang");
        assertNotNull(yangFile1);
        final URL yangFile2 = getClass().getResource("/yang-text-schema-context-resolver-test/aux-feature.yang");
        assertNotNull(yangFile2);

        final YangTextSchemaSourceRegistration registration1 =
                yangTextSchemaContextResolver.registerSource(yangFile1);
        assertNotNull(registration1);
        final YangTextSchemaSourceRegistration registration2 =
                yangTextSchemaContextResolver.registerSource(yangFile2);
        assertNotNull(registration2);

        final QName cont = QName.create("foo-feature-namespace", "2016-09-26", "bar-feature-container");
        final QName condLeaf = QName.create("foo-feature-namespace", "2016-09-26", "conditional-leaf");
        final QName uncondLeaf = QName.create("foo-feature-namespace", "2016-09-26", "unconditional-leaf");
        final QName auxCont = QName.create("aux-feature-namespace", "2016-09-26", "aux-cond-cont");

        final QName usedFeature = QName.create("foo-feature-namespace", "2016-09-26", "used-feature");
        final QName unusedFeature = QName.create("foo-feature-namespace", "2016-09-26", "unused-feature");

        Iterable<QName> pathToConditional = List.of(cont, condLeaf);
        Iterable<QName> pathToUnconditional = List.of(cont, uncondLeaf);
        Iterable<QName> pathToAuxiliary = List.of(auxCont);

        final EffectiveModelContext context1 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context1, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context1, uncondLeaf.getModule(), pathToUnconditional));
        assertTrue(isModulePresent(context1, auxCont.getModule(), pathToAuxiliary));

        final Registration featRegistration1 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(unusedFeature));
        final EffectiveModelContext context2 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertFalse(isModulePresent(context2, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context2, uncondLeaf.getModule(), pathToUnconditional));
        assertTrue(isModulePresent(context2, auxCont.getModule(), pathToAuxiliary));

        final Registration featRegistration2 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(usedFeature));
        final EffectiveModelContext context3 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context3, condLeaf.getModule(), pathToConditional));

        final Registration featRegistration3 = yangTextSchemaContextResolver.registerSupportedFeatures(
                unusedFeature.getModule(), Set.of(usedFeature, unusedFeature));
        featRegistration1.close();
        featRegistration2.close();
        final EffectiveModelContext context4 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context4, condLeaf.getModule(), pathToConditional));
        assertTrue(isModulePresent(context4, auxCont.getModule(), pathToAuxiliary));

        featRegistration3.close();
        final Registration featRegistration4 = yangTextSchemaContextResolver.registerSupportedFeatures(
                auxCont.getModule(), Set.of());
        final EffectiveModelContext context5 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context5, condLeaf.getModule(), pathToConditional));
        assertFalse(isModulePresent(context5, auxCont.getModule(), pathToAuxiliary));

        featRegistration4.close();
        final EffectiveModelContext context6 = yangTextSchemaContextResolver.getEffectiveModelContext().orElseThrow();

        assertTrue(isModulePresent(context6, auxCont.getModule(), pathToAuxiliary));
    }

    private static boolean isModulePresent(EffectiveModelContext context, QNameModule qnameModule, Iterable<QName>
            path) {
        for (Module module : context.getModules()) {
            if (module.getQNameModule().equals(qnameModule)) {
                return module.findDataTreeChild(path).isPresent();
            }
        }
        throw new AssertionError("No module with given QNameModule present in the context.");
    }
}
