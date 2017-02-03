/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug7424Test {
    @Test
    public void testRpc() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-rpc.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testNotification() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-notification.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testData() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-data.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testRpcUses() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-rpc-uses.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testExtension() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-extension.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testFeature() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-feature.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testIdentity() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug7424/foo-identity.yang");
            fail("Test should fail due to invalid yang model.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
        }
    }

    @Test
    public void testModules() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug7424/modules");
            fail("Test should fail due to invalid yang models.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
            throw e;
        }
    }

    @Test
    public void testNamespaces() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug7424/namespaces");
            fail("Test should fail due to invalid yang models.");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Error in module 'foo': cannot add '(foo?revision=1970-01-01)name'. "
                                    + "Node name collision: '(foo?revision=1970-01-01)name' already declared."));
            throw e;
        }
    }
}
