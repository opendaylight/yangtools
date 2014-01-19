/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.wadl.generator.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.wadl.generator.WadlRestconfGenerator;
import org.opendaylight.yangtools.yang2sources.spi.CodeGenerator;

public class WadlGenerator implements CodeGenerator {
    
    @Override
    public Collection<File> generateSources(SchemaContext context, File outputDir, Set<Module> currentModules)
            throws IOException {
        
        final File outputBaseDir;
        if (outputDir == null) {
            outputBaseDir = new File("target" + File.separator + "generated-sources" + File.separator
                    + "maven-sal-api-gen" + File.separator + "wadl");
        } else {
            outputBaseDir = outputDir;
        }

        final WadlRestconfGenerator generator = new WadlRestconfGenerator(outputBaseDir);
        return generator.generate(context, currentModules);
    }

    @Override
    public void setLog(Log log) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMavenProject(MavenProject project) {
        // TODO Auto-generated method stub
        
    }

}
