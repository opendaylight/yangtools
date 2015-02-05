package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface SubmoduleStatement extends
    Statement<SubmoduleStatement>,
    LinkageGroup,
    MetaGroup,
    RevisionGroup,
    BodyGroup {

    String getName();

    @Override
    public Iterable<? extends UnknownStatement> getStmtSeps();

    public @Nullable YangVersionStatement getYangVersion();

    public @Nonnull BelongsToStatement getBelongsTo();

    @Override
    public Iterable<? extends ImportStatement> getImports();

    @Override
    public Iterable<? extends IncludeStatement> getIncludes();

    @Override
    public OrganizationStatement getOrganization();

    @Override
    public ContactStatement getContact();

    @Override
    public DescriptionStatement getDescription();

    @Override
    public ReferenceStatement getReference();

    @Override
    public Iterable<? extends RevisionStatement> getRevisions();

    @Override
    public Iterable<? extends ExtensionStatement> getExtensions();

    @Override
    public Iterable<? extends FeatureStatement> getFeatures();

    @Override
    public Iterable<? extends IdentityStatement> getIdentities();

    @Override
    public Iterable<? extends TypedefStatement> getTypedefs();

    @Override
    public Iterable<? extends GroupingStatement> getGroupings();

    @Override
    public Iterable<? extends DataDefinitionStatement<?>> getDataDefinitions();

    @Override
    public Iterable<? extends AugmentStatement> getAugments();

    @Override
    public Iterable<? extends RpcStatement> getRpcs();

    @Override
    public Iterable<? extends NotificationStatement> getNotifications();

    @Override
    public Iterable<? extends DeviationStatement> getDeviations();
}

