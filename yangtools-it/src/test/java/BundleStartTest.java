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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
    private static final String YANG_VERSION = "0.6.2-SNAPSHOT";
    private static final String MVN_PREFIX = "mvn:" + GROUP;

    @Inject
    BundleContext ctx;

    private List<Option> coreBundles() {
        List<Option> options = new ArrayList<>();

        options.add(mavenBundle("org.slf4j", "slf4j-api", "1.7.2"));
        options.add(mavenBundle("org.slf4j", "log4j-over-slf4j", "1.7.2"));
        options.add(mavenBundle("ch.qos.logback", "logback-core", "1.0.9"));
        options.add(mavenBundle("ch.qos.logback", "logback-classic", "1.0.9"));
        options.add(mavenBundle("com.google.guava", "guava", "14.0.1"));
        options.add(mavenBundle("org.apache.commons", "commons-lang3", "3.1"));
        options.add(mavenBundle("org.opendaylight.yangtools.thirdparty", "antlr4-runtime-osgi-nohead", "4.0"));
        options.add(mavenBundle("org.opendaylight.yangtools.thirdparty", "xtend-lib-osgi", "2.4.3"));
        options.add(mavenBundle("org.sonatype.plexus", "plexus-build-api", "0.0.7"));
        options.add(mavenBundle("org.codehaus.plexus", "plexus-slf4j-logging", "1.1"));
        options.add(mavenBundle("org.javassist", "javassist", "3.17.1-GA"));

        options.add(mavenBundle(GROUP, "concepts", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-binding", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-common", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-data-api", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-data-impl", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-data-util", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-model-api", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-model-util", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-parser-api", YANG_VERSION));
        options.add(mavenBundle(GROUP, "yang-parser-impl", YANG_VERSION));

        options.add(mavenBundle(GROUP, "binding-generator-api", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-generator-impl", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-generator-spi", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-generator-util", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-model-api", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-java-api-generator", YANG_VERSION));
        options.add(mavenBundle(GROUP, "binding-type-provider", YANG_VERSION));
        options.add(mavenBundle(GROUP, "maven-sal-api-gen-plugin", YANG_VERSION));

        options.add(junitBundles());
        return options;
    }

    @Configuration
    public final Option[] config() {
        final List<Option> options = coreBundles();
        return options.toArray(new Option[0]);
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

        testBundle(MVN_PREFIX + "/binding-generator-api/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-impl/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-spi/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-generator-util/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-model-api/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-java-api-generator/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/binding-type-provider/" + YANG_VERSION);
        testBundle(MVN_PREFIX + "/maven-sal-api-gen-plugin/" + YANG_VERSION);
    }

    private void testBundle(String name) throws BundleException {
        Bundle bundle = ctx.getBundle(name);
        assertNotNull("Bundle '" + name + "' not found", bundle);
        assertEquals("Bundle '" + name + "' is not in ACTIVE state", Bundle.ACTIVE, bundle.getState());
    }

}
