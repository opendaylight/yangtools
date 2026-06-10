/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

class BugBelongsTo {

    @Test
    void testUnusedLibrarySubmoduleCrashes() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild();

        // 1. Add an UNRELATED module as the primary source
        reactor.addSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-yang-types@2013-07-15.yang"));

        // 2. Add the module and submodule as UNUSED library sources
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-netconf-config@2013-10-21.yang"));
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-netconf-tls@2013-10-21.yang"));
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-x509-cert-to-name@2014-12-10.yang"));
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-netconf-acm@2018-02-14.yang"));
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-inet-types@2013-07-15.yang"));
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/ietf-netconf-common@2013-10-21.yang"));

        // 3. THIS WILL CRASH with InferenceException: Submodule was not resolved
        final var context = reactor.buildEffective();

        assertNotNull(context);
    }
}