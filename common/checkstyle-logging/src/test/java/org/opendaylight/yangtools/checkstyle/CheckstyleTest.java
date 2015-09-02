/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CheckstyleTest {

    private Checker checker;
    private ByteArrayOutputStream baos;

    @Before
    public void setup() throws CheckstyleException {
        baos = new ByteArrayOutputStream();
        AuditListener listener = new DefaultLogger(baos, false);

        InputSource inputSource = new InputSource(CheckstyleTest.class.getClassLoader().getResourceAsStream(
                "checkstyle-logging.xml"));
        Configuration configuration = ConfigurationLoader.loadConfiguration(inputSource,
                new PropertiesExpander(System.getProperties()), false);

        checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        checker.configure(configuration);
        checker.addListener(listener);
    }

    @After
    public void destroy() {
        checker.destroy();
    }

    @Test
    public void testLoggerChecks() throws Exception {
        verify(CheckLoggingTestClass.class, true, "15: Logger must be declared as private static final.", "15: Logger name should be LOG.",
                "15: LoggerFactory.getLogger Class argument is incorrect.",
                "17: Logger might be declared only once.", "16: Logger must be slf4j.", "22: Line contains printStacktrace",
                "23: Line contains console output", "24: Line contains console output", "26: Log message placeholders count is incorrect.",
                "32: Log message placeholders count is incorrect", "41: Log message contains string concatenation.");
    }

    @Test
    public void testCodingChecks() {
        verify(CheckCodingStyleTestClass.class, false, "9: Line has Windows line delimiter.", "14: Wrong order for", "24:1: Line contains a tab character.",
                "22: Line has trailing spaces.", "22: ctor def child at indentation level 16 not at correct indentation, 8", "17:8: Unused import",
                "23: Line has trailing spaces.");
    }

    private void verify(final Class<?> testClass, final boolean checkCount, final String... expectedMessages) {
        final String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator + testClass.getName().replaceAll("\\.", "/") + ".java";
        final File testFile = new File(filePath);
        checker.process(Lists.newArrayList(testFile));
        final String output = baos.toString();
        System.out.println();
        if (checkCount) {
            final int count = output.split("\n").length - 2;
            assertEquals(expectedMessages.length, count);
        }
        for(final String message : expectedMessages) {
            assertTrue("Expected message not found: " + message, output.contains(message));
        }
    }
}
