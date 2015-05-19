/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class ListKeysTest {

    @Test
    public void correctListKeysTest() throws IOException, YangSyntaxErrorException, URISyntaxException {
        File yangFile = new File(getClass().getResource("/list-keys-test/correct-list-keys-test.yang").toURI());

        YangParserImpl parser = YangParserImpl.getInstance();
        parser.parseFile(yangFile, yangFile.getParentFile());
    }

    @Test(expected = YangParseException.class)
    public void incorrectListKeysTest1() throws IOException, YangSyntaxErrorException, URISyntaxException {
        File yangFile = new File(getClass().getResource("/list-keys-test/incorrect-list-keys-test.yang").toURI());

        YangParserImpl parser = YangParserImpl.getInstance();
        parser.parseFile(yangFile, yangFile.getParentFile());
    }

    @Test(expected = YangParseException.class)
    public void incorrectListKeysTest2() throws IOException, YangSyntaxErrorException, URISyntaxException {
        File yangFile = new File(getClass().getResource("/list-keys-test/incorrect-list-keys-test2.yang").toURI());

        YangParserImpl parser = YangParserImpl.getInstance();
        parser.parseFile(yangFile, yangFile.getParentFile());
    }

    @Test(expected = YangParseException.class)
    public void incorrectListKeysTest3() throws IOException, YangSyntaxErrorException, URISyntaxException {
        File yangFile = new File(getClass().getResource("/list-keys-test/incorrect-list-keys-test3.yang").toURI());

        YangParserImpl parser = YangParserImpl.getInstance();
        parser.parseFile(yangFile, yangFile.getParentFile());
    }

    @Test(expected = YangParseException.class)
    public void incorrectListKeysTest4() throws IOException, YangSyntaxErrorException, URISyntaxException {
        File yangFile = new File(getClass().getResource("/list-keys-test/incorrect-list-keys-test4.yang").toURI());

        YangParserImpl parser = YangParserImpl.getInstance();
        parser.parseFile(yangFile, yangFile.getParentFile());
    }

}
