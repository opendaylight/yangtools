/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import com.google.common.base.Preconditions;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;

public abstract class AbstractTypesTest {

    private final URL testSourcesDirUrl;
    protected Set<File> testModels;

    AbstractTypesTest(final URL testSourcesDirUrl) {
        this.testSourcesDirUrl = testSourcesDirUrl;
    }

    @Before
    public void loadTestResources() throws URISyntaxException {
        File testSourcesDir = new File(testSourcesDirUrl.toURI());
        File[] testFiles = Preconditions.checkNotNull(testSourcesDir.listFiles(), testSourcesDir
                + " does not denote a directory");
        testModels = new HashSet<>();
        for (File file : testFiles) {
            if (file.isFile()) {
                testModels.add(file);
            }
        }
    }

}
