/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

public class SchemaContextFactoryDeviationsTest extends AbstractSchemaRepositoryTest {
    private static final String FOO = "/bug9195/foo.yang";
    private static final String BAR = "/bug9195/bar.yang";
    private static final String BAZ = "/bug9195/baz.yang";
    private static final String FOOBAR = "/bug9195/foobar.yang";
    private static final String BAR_INVALID = "/bug9195/bar-invalid.yang";
    private static final String BAZ_INVALID = "/bug9195/baz-invalid.yang";
    private static final XMLNamespace FOO_NS = XMLNamespace.of("foo-ns");
    private static final XMLNamespace BAR_NS = XMLNamespace.of("bar-ns");
    private static final XMLNamespace BAZ_NS = XMLNamespace.of("baz-ns");
    private static final Revision REVISION = Revision.of("2017-05-16");
    private static final QNameModule FOO_MODULE = QNameModule.create(FOO_NS, REVISION);
    private static final QName MY_FOO_CONT_A = QName.create(FOO_MODULE, "my-foo-cont-a");
    private static final QName MY_FOO_CONT_B = QName.create(FOO_MODULE, "my-foo-cont-b");
    private static final QName MY_FOO_CONT_C = QName.create(FOO_MODULE, "my-foo-cont-c");
    private static final QNameModule BAR_MODULE = QNameModule.create(BAR_NS, REVISION);
    private static final QName MY_BAR_CONT_A = QName.create(BAR_MODULE, "my-bar-cont-a");
    private static final QName MY_BAR_CONT_B = QName.create(BAR_MODULE, "my-bar-cont-b");
    private static final QNameModule BAZ_MODULE = QNameModule.create(BAZ_NS, REVISION);

    @Test
    public void testDeviationsSupportedInSomeModules() throws Exception {
        final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations =
                ImmutableSetMultimap.<QNameModule, QNameModule>builder()
                .put(FOO_MODULE, BAR_MODULE)
                .put(FOO_MODULE, BAZ_MODULE)
                .put(BAR_MODULE, BAZ_MODULE)
                .build();

        final ListenableFuture<EffectiveModelContext> lf = createSchemaContext(modulesWithSupportedDeviations, FOO, BAR,
            BAZ, FOOBAR);
        assertTrue(lf.isDone());
        final EffectiveModelContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertAbsent(schemaContext, MY_FOO_CONT_A);
        assertAbsent(schemaContext, MY_FOO_CONT_B);
        assertPresent(schemaContext, MY_FOO_CONT_C);
        assertAbsent(schemaContext, MY_BAR_CONT_A);
        assertPresent(schemaContext, MY_BAR_CONT_B);
    }

    @Test
    public void testDeviationsSupportedInAllModules() throws Exception {
        final ListenableFuture<EffectiveModelContext> lf = createSchemaContext(null, FOO, BAR, BAZ, FOOBAR);
        assertTrue(lf.isDone());
        final EffectiveModelContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertAbsent(schemaContext, MY_FOO_CONT_A);
        assertAbsent(schemaContext, MY_FOO_CONT_B);
        assertAbsent(schemaContext, MY_FOO_CONT_C);
        assertAbsent(schemaContext, MY_BAR_CONT_A);
        assertAbsent(schemaContext, MY_BAR_CONT_B);
    }

    @Test
    public void testDeviationsSupportedInNoModule() throws Exception {
        final ListenableFuture<EffectiveModelContext> lf = createSchemaContext(ImmutableSetMultimap.of(), FOO, BAR, BAZ,
            FOOBAR);
        assertTrue(lf.isDone());
        final EffectiveModelContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertPresent(schemaContext, MY_FOO_CONT_A);
        assertPresent(schemaContext, MY_FOO_CONT_B);
        assertPresent(schemaContext, MY_FOO_CONT_C);
        assertPresent(schemaContext, MY_BAR_CONT_A);
        assertPresent(schemaContext, MY_BAR_CONT_B);
    }

    @Test
    public void shouldFailOnAttemptToDeviateTheSameModule2() throws Exception {
        final ListenableFuture<EffectiveModelContext> lf = createSchemaContext(null, BAR_INVALID, BAZ_INVALID);
        assertTrue(lf.isDone());

        final ExecutionException ex = assertThrows(ExecutionException.class, lf::get);
        final Throwable cause = Throwables.getRootCause(ex);
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(),
            startsWith("Deviation must not target the same module as the one it is defined in"));
    }

    private static void assertAbsent(final EffectiveModelContext schemaContext, final QName qname) {
        assertEquals(Optional.empty(), schemaContext.findDataTreeChild(qname));
    }

    private static void assertPresent(final EffectiveModelContext schemaContext, final QName qname) {
        assertNotEquals(Optional.empty(), schemaContext.findDataTreeChild(qname));
    }
}
