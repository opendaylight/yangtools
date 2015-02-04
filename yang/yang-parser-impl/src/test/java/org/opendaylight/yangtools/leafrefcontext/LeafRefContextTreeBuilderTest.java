/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.leafref.parser.LeafRefPathSyntaxErrorException;

import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import java.io.File;
import org.junit.Test;

public class LeafRefContextTreeBuilderTest {

    @Test
    public void buildLeafRefContextTreeTest() throws URISyntaxException, IOException, YangSyntaxErrorException {
        File resourceFile = new File(getClass().getResource("/leafref-context-test/correct-modules/leafref-test.yang")
                .toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        SchemaContext context = parser.parseFile(resourceFile, resourceDir);

       LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(context);

       LeafRefContext rootLeafRefContext = leafRefContextTreeBuilder.buildLeafRefContextTree();

       System.out.println(rootLeafRefContext.getCurrentNodeQName());

    }

    @Test(expected=LeafRefPathSyntaxErrorException.class)
    public void incorrectLeafRefPathTest() throws URISyntaxException, IOException, YangSyntaxErrorException {
        File resourceFile = new File(getClass().getResource("/leafref-context-test/incorrect-modules/leafref-test.yang")
                .toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        SchemaContext context = parser.parseFile(resourceFile, resourceDir);

       LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(context);

       leafRefContextTreeBuilder.buildLeafRefContextTree();

    }

}
