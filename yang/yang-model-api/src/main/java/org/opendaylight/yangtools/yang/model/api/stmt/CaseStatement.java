package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;


public interface CaseStatement extends DeclaredStatement<QName>, DataDefinitionContainer, DocumentationGroup.WithStatus, ConditionalDataDefinition {

    @Nonnull String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

    @Override
    public Iterable<? extends DataDefinitionStatement> getDataDefinitions();

}
