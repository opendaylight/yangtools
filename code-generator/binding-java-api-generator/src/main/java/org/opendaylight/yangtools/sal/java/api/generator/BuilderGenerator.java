package org.opendaylight.yangtools.sal.java.api.generator;

import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;

public final class BuilderGenerator implements CodeGenerator {

    public static final String BUILDER = "Builder";

    @Override
    public boolean isAcceptable(Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            for (Type t : ((GeneratedType) type).getImplements()) {
                // "rpc" and "grouping" elements do not implement Augmentable
                if (t.getFullyQualifiedName().equals(Augmentable.class.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String generate(Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            final GeneratedType genType = (GeneratedType) type;
            final BuilderTemplate template = new BuilderTemplate(genType);
            return template.generate();
        }
        return "";
    }

    @Override
    public String getUnitName(Type type) {
        return type.getName() + BUILDER;
    }

}
