package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;



public interface LeafListStatement extends DataDefinitionStatement<LeafListStatement>, MultipleElementsGroup, TypedGroup {

    @Override
    public String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    @Override
    public @Nonnull TypeStatement getType();

    @Override
    public @Nullable UnitsStatement getUnits();

    public @Nullable Iterable<? extends MustStatement> getMusts();

    @Nullable ConfigStatement getConfig();

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

}
