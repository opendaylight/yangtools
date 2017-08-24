/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class MultipleRevImportBug6875Test {
    private static final String BAR_NS = "bar";
    private static final String BAR_REV_1 = "2017-02-06";
    private static final String BAR_REV_2 = "1999-01-01";
    private static final String BAR_REV_3 = "1970-01-01";
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void testYang11() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-multiple-rev-import-test");

        final SettableSchemaProvider<ASTSchemaSource> foo = getSourceProvider("/rfc7950/bug6875/yang1-1/foo.yang");
        final SettableSchemaProvider<ASTSchemaSource> bar1 = getSourceProvider("/rfc7950/bug6875/yang1-1/bar@1999-01-01.yang");
        final SettableSchemaProvider<ASTSchemaSource> bar2 = getSourceProvider("/rfc7950/bug6875/yang1-1/bar@2017-02-06.yang");
        final SettableSchemaProvider<ASTSchemaSource> bar3 = getSourceProvider("/rfc7950/bug6875/yang1-1/bar@1970-01-01.yang");

        setAndRegister(sharedSchemaRepository, foo);
        setAndRegister(sharedSchemaRepository, bar1);
        setAndRegister(sharedSchemaRepository, bar2);
        setAndRegister(sharedSchemaRepository, bar3);

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final ListenableFuture<SchemaContext> schemaContextFuture = fact
                .createSchemaContext(ImmutableList.of(foo.getId(), bar1.getId(), bar2.getId(), bar3.getId()));
        assertTrue(schemaContextFuture.isDone());

        final SchemaContext context = schemaContextFuture.get();
        assertEquals(context.getModules().size(), 4);

        assertTrue(findNode(context, ImmutableList.of(foo("root"), foo("my-container-1"))) instanceof ContainerSchemaNode);
        assertTrue(findNode(context, ImmutableList.of(foo("root"), foo("my-container-2"))) instanceof ContainerSchemaNode);

        assertTrue(findNode(context, ImmutableList.of(bar3("root"), foo("my-container-1"))) instanceof ContainerSchemaNode);
        assertTrue(findNode(context, ImmutableList.of(bar3("root"), foo("my-container-2"))) instanceof ContainerSchemaNode);

        assertNull(findNode(context, ImmutableList.of(bar2("root"), foo("my-container-1"))));
        assertNull(findNode(context, ImmutableList.of(bar2("root"), foo("my-container-2"))));

        assertNull(findNode(context, ImmutableList.of(bar1("root"), foo("my-container-1"))));
        assertNull(findNode(context, ImmutableList.of(bar1("root"), foo("my-container-2"))));
    }

    @Test
    public void testYang10() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-multiple-rev-import-test");

        final SettableSchemaProvider<ASTSchemaSource> foo = getSourceProvider("/rfc7950/bug6875/yang1-0/foo.yang");
        final SettableSchemaProvider<ASTSchemaSource> bar1 = getSourceProvider("/rfc7950/bug6875/yang1-0/bar@1999-01-01.yang");
        final SettableSchemaProvider<ASTSchemaSource> bar2 = getSourceProvider("/rfc7950/bug6875/yang1-0/bar@2017-02-06.yang");

        setAndRegister(sharedSchemaRepository, foo);
        setAndRegister(sharedSchemaRepository, bar1);
        setAndRegister(sharedSchemaRepository, bar2);

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final ListenableFuture<SchemaContext> schemaContextFuture = fact.createSchemaContext(
            Lists.newArrayList(foo.getId(), bar1.getId(), bar2.getId()));
        assertTrue(schemaContextFuture.isDone());

        try {
            schemaContextFuture.get();
            fail("Test should fail due to invalid imports of yang source.");
        } catch (final ExecutionException e) {
            assertTrue(e.getCause().getCause().getMessage().startsWith(
                "Module:bar imported twice with different revisions"));
        }
    }

    private static void setAndRegister(final SharedSchemaRepository sharedSchemaRepository,
            final SettableSchemaProvider<ASTSchemaSource> source) {
        source.register(sharedSchemaRepository);
        source.setResult();
    }

    private static SettableSchemaProvider<ASTSchemaSource> getSourceProvider(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
            ASTSchemaSource.class);
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<QName> qNames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qNames, true));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, FOO_REV, localName);
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
