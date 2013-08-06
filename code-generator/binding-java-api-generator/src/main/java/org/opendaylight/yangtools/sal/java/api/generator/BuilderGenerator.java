package org.opendaylight.yangtools.sal.java.api.generator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.opendaylight.yangtools.sal.java.api.generator.BuilderTemplate;
import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public final class BuilderGenerator implements CodeGenerator {

    public static final String FILE_NAME_SUFFIX = "Builder";

    @Override
    public Writer generate(Type type) throws IOException {
        final Writer writer = new StringWriter();
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            final GeneratedType genType = (GeneratedType) type;
            final BuilderTemplate template = new BuilderTemplate(genType);
            writer.write(template.generate().toString());
        }
        return writer;
    }

}
