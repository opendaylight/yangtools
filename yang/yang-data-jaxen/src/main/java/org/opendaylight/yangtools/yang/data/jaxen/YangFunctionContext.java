/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Verify;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.UnresolvableException;
import org.jaxen.XPathFunctionContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;

/**
 * A {@link FunctionContext} which contains also YANG-specific functions current(), re-match(), deref(),
 * derived-from(), derived-from-or-self(), enum-value() and bit-is-set().
 */
final class YangFunctionContext implements FunctionContext {
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Double DOUBLE_NAN = Double.NaN;

    // Core XPath functions, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final FunctionContext XPATH_FUNCTION_CONTEXT = new XPathFunctionContext(false);

    // Singleton instance of reuse
    private static final YangFunctionContext INSTANCE = new YangFunctionContext();

    private YangFunctionContext() {
    }

    static YangFunctionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Function getFunction(final String namespaceURI, final String prefix, final String localName)
            throws UnresolvableException {
        if (prefix == null) {
            switch (localName) {
                case "bit-is-set":
                    return YangFunctionContext::bitIsSet;
                case "current":
                    return YangFunctionContext::current;
                case "deref":
                    return YangFunctionContext::deref;
                case "derived-from":
                    return YangFunctionContext::derivedFrom;
                case "derived-from-or-self":
                    return YangFunctionContext::derivedFromOrSelf;
                case "enum-value":
                    return YangFunctionContext::enumValueFunction;
                case "re-match":
                    return YangFunctionContext::reMatch;
                default:
                    break;
            }
        }

        return XPATH_FUNCTION_CONTEXT.getFunction(namespaceURI, prefix, localName);
    }

    // bit-is-set(node-set nodes, string bit-name) function as per
    // https://tools.ietf.org/html/rfc7950#section-10.6.1
    private static boolean bitIsSet(final Context context, final List<?> args) throws FunctionCallException {
        if (args == null || args.size() != 1) {
            throw new FunctionCallException("bit-is-set() takes two arguments: node-set nodes, string bit-name");
        }

        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("Argument bit-name of bit-is-set() function should be a String");
        }

        final String bitName = (String) args.get(0);

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final TypedDataSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(currentNodeContext);

        final TypeDefinition<?> nodeType = correspondingSchemaNode.getType();
        if (!(nodeType instanceof BitsTypeDefinition)) {
            return false;
        }

        final Object nodeValue = currentNodeContext.getNode().getValue();
        if (!(nodeValue instanceof Set)) {
            return false;
        }

