package org.opendaylight.yangtools.yang.model.api.stmt;


@Rfc6020AbnfRule("*(if-feature-stmt)")
public interface ConditionalFeature {

    Iterable<? extends IfFeatureStatement> getIfFeatures();

}
