/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;

@MetaInfServices(value = FileGeneratorFactory.class)
@NonNullByDefault
public final class TestFileGeneratorFactory extends AbstractFileGeneratorFactory {
    public static final String PREFIX = "prefix";

    public TestFileGeneratorFactory() {
        super(TestFileGenerator.class.getName());
    }

    @Override
    public FileGenerator newFileGenerator(final Map<String, String> configuration) {
        return new TestFileGenerator(configuration.get(PREFIX));
    }
}
