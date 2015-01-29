package org.opendaylight.yangtools.yang.model.api.stmt;


@Rfc6020AbnfRule("body-stmts")
public interface BodyGroup extends DataDefinitionContainer.WithReusableDefinitions {

    Iterable<? extends ExtensionStatement> getExtensions();
    Iterable<? extends FeatureStatement> getFeatures();
    Iterable<? extends IdentityStatement> getIdentities();


    @Override
    Iterable<? extends TypedefStatement> getTypedefs();

    @Override
    Iterable<? extends GroupingStatement> getGroupings();

    @Override
    Iterable<? extends DataDefinitionStatement> getDataDefinitions();

    Iterable<? extends AugmentStatement> getAugments();
    Iterable<? extends RpcStatement> getRpcs();
    Iterable<? extends NotificationStatement> getNotifications();
    Iterable<? extends DeviationStatement> getDeviations();

}
