package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
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

    public @Nullable YangVersionStatement getYangVersion();

    public @Nonnull BelongsToStatement getBelongsTo();

    @Override
    public Collection<? extends ImportStatement> getImports();

    @Override
    public Collection<? extends IncludeStatement> getIncludes();

    @Override
    public OrganizationStatement getOrganization();

    @Override
    public ContactStatement getContact();

    @Override
    public DescriptionStatement getDescription();

    @Override
    public ReferenceStatement getReference();

    @Override
    public Collection<? extends RevisionStatement> getRevisions();

    @Override
    public Collection<? extends ExtensionStatement> getExtensions();

    @Override
    public Collection<? extends IdentityStatement> getIdentities();

    @Override
    public Collection<? extends TypedefStatement> getTypedefs();

    @Override
    public Collection<? extends GroupingStatement> getGroupings();

    @Override
    public Collection<? extends DataDefinitionStatement<?>> getDataDefinitions();

    @Override
    public Collection<? extends AugmentStatement> getAugments();

    @Override
    public Collection<? extends RpcStatement> getRpcs();

    @Override
    public Collection<? extends NotificationStatement> getNotifications();

    @Override
    public Collection<? extends DeviationStatement> getDeviations();
}

