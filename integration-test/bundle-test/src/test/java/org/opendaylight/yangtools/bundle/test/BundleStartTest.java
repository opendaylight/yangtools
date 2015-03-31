/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.bundle.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

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

    @Inject
    BundleContext ctx;

    private List<Option> coreBundles() {
        List<Option> options = new ArrayList<>();

        options.add(mavenBundle("org.slf4j", "slf4j-api").versionAsInProject());
        options.add(mavenBundle("ch.qos.logback", "logback-core").versionAsInProject());
        options.add(mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject());
        options.add(mavenBundle("com.google.guava", "guava").versionAsInProject());
        options.add(mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject());
        options.add(mavenBundle("org.opendaylight.yangtools.thirdparty", "antlr4-runtime-osgi-nohead")
                .versionAsInProject());
        options.add(mavenBundle("org.javassist", "javassist").versionAsInProject());
        options.add(mavenBundle("com.github.romix", "java-concurrent-hash-trie-map").versionAsInProject());

        options.add(mavenBundle(GROUP, "concepts").versionAsInProject());
        options.add(mavenBundle(GROUP, "util").versionAsInProject());
        options.add(mavenBundle(GROUP, "object-cache-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-binding").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-common").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-data-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-data-impl").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-data-util").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-model-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-model-util").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-parser-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "yang-parser-impl").versionAsInProject());

        options.add(mavenBundle(GROUP, "binding-generator-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "binding-generator-impl").versionAsInProject());
        options.add(mavenBundle(GROUP, "binding-generator-spi").versionAsInProject());
        options.add(mavenBundle(GROUP, "binding-generator-util").versionAsInProject());
        options.add(mavenBundle(GROUP, "binding-model-api").versionAsInProject());
        options.add(mavenBundle(GROUP, "binding-type-provider").versionAsInProject());

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
        testBundle("yang-binding");
        testBundle("yang-common");
        testBundle("yang-data-api");
        testBundle("yang-data-impl");
        testBundle("yang-data-util");
        testBundle("yang-model-api");
        testBundle("yang-model-util");
        testBundle("yang-parser-api");
        testBundle("yang-parser-impl");

        testBundle("binding-generator-api");
        testBundle("binding-generator-impl");
        testBundle("binding-generator-spi");
        testBundle("binding-generator-util");
        testBundle("binding-model-api");
        testBundle("binding-type-provider");
    }

    private void testBundle(String name) throws BundleException {
        Bundle bundle = getBundle(name);
        assertNotNull("Bundle '" + name + "' not found", bundle);
        assertEquals("Bundle '" + name + "' is not in ACTIVE state", Bundle.ACTIVE, bundle.getState());
    }

    private Bundle getBundle(final String name) {
        final String bn = GROUP + "." + name;
        for (Bundle b : ctx.getBundles()) {
            if (bn.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }

}
