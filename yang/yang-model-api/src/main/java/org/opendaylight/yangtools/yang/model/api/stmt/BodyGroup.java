package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;

@Rfc6020AbnfRule("body-stmts")
public interface BodyGroup extends DataDefinitionContainer.WithReusableDefinitions {

    Collection<? extends ExtensionStatement> getExtensions();
    Collection<? extends FeatureStatement> getFeatures();
    Collection<? extends IdentityStatement> getIdentities();


    @Override
    Collection<? extends TypedefStatement> getTypedefs();

    @Override
    Collection<? extends GroupingStatement> getGroupings();

    @Override
    Collection<? extends DataDefinitionStatement<?>> getDataDefinitions();

    Collection<? extends AugmentStatement> getAugments();
    Collection<? extends RpcStatement> getRpcs();
    Collection<? extends NotificationStatement> getNotifications();
    Collection<? extends DeviationStatement> getDeviations();

}
