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

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

public class CheckstyleTest {

    private Checker checker;
    private ByteArrayOutputStream baos;

    @Before
    public void setup() throws CheckstyleException {
        baos = new ByteArrayOutputStream();
        final AuditListener listener = new DefaultLogger(baos, false);

        final InputSource inputSource = new InputSource(CheckstyleTest.class.getClassLoader().getResourceAsStream(
                "checkstyle-logging.xml"));
        final Configuration configuration = ConfigurationLoader.loadConfiguration(inputSource,
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
        verify(CheckLoggingTestClass.class, true,
                "16: Logger must be declared as private static final",
                "16: Logger name should be LOG",
                "16: LoggerFactory.getLogger Class argument is incorrect",
                "18: Logger might be declared only once",
                "17: Logger must be slf4j",
                "27: Log message placeholders count is incorrect",
                "36: Log message placeholders count is incorrect",
                "45: Log message contains string concatenation");
    }

    @Test
    public void testLogMessageExtractorCheck() throws Exception {
        File logMessageReport = LogMessageExtractorCheck.DEFAULT_REPORT_FILE;
        logMessageReport.delete();
        verify(CheckLoggingTestClass.class, false);
        List<String> reportLines = Files.readAllLines(logMessageReport.toPath(), StandardCharsets.UTF_8);
        assertEquals(6, reportLines.size());
        assertEquals("src/test/java/org/opendaylight/yangtools/checkstyle/CheckLoggingTestClass.java:27:\"foo {} {}\"", reportLines.get(0));
        // TODO assertEquals("src/test/java/org/opendaylight/yangtools/checkstyle/CheckLoggingTestClass.java:28:\"foo {} bar {}\"", reportLines.get(1));
    }

    private void verify(final Class<?> testClass, final boolean checkCount, final String... expectedMessages) throws CheckstyleException {
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
            assertTrue("Expected message not found: " + message + "; output: " + output, output.contains(message));
        }
    }
}