        final BitsTypeDefinition bitsType = (BitsTypeDefinition) nodeType;
        Preconditions.checkState(containsBit(bitsType, bitName), "Bit %s does not belong to bits %s.", bitName,
            bitsType);
        return ((Set<?>)nodeValue).contains(bitName);
    }

    // current() function, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static NormalizedNodeContext current(final Context context, final List<?> args)
            throws FunctionCallException {
        if (!args.isEmpty()) {
            throw new FunctionCallException("current() takes no arguments.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());
        return (NormalizedNodeContext) context;
    }

    // deref(node-set nodes) function as per https://tools.ietf.org/html/rfc7950#section-10.3.1
    private static NormalizedNode<?, ?> deref(final Context context, final List<?> args) throws FunctionCallException {
        if (!args.isEmpty()) {
            throw new FunctionCallException("deref() takes only one argument: node-set nodes.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());
        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final TypedDataSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(currentNodeContext);

        final Object nodeValue = currentNodeContext.getNode().getValue();
        final TypeDefinition<?> type = correspondingSchemaNode.getType();
        if (type instanceof InstanceIdentifierTypeDefinition) {
            return nodeValue instanceof YangInstanceIdentifier
                    ? getNodeReferencedByInstanceIdentifier((YangInstanceIdentifier) nodeValue, currentNodeContext)
                            : null;
        }
        if (type instanceof LeafrefTypeDefinition) {
            final PathExpression xpath = ((LeafrefTypeDefinition) type).getPathStatement();
            return getNodeReferencedByLeafref(xpath, currentNodeContext, getSchemaContext(currentNodeContext),
                correspondingSchemaNode, nodeValue);
        }
        return null;
    }

    // derived-from(node-set nodes, string identity) function as per https://tools.ietf.org/html/rfc7950#section-10.4.1
    private static boolean derivedFrom(final Context context, final List<?> args) throws FunctionCallException {
        final Entry<IdentitySchemaNode, IdentitySchemaNode> ids = commonDerivedFrom("derived-from", context, args);
        return ids != null && isAncestorOf(ids.getKey(), ids.getValue());
    }

    // derived-from-or-self(node-set nodes, string identity) function as per
    // https://tools.ietf.org/html/rfc7950#section-10.4.2
    private static boolean derivedFromOrSelf(final Context context, final List<?> args) throws FunctionCallException {
        final Entry<IdentitySchemaNode, IdentitySchemaNode> ids = commonDerivedFrom("derived-from-or-self", context,
            args);
        return ids != null && (ids.getValue().equals(ids.getKey()) || isAncestorOf(ids.getKey(), ids.getValue()));
    }

    private static @Nullable Entry<IdentitySchemaNode, IdentitySchemaNode> commonDerivedFrom(final String functionName,
            final Context context, final List<?> args) throws FunctionCallException {
        if (args == null || args.size() != 1) {
            throw new FunctionCallException(functionName + "() takes two arguments: node-set nodes, string identity");
        }
        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("Argument 'identity' of " + functionName
                + "() function should be a String.");
        }
        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final TypedDataSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(currentNodeContext);

        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        return correspondingSchemaNode.getType() instanceof IdentityrefTypeDefinition
                && currentNodeContext.getNode().getValue() instanceof QName ? new SimpleImmutableEntry<>(
                        getIdentitySchemaNodeFromString((String) args.get(0), schemaContext, correspondingSchemaNode),
                        getIdentitySchemaNodeFromQName((QName) currentNodeContext.getNode().getValue(), schemaContext))
                        : null;
    }

    // enum-value(node-set nodes) function as per https://tools.ietf.org/html/rfc7950#section-10.5.1
    private static Object enumValueFunction(final Context context, final List<?> args) throws FunctionCallException {
        if (!args.isEmpty()) {
            throw new FunctionCallException("enum-value() takes one argument: node-set nodes.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final TypedDataSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(currentNodeContext);

        final TypeDefinition<?> nodeType = correspondingSchemaNode.getType();
        if (!(nodeType instanceof EnumTypeDefinition)) {
            return DOUBLE_NAN;
        }

        final Object nodeValue = currentNodeContext.getNode().getValue();
        if (!(nodeValue instanceof String)) {
            return DOUBLE_NAN;
        }

        final EnumTypeDefinition enumerationType = (EnumTypeDefinition) nodeType;
        final String enumName = (String) nodeValue;

        return getEnumValue(enumerationType, enumName);
    }

    // re-match(string subject, string pattern) function as per https://tools.ietf.org/html/rfc7950#section-10.2.1
    private static boolean reMatch(final Context context, final List<?> args) throws FunctionCallException {
        if (args == null || args.size() != 2) {
            throw new FunctionCallException("re-match() takes two arguments: string subject, string pattern.");
        }
        final Object subject = args.get(0);
        if (!(subject instanceof String)) {
            throw new FunctionCallException("First argument of re-match() should be a String.");
        }
        final Object pattern = args.get(1);
        if (!(pattern instanceof String)) {
            throw new FunctionCallException("Second argument of re-match() should be a String.");
        }

        return ((String) subject).matches(RegexUtils.getJavaRegexFromXSD((String) pattern));
    }

    private static boolean isAncestorOf(final IdentitySchemaNode identity, final IdentitySchemaNode descendant) {
        for (IdentitySchemaNode base : descendant.getBaseIdentities()) {
            if (identity.equals(base) || isAncestorOf(identity, base)) {
                return true;
            }
        }
        return false;
    }

    private static IdentitySchemaNode getIdentitySchemaNodeFromQName(final QName identityQName,
            final SchemaContext schemaContext) {
        final Optional<Module> module = schemaContext.findModule(identityQName.getModule());
        Preconditions.checkArgument(module.isPresent(), "Module for %s not found", identityQName);
        return findIdentitySchemaNodeInModule(module.get(), identityQName);
    }

    private static IdentitySchemaNode getIdentitySchemaNodeFromString(final String identity,
            final SchemaContext schemaContext, final TypedDataSchemaNode correspondingSchemaNode) {
        final List<String> identityPrefixAndName = COLON_SPLITTER.splitToList(identity);
        final Module module = schemaContext.findModule(correspondingSchemaNode.getQName().getModule()).get();
        if (identityPrefixAndName.size() == 2) {
            // prefix of local module
            if (identityPrefixAndName.get(0).equals(module.getPrefix())) {
                return findIdentitySchemaNodeInModule(module, QName.create(module.getQNameModule(),
                        identityPrefixAndName.get(1)));
            }

            // prefix of imported module
            for (final ModuleImport moduleImport : module.getImports()) {
                if (identityPrefixAndName.get(0).equals(moduleImport.getPrefix())) {
                    final Module importedModule = schemaContext.findModule(moduleImport.getModuleName(),
                        moduleImport.getRevision()).get();
                    return findIdentitySchemaNodeInModule(importedModule, QName.create(
                        importedModule.getQNameModule(), identityPrefixAndName.get(1)));
                }
            }

            throw new IllegalArgumentException(String.format("Cannot resolve prefix '%s' from identity '%s'.",
                    identityPrefixAndName.get(0), identity));
        }

        if (identityPrefixAndName.size() == 1) {
            // without prefix
            return findIdentitySchemaNodeInModule(module, QName.create(module.getQNameModule(),
                    identityPrefixAndName.get(0)));
        }

        throw new IllegalArgumentException(String.format("Malformed identity argument: %s.", identity));
    }

    private static IdentitySchemaNode findIdentitySchemaNodeInModule(final Module module, final QName identityQName) {
        for (final IdentitySchemaNode id : module.getIdentities()) {
            if (identityQName.equals(id.getQName())) {
                return id;
            }
        }

        throw new IllegalArgumentException(String.format("Identity %s does not have a corresponding"
                    + " identity schema node in the module %s.", identityQName, module));
    }

    private static NormalizedNode<?, ?> getNodeReferencedByInstanceIdentifier(final YangInstanceIdentifier path,
            final NormalizedNodeContext currentNodeContext) {
        final NormalizedNodeNavigator navigator = (NormalizedNodeNavigator) currentNodeContext.getNavigator();
        final NormalizedNode<?, ?> rootNode = navigator.getDocument().getRootNode();
        final List<PathArgument> pathArguments = path.getPathArguments();
        if (pathArguments.get(0).getNodeType().equals(rootNode.getNodeType())) {
            final List<PathArgument> relPath = pathArguments.subList(1, pathArguments.size());
            final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(rootNode, relPath);
            if (possibleNode.isPresent()) {
                return possibleNode.get();
            }
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByLeafref(final PathExpression xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedDataSchemaNode correspondingSchemaNode, final Object nodeValue) {
        final NormalizedNode<?, ?> referencedNode = xpath.isAbsolute() ? getNodeReferencedByAbsoluteLeafref(xpath,
                currentNodeContext, schemaContext, correspondingSchemaNode) : getNodeReferencedByRelativeLeafref(xpath,
                currentNodeContext, schemaContext, correspondingSchemaNode);

        if (referencedNode instanceof LeafSetNode) {
            return getReferencedLeafSetEntryNode((LeafSetNode<?>) referencedNode, nodeValue);
        }

        if (referencedNode instanceof LeafNode && referencedNode.getValue().equals(nodeValue)) {
            return referencedNode;
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByAbsoluteLeafref(final PathExpression xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedDataSchemaNode correspondingSchemaNode) {
        final LeafrefXPathStringParsingPathArgumentBuilder builder = new LeafrefXPathStringParsingPathArgumentBuilder(
                xpath.getOriginalString(), schemaContext, correspondingSchemaNode, currentNodeContext);
        final List<PathArgument> pathArguments = builder.build();
        final NormalizedNodeNavigator navigator = (NormalizedNodeNavigator) currentNodeContext.getNavigator();
        final NormalizedNode<?, ?> rootNode = navigator.getDocument().getRootNode();
        if (pathArguments.get(0).getNodeType().equals(rootNode.getNodeType())) {
            final List<PathArgument> relPath = pathArguments.subList(1, pathArguments.size());
            final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(rootNode, relPath);
            if (possibleNode.isPresent()) {
                return possibleNode.get();
            }
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByRelativeLeafref(final PathExpression xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedDataSchemaNode correspondingSchemaNode) {
        NormalizedNodeContext relativeNodeContext = currentNodeContext;
        final StringBuilder xPathStringBuilder = new StringBuilder(xpath.getOriginalString());
        // strip the relative path of all ../ at the beginning
        while (xPathStringBuilder.indexOf("../") == 0) {
            xPathStringBuilder.delete(0, 3);
            relativeNodeContext = relativeNodeContext.getParent();
        }

        // add / to the beginning of the path so that it can be processed the same way as an absolute path
        xPathStringBuilder.insert(0, '/');
        final LeafrefXPathStringParsingPathArgumentBuilder builder = new LeafrefXPathStringParsingPathArgumentBuilder(
                xPathStringBuilder.toString(), schemaContext, correspondingSchemaNode, currentNodeContext);
        final List<PathArgument> pathArguments = builder.build();
        final NormalizedNode<?, ?> relativeNode = relativeNodeContext.getNode();
        final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(relativeNode, pathArguments);
        if (possibleNode.isPresent()) {
            return possibleNode.get();
        }

        return null;
    }

    private static LeafSetEntryNode<?> getReferencedLeafSetEntryNode(final LeafSetNode<?> referencedNode,
            final Object currentNodeValue) {
        for (final LeafSetEntryNode<?> entryNode : referencedNode.getValue()) {
            if (currentNodeValue.equals(entryNode.getValue())) {
                return entryNode;
            }
        }

        return null;
    }


    private static boolean containsBit(final BitsTypeDefinition bitsType, final String bitName) {
        for (BitsTypeDefinition.Bit bit : bitsType.getBits()) {
            if (bitName.equals(bit.getName())) {
                return true;
            }
        }

        return false;
    }

    private static int getEnumValue(final EnumTypeDefinition enumerationType, final String enumName) {
        for (final EnumTypeDefinition.EnumPair enumPair : enumerationType.getValues()) {
            if (enumName.equals(enumPair.getName())) {
                return enumPair.getValue();
            }
        }

        throw new IllegalStateException(String.format("Enum %s does not belong to enumeration %s.",
                enumName, enumerationType));
    }

    private static SchemaContext getSchemaContext(final NormalizedNodeContext normalizedNodeContext) {
        final ContextSupport contextSupport = normalizedNodeContext.getContextSupport();
        Verify.verify(contextSupport instanceof NormalizedNodeContextSupport, "Unhandled context support %s",
                contextSupport.getClass());
        return ((NormalizedNodeContextSupport) contextSupport).getSchemaContext();
    }

    private static TypedDataSchemaNode getCorrespondingTypedSchemaNode(final NormalizedNodeContext currentNodeContext) {
        final DataSchemaNode schemaNode = currentNodeContext.getSchema().getDataSchemaNode();
        Preconditions.checkState(schemaNode instanceof TypedDataSchemaNode, "Node %s must be a leaf or a leaf-list.",
                currentNodeContext.getNode());
        return (TypedDataSchemaNode) schemaNode;
    }
}
