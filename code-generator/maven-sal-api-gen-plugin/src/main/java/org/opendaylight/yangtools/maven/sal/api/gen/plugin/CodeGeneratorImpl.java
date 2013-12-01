/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.maven.sal.api.gen.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.CodeGenerator;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.base.Preconditions;

public final class CodeGeneratorImpl implements CodeGenerator, BuildContextAware {
    private BuildContext buildContext;

    @Override
    public Collection<File> generateSources(final SchemaContext context, final File outputDir,
            final Set<Module> yangModules) throws IOException {
        final File outputBaseDir;
        if (outputDir == null) {
            outputBaseDir = new File("target" + File.separator + "generated-sources" + File.separator
                    + "maven-sal-api-gen");
        } else {
            outputBaseDir = outputDir;
        }

        final BindingGenerator bindingGenerator = new BindingGeneratorImpl();
        final List<Type> types = bindingGenerator.generateTypes(context, yangModules);
        final GeneratorJavaFile generator = new GeneratorJavaFile(buildContext, new HashSet<>(types));

        return generator.generateToFile(outputBaseDir);
    }

    @Override
    public void setLog(Log log) {
        // use maven logging if necessary

    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        // no additional config utilized
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        // no resource processing necessary
    }

    @Override
    public void setMavenProject(MavenProject project) {
        // no additional information needed
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
    }

}
