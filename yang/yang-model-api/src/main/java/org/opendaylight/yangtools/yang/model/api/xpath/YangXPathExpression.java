package org.opendaylight.yangtools.yang.model.api.xpath;

// FIXME: should this include evaluate(EffectiveStatement/SchemaNode) or similar?
public interface YangXPathExpression {

    YangExpr getRootExpr();
}
