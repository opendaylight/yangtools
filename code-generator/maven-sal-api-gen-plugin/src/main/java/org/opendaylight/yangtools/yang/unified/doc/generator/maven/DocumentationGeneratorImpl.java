package org.opendaylight.yangtools.yang.unified.doc.generator.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.unified.doc.generator.GeneratorImpl;
import org.opendaylight.yangtools.yang2sources.spi.CodeGenerator;

public class DocumentationGeneratorImpl extends GeneratorImpl implements CodeGenerator {

    @Override
    public Collection<File> generateSources(SchemaContext arg0, File arg1, Set<Module> arg2) throws IOException {
        // TODO Auto-generated method stub
         generate(arg0, arg1, arg2);
         return Collections.emptySet();
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
}
