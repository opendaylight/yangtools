/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.common.QName;
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


       QNameModule imp = null;
       QNameModule tst = null;
       Set<Module> modules = context.getModules();
       for (Module module : modules) {
            if (module.getName().equals("import-mod")) {
                imp = module.getQNameModule();
            }
            if (module.getName().equals("leafref-test")) {
                tst = module.getQNameModule();
            }
        }

       QName q1 = QName.create(tst,"odl-project");
       QName q2 = QName.create(tst,"project");
       QName q3 = QName.create(tst,"project-lead");
       QName q4 = QName.create(tst,"project-lead2");

       LeafRefContext leafRefCtx = rootLeafRefContext.getReferencingChildByName(q1).getReferencingChildByName(q2).getReferencingChildByName(q3);

       System.out.println();
       System.out.println("******* Test 1 ************");
       System.out.println("Original definition string:");
       System.out.println(leafRefCtx.getLeafRefTargetPathString());

       System.out.println("Parsed leafref path:");
       System.out.println(leafRefCtx.getLeafRefTargetPath().toString());


       LeafRefContext leafRefCtx2 = rootLeafRefContext.getReferencingChildByName(q1).getReferencingChildByName(q2).getReferencingChildByName(q4);

       System.out.println();
       System.out.println("******* Test 2 ************");
       System.out.println("Original definition string2:");
       System.out.println(leafRefCtx2.getLeafRefTargetPathString());

       System.out.println("Parsed leafref path2:");
       System.out.println(leafRefCtx2.getLeafRefTargetPath().toString());
       System.out.println();

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
