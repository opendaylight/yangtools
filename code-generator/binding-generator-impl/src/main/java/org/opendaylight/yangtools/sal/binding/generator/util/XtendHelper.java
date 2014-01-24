package org.opendaylight.yangtools.sal.binding.generator.util;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class XtendHelper {

    @SuppressWarnings({"rawtypes","unchecked"})
    public static Iterable<TypeDefinition> getTypes(UnionTypeDefinition definition) {
        return (Iterable<TypeDefinition>) (List) definition.getTypes();
    }
}
