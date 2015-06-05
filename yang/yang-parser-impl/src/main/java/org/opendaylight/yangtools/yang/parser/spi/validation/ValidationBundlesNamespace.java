package org.opendaylight.yangtools.yang.parser.spi.validation;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public interface ValidationBundlesNamespace extends
        IdentifierNamespace<ValidationBundlesNamespace.ValidationBundleType, Collection<?>> {

    public static enum ValidationBundleType {
        SUPPORTED_REFINE_SUBSTATEMENTS, SUPPORTED_REFINE_TARGETS, SUPPORTED_AUGMENT_TARGETS,
        SUPPORTED_CASE_SHORTHANDS, SUPPORTED_DATA_NODES
    }
}
