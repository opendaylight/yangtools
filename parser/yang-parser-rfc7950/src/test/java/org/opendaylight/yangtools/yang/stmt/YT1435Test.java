/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

public class YT1435Test {
    @Test
    public void testModulesWithFooAddedAsLibSourcesFirst() throws Exception {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addLibSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-a.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-b.yang"))
                .addSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang")
                        )
                .buildEffective();
        assertNotNull(schemaContext);
    }

    @Test
    public void testModulesWithFooAddedAsLibSourcesLater() throws Exception {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang")
                )
                .addLibSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-a.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-b.yang"))
                .buildEffective();
        assertNotNull(schemaContext);
    }

    @Test
    public void testModulesWithAllAddedAsLibSourcesFirst() throws Exception {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addLibSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-a.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-b.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang"))
                .addSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang")
                )
                .buildEffective();
        assertNotNull(schemaContext);
    }

    @Ignore
    @Test
    public void testModulesWithAllAddedAsLibSourcesLater() throws Exception {
        final var schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang")
                )
                .addLibSources(
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-a.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/foo-b.yang"),
                        StmtTestUtils.sourceForResource(
                                "/bugs/YT1435/bar.yang"))
                .buildEffective();
        assertNotNull(schemaContext);
    }
}
