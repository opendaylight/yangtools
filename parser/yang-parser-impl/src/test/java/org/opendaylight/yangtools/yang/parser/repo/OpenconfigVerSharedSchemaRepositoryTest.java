/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;

class OpenconfigVerSharedSchemaRepositoryTest extends AbstractSchemaRepositoryTest {
    @Test
    void testSharedSchemaRepository() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-test");

        final var bar = assertYangTextResource("/openconfig-version/shared-schema-repository/bar@2016-01-01.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final var foo = assertYangTextResource("/openconfig-version/shared-schema-repository/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final var semVer = assertYangTextResource(
            "/openconfig-version/shared-schema-repository/openconfig-extensions.yang");
        semVer.register(sharedSchemaRepository);
        semVer.setResult();

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var inetAndTopologySchemaContextFuture =
                fact.createEffectiveModelContext(bar.getId(), foo.getId(), semVer.getId());
        assertSchemaContext(Futures.getDone(inetAndTopologySchemaContextFuture), 3);

        final var barSchemaContextFuture = fact.createEffectiveModelContext(bar.getId(), semVer.getId());
        assertSchemaContext(Futures.getDone(barSchemaContextFuture), 2);
    }
}
