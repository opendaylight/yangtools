/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.system.test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_YANG_DIR_PATH = "./src/main/yang";

    /*
     * mvn exec:exec@yang-parser-system-test-exec
     * mvn exec:exec@yang-parser-system-test-exec -Dexec.executable="java"
     * mvn exec:exec@yang-parser-system-test-exec -Dexec.executable="java" -Dexec.args="-classpath %classpath your.package.MainClass"
     *
     * mvn exec:java@yang-parser-system-test-java
     * mvn exec:java@yang-parser-system-test-java -Dexec.args="./src/main/yang"
     * mvn exec:java@yang-parser-system-test-java -Dexec.mainClass="org.opendaylight.yangtools.yang.parser.system.test.Main" -Dexec.args="./src/main/yang"
     *
     * java -jar yang-system-test-1.1.0-SNAPSHOT-jar-with-dependencies.jar ~/eclipse_workspace/yangtools/yang/yang-system-test/src/main/yang/
     */
    public static void main(final String[] args) throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        final String yangDirPath = args.length == 1 ? args[0] : DEFAULT_YANG_DIR_PATH;
        LOG.info("Yang models dir path: {} ",yangDirPath);
        LOG.info("Successfully resolved SchemaContext: {} ",YangParserUtils.parseYangSources(yangDirPath));
    }
}
