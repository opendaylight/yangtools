package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

abstract class AbstractConstrainedTypeDefinitionBuilder<T extends TypeDefinition<T>>
        extends AbstractTypeDefinitionBuilder<T> {
    // FIXME: get source reference
    private final TypeConstraints constraints = new TypeConstraints("foo", 4);

    final TypeConstraints getConstraints() {
        return constraints;
    }

    protected final TypeConstraints validConstraints() {
        constraints.validateConstraints();
        return constraints;
    }
}
