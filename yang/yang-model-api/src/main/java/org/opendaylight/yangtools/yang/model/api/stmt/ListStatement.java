package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

public interface ListStatement extends DataDefinitionStatement<ListStatement>, MultipleElementsGroup,
        DataDefinitionContainer.WithReusableDefinitions {

    @Override
    public String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    public Iterable<? extends MustStatement> getMusts();

    public @Nullable KeyStatement getKey();

    public Iterable<? extends UniqueStatement> getUnique();

    public @Nullable ConfigStatement getConfig();

    @Override
    public @Nullable MinElementsStatement getMinElements();

    @Override
    public @Nullable MaxElementsStatement getMaxElements();

    @Override
    public @Nullable OrderedByStatement getOrderedBy();

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
