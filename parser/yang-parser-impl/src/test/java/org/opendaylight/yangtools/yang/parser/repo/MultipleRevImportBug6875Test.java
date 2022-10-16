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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class MultipleRevImportBug6875Test {
    private static final String BAR_NS = "bar";
    private static final String BAR_REV_1 = "2017-02-06";
    private static final String BAR_REV_2 = "1999-01-01";
    private static final String BAR_REV_3 = "1970-01-01";
    private static final String FOO_NS = "foo";

    @Test
    public void testYang11() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-multiple-rev-import-test");

        final SettableSchemaProvider<IRSchemaSource> foo = getSourceProvider(
            "/rfc7950/bug6875/yang1-1/foo.yang");
        final SettableSchemaProvider<IRSchemaSource> bar1 = getSourceProvider(
            "/rfc7950/bug6875/yang1-1/bar@1999-01-01.yang");
        final SettableSchemaProvider<IRSchemaSource> bar2 = getSourceProvider(
            "/rfc7950/bug6875/yang1-1/bar@2017-02-06.yang");
        final SettableSchemaProvider<IRSchemaSource> bar3 = getSourceProvider(
            "/rfc7950/bug6875/yang1-1/bar@1970-01-01.yang");

        setAndRegister(sharedSchemaRepository, foo);
        setAndRegister(sharedSchemaRepository, bar1);
        setAndRegister(sharedSchemaRepository, bar2);
        setAndRegister(sharedSchemaRepository, bar3);

        final ListenableFuture<EffectiveModelContext> schemaContextFuture =
                sharedSchemaRepository.createEffectiveModelContextFactory().createEffectiveModelContext(
                    foo.getId(), bar1.getId(), bar2.getId(), bar3.getId());
        assertTrue(schemaContextFuture.isDone());

        final SchemaContext context = schemaContextFuture.get();
        assertEquals(context.getModules().size(), 4);

        assertThat(context.findDataTreeChild(foo("root"), foo("my-container-1")).orElse(null),
            instanceOf(ContainerSchemaNode.class));
        assertThat(context.findDataTreeChild(foo("root"), foo("my-container-2")).orElse(null),
            instanceOf(ContainerSchemaNode.class));

        assertThat(context.findDataTreeChild(bar3("root"), foo("my-container-1")).orElse(null),
            instanceOf(ContainerSchemaNode.class));
        assertThat(context.findDataTreeChild(bar3("root"), foo("my-container-2")).orElse(null),
            instanceOf(ContainerSchemaNode.class));

        assertEquals(Optional.empty(), context.findDataTreeChild(bar2("root"), foo("my-container-1")));
        assertEquals(Optional.empty(), context.findDataTreeChild(bar2("root"), foo("my-container-2")));

        assertEquals(Optional.empty(), context.findDataTreeChild(bar1("root"), foo("my-container-1")));
        assertEquals(Optional.empty(), context.findDataTreeChild(bar1("root"), foo("my-container-2")));
    }

    @Test
    public void testYang10() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-multiple-rev-import-test");

        final SettableSchemaProvider<IRSchemaSource> foo = getSourceProvider(
            "/rfc7950/bug6875/yang1-0/foo.yang");
        final SettableSchemaProvider<IRSchemaSource> bar1 = getSourceProvider(
            "/rfc7950/bug6875/yang1-0/bar@1999-01-01.yang");
        final SettableSchemaProvider<IRSchemaSource> bar2 = getSourceProvider(
            "/rfc7950/bug6875/yang1-0/bar@2017-02-06.yang");

        setAndRegister(sharedSchemaRepository, foo);
        setAndRegister(sharedSchemaRepository, bar1);
        setAndRegister(sharedSchemaRepository, bar2);

        final ListenableFuture<EffectiveModelContext> schemaContextFuture =
                sharedSchemaRepository.createEffectiveModelContextFactory().createEffectiveModelContext(
                    foo.getId(), bar1.getId(), bar2.getId());
        assertTrue(schemaContextFuture.isDone());

        final ExecutionException ex = assertThrows(ExecutionException.class, schemaContextFuture::get);
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
        assertThat(cause.getMessage(), startsWith("Module:bar imported twice with different revisions"));
    }

    private static void setAndRegister(final SharedSchemaRepository sharedSchemaRepository,
            final SettableSchemaProvider<IRSchemaSource> source) {
        source.register(sharedSchemaRepository);
        source.setResult();
    }

    private static SettableSchemaProvider<IRSchemaSource> getSourceProvider(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToIRTransformer.transformText(yangSource),
            IRSchemaSource.class);
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar1(final String localName) {
        return QName.create(BAR_NS, BAR_REV_1, localName);
    }

    private static QName bar2(final String localName) {
        return QName.create(BAR_NS, BAR_REV_2, localName);
    }

    private static QName bar3(final String localName) {
        return QName.create(BAR_NS, BAR_REV_3, localName);
    }
}
