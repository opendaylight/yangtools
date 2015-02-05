package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;


public interface GroupingStatement extends Statement<GroupingStatement>, DocumentationGroup.WithStatus,DataDefinitionContainer.WithReusableDefinitions {

    String getName();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

    @Override
    public Iterable<? extends TypedefStatement> getTypedefs();

    @Override
    public Iterable<? extends GroupingStatement> getGroupings();

    @Override
    public Iterable<? extends DataDefinitionStatement<?>> getDataDefinitions();

}
