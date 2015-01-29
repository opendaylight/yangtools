package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
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
    public Collection<? extends TypedefStatement> getTypedefs();

    @Override
    public Collection<? extends GroupingStatement> getGroupings();

    @Override
    public Collection<? extends DataDefinitionStatement<?>> getDataDefinitions();

}
