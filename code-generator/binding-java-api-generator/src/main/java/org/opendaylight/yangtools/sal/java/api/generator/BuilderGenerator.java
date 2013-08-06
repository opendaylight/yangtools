package org.opendaylight.yangtools.sal.java.api.generator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.java.api.generator.BuilderTemplate;
import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.DataObject;

public final class BuilderGenerator extends AbstractCodeGenerator {

    public static final String FILE_NAME_SUFFIX = "Builder";

    @Override
    public Writer generate(Type type) throws IOException {
        final Writer writer = new StringWriter();
        if (type instanceof GeneratedType  && isAcceptable((GeneratedType )type)) {
            final GeneratedType genType = (GeneratedType) type;
            final BuilderTemplate template = new BuilderTemplate(genType);
            writer.write(template.generate().toString());
        }
        return writer;
    }

    public boolean isAcceptable(Type type) {
    	return super.isAcceptable(type) && type instanceof GeneratedType 
    	&& ((GeneratedType )type).getImplements().contains(Types.typeForClass(DataObject.class));
    }

}
