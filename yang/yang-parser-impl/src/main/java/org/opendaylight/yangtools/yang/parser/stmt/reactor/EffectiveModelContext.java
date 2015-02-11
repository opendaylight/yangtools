package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public class EffectiveModelContext implements Immutable {

    private final ImmutableList<DeclaredStatement<?>> rootStatements;

    public EffectiveModelContext(List<DeclaredStatement<?>> rootStatements) {
        this.rootStatements = ImmutableList.copyOf(rootStatements);
    }

    public ImmutableList<DeclaredStatement<?>> getRootStatements() {
        return rootStatements;
    }

}
