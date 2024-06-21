/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.Map;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.plugin.generator.api.AbstractFileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;

@MetaInfServices(value = FileGeneratorFactory.class)
public final class JavaFileGeneratorFactory extends AbstractFileGeneratorFactory {
    public JavaFileGeneratorFactory() {
        super("BindingJavaFileGenerator");
    }

    @Override
    public JavaFileGenerator newFileGenerator(final Map<String, String> configuration) {
        return new JavaFileGenerator(configuration);
    }
}
