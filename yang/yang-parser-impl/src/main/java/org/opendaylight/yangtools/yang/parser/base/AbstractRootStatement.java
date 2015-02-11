package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionGroup;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractRootStatement<T extends DeclaredStatement<String>> extends AbstractDeclaredStatement<String>
        implements LinkageGroup, MetaGroup, RevisionGroup, BodyGroup {

    protected AbstractRootStatement(StmtContext<String, T,?> context) {
        super(context);
    }

    @Override
    public final Iterable<? extends ImportStatement> getImports() {
        return allDeclared(ImportStatement.class);
    }

    @Override
    public final Iterable<? extends IncludeStatement> getIncludes() {
        return allDeclared(IncludeStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public final OrganizationStatement getOrganization() {
        return firstDeclared(OrganizationStatement.class);
    }

    @Override
    public final ContactStatement getContact() {
        return firstDeclared(ContactStatement.class);
    }

    @Override
    public final Iterable<? extends RevisionStatement> getRevisions() {
        return allDeclared(RevisionStatement.class);
    }

    @Override
    public final Iterable<? extends ExtensionStatement> getExtensions() {
        return allDeclared(ExtensionStatement.class);
    }

    @Override
    public final Iterable<? extends FeatureStatement> getFeatures() {
        return allDeclared(FeatureStatement.class);
    }

    @Override
    public final Iterable<? extends IdentityStatement> getIdentities() {
        return allDeclared(IdentityStatement.class);
    }

    @Override
    public Iterable<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Override
    public Iterable<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Override
    public Iterable<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }

    @Override
    public final Iterable<? extends AugmentStatement> getAugments() {
        return allDeclared(AugmentStatement.class);
    }

    @Override
    public final Iterable<? extends RpcStatement> getRpcs() {
        return allDeclared(RpcStatement.class);
    }

    @Override
    public final Iterable<? extends NotificationStatement> getNotifications() {
        return allDeclared(NotificationStatement.class);
    }

    @Override
    public final Iterable<? extends DeviationStatement> getDeviations() {
        return allDeclared(DeviationStatement.class);
    }
}
