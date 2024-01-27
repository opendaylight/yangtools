/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSetMultimap;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

public class SchemaContextFactoryDeviationsTest extends AbstractSchemaRepositoryTest {
    private static final String FOO = "/bug9195/foo.yang";
    private static final String BAR = "/bug9195/bar.yang";
    private static final String BAZ = "/bug9195/baz.yang";
    private static final String FOOBAR = "/bug9195/foobar.yang";
    private static final String BAR_INVALID = "/bug9195/bar-invalid.yang";
    private static final String BAZ_INVALID = "/bug9195/baz-invalid.yang";
    private static final QNameModule FOO_MODULE = QNameModule.of("foo-ns", "2017-05-16");
    private static final QName MY_FOO_CONT_A = QName.create(FOO_MODULE, "my-foo-cont-a");
    private static final QName MY_FOO_CONT_B = QName.create(FOO_MODULE, "my-foo-cont-b");
    private static final QName MY_FOO_CONT_C = QName.create(FOO_MODULE, "my-foo-cont-c");
    private static final QNameModule BAR_MODULE = QNameModule.of("bar-ns", "2017-05-16");
    private static final QName MY_BAR_CONT_A = QName.create(BAR_MODULE, "my-bar-cont-a");
    private static final QName MY_BAR_CONT_B = QName.create(BAR_MODULE, "my-bar-cont-b");
    private static final QNameModule BAZ_MODULE = QNameModule.of("baz-ns", "2017-05-16");

    @Test
    public void testDeviationsSupportedInSomeModules() {
        final var context = assertModelContext(ImmutableSetMultimap.<QNameModule, QNameModule>builder()
            .put(FOO_MODULE, BAR_MODULE)
            .put(FOO_MODULE, BAZ_MODULE)
            .put(BAR_MODULE, BAZ_MODULE)
            .build(),
            FOO, BAR, BAZ, FOOBAR);

        assertAbsent(context, MY_FOO_CONT_A);
        assertAbsent(context, MY_FOO_CONT_B);
        assertPresent(context, MY_FOO_CONT_C);
        assertAbsent(context, MY_BAR_CONT_A);
        assertPresent(context, MY_BAR_CONT_B);
    }

    @Test
    public void testDeviationsSupportedInAllModules() {
        final var context = assertModelContext(null, FOO, BAR, BAZ, FOOBAR);

        assertAbsent(context, MY_FOO_CONT_A);
        assertAbsent(context, MY_FOO_CONT_B);
        assertAbsent(context, MY_FOO_CONT_C);
        assertAbsent(context, MY_BAR_CONT_A);
        assertAbsent(context, MY_BAR_CONT_B);
    }

    @Test
    public void testDeviationsSupportedInNoModule() {
        final var context = assertModelContext(ImmutableSetMultimap.of(), FOO, BAR, BAZ, FOOBAR);

        assertPresent(context, MY_FOO_CONT_A);
        assertPresent(context, MY_FOO_CONT_B);
        assertPresent(context, MY_FOO_CONT_C);
        assertPresent(context, MY_BAR_CONT_A);
        assertPresent(context, MY_BAR_CONT_B);
    }

    @Test
    public void shouldFailOnAttemptToDeviateTheSameModule2() {
        final var cause = assertInstanceOf(InferenceException.class,
            Throwables.getRootCause(assertExecutionException(null, BAR_INVALID, BAZ_INVALID)));
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
