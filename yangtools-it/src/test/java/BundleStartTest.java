/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@RunWith(PaxExam.class)
public class BundleStartTest {

    private static final String GROUP = "org.opendaylight.yangtools";
    private static final String YANG_VERSION = "0.5.9-SNAPSHOT";
    private static final String CODEGEN_VERSION = "0.6.0-SNAPSHOT";
    private static final String MVN_PREFIX = "mvn:" + GROUP;

    @Inject
    BundleContext ctx;

    @Configuration
    public Option[] config() {

        return options(
                mavenBundle("org.slf4j", "slf4j-api", "1.7.2"),
                mavenBundle("org.slf4j", "log4j-over-slf4j", "1.7.2"),
                mavenBundle("ch.qos.logback", "logback-core", "1.0.9"),
                mavenBundle("ch.qos.logback", "logback-classic", "1.0.9"),
                mavenBundle("com.google.guava", "guava", "14.0.1"),
                mavenBundle("commons-lang", "commons-lang", "2.3"),

                mavenBundle(GROUP, "concepts", "0.1.1-SNAPSHOT"),
                mavenBundle(GROUP, "yang-binding", YANG_VERSION),
                mavenBundle(GROUP, "yang-common", YANG_VERSION),
                mavenBundle(GROUP, "yang-data-api", YANG_VERSION),
                mavenBundle(GROUP, "yang-data-impl", YANG_VERSION),
                mavenBundle(GROUP, "yang-data-util", YANG_VERSION),
                mavenBundle(GROUP, "yang-model-api", YANG_VERSION),
                mavenBundle(GROUP, "yang-model-util", YANG_VERSION),
                mavenBundle(GROUP, "yang-parser-api", YANG_VERSION),
                mavenBundle(GROUP, "yang-parser-impl", YANG_VERSION),

                mavenBundle(GROUP, "binding-generator-api", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-generator-impl", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-generator-spi", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-generator-util", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-java-api-generator", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-model-api", CODEGEN_VERSION),
                mavenBundle(GROUP, "binding-type-provider", CODEGEN_VERSION),
                mavenBundle(GROUP, "maven-sal-api-gen-plugin", CODEGEN_VERSION),

                junitBundles());
    }

    @Test
    public void testBundleActivation() throws BundleException {
        testBundle(MVN_PREFIX + "/yang-binding/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-common/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-data-api/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-data-impl/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-data-util/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-model-api/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-model-util/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-parser-api/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/yang-parser-impl/" + YANG_VERSION);

        testBundle(MVN_PREFIX + "/binding-generator-api/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-impl/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-spi/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-util/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-java-api-generator/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-model-api/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/binding-type-provider/" + CODEGEN_VERSION);
        testBundle(MVN_PREFIX + "/maven-sal-api-gen-plugin/" + CODEGEN_VERSION);
    }

    private void testBundle(String name) throws BundleException {
        Bundle bundle = ctx.getBundle(name);
        assertNotNull("Bundle '" + name + "' not found", bundle);
        assertEquals("Bundle '" + name + "' is not in ACTIVE state", Bundle.ACTIVE, bundle.getState());
    }

}
