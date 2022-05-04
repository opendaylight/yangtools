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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
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
}
