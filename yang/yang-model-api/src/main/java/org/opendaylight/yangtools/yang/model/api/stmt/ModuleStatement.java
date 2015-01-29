package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface ModuleStatement extends
    DeclaredStatement<String>,
    ModuleHeaderGroup,
    LinkageGroup,
    MetaGroup,
    RevisionGroup,
    BodyGroup

    {

    String getName();

    @Override
    public YangVersionStatement getYangVersion();

    @Override
    public NamespaceStatement getNamespace();

    @Override
    public PrefixStatement getPrefix();

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
    public Iterable<? extends DataDefinitionStatement> getDataDefinitions();

    @Override
    public Iterable<? extends AugmentStatement> getAugments();

    @Override
    public Iterable<? extends RpcStatement> getRpcs();

    @Override
    public Iterable<? extends NotificationStatement> getNotifications();

    @Override
    public Iterable<? extends DeviationStatement> getDeviations();


}
