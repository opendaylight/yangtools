package org.opendaylight.yangtools.yang.data.impl.codec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

public class InstanceIdentifierForXmlCodec {

    private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\[(.*?)\\]");
    public static final String SQUOTE = "'";
    public static final String DQUOTE = "\"";

    public static final InstanceIdentifierForXmlCodec INSTANCE_IDENTIFIER_FOR_XML_CODEC = new InstanceIdentifierForXmlCodec();

    public InstanceIdentifierForXmlCodec() {
    }

    public InstanceIdentifier deserialize(Element element, SchemaContext schemaContext) {
        Preconditions.checkNotNull(element, "Value of element for deserialization can't be null");
        Preconditions.checkNotNull(schemaContext,
                "Schema context for deserialization of instance identifier type can't be null");

        String valueTrimmed = element.getTextContent().trim();
        if (!valueTrimmed.startsWith("/")) {
            return null;
        }
        String[] xPathParts = valueTrimmed.split("/");
        if (xPathParts.length < 2) { // must be at least "/pr:node"
            return null;
        }
        List<PathArgument> result = new ArrayList<>();
        for (int i = 1; i < xPathParts.length; i++) {
            String xPathPartTrimmed = xPathParts[i].trim();

            PathArgument pathArgument = toPathArgument(xPathPartTrimmed, element, schemaContext);
            if (pathArgument != null) {
                result.add(pathArgument);
            }
        }
        return new InstanceIdentifier(result);
    }

    public Element serialize(InstanceIdentifier data, Element element) {
        Preconditions.checkNotNull(data, "Variable should contain instance of instance identifier and can't be null");
        Preconditions.checkNotNull(element, "DOM element can't be null");
        Map<String, String> prefixes = new HashMap<>();
        StringBuilder textContent = new StringBuilder();
        for (PathArgument pathArgument : data.getPath()) {
            textContent.append("/");
            writeIdentifierWithNamespacePrefix(element, textContent, pathArgument.getNodeType(), prefixes);
            if (pathArgument instanceof NodeIdentifierWithPredicates) {
                Map<QName, Object> predicates = ((NodeIdentifierWithPredicates) pathArgument).getKeyValues();

                for (QName keyValue : predicates.keySet()) {
                    String predicateValue = String.valueOf(predicates.get(keyValue));
                    textContent.append("[");
                    writeIdentifierWithNamespacePrefix(element, textContent, keyValue, prefixes);
                    textContent.append("='");
                    textContent.append(predicateValue);
                    textContent.append("'");
                    textContent.append("]");
                }
            } else if (pathArgument instanceof NodeWithValue) {
                textContent.append("[.='");
                textContent.append(((NodeWithValue) pathArgument).getValue());
                textContent.append("'");
                textContent.append("]");
            }
        }
        element.setTextContent(textContent.toString());
        return element;
    }

    private String getIdAndPrefixAsStr(String pathPart) {
        int predicateStartIndex = pathPart.indexOf("[");
        return predicateStartIndex == -1 ? pathPart : pathPart.substring(0, predicateStartIndex);
    }

    private PathArgument toPathArgument(String xPathArgument, Element element, SchemaContext schemaContext) {

        QName mainQName = toIdentity(xPathArgument, element, schemaContext);

        // predicates
        QName currentQName = mainQName;
        List<String> predicatesStr = new ArrayList<>();
        Map<QName, Object> predicates = new HashMap<>();
        Matcher matcher = PREDICATE_PATTERN.matcher(xPathArgument);
        while (matcher.find()) {
            predicatesStr.add(matcher.group(1).trim());
        }
        for (String predicateStr : predicatesStr) {
            int indexOfEqualityMark = predicateStr.indexOf("=");
            if (indexOfEqualityMark != -1) {
                String predicateValue = toPredicateValue(predicateStr.substring(indexOfEqualityMark + 1));
                if (predicateStr.startsWith(".")) { // it is leaf-list
                    if (predicateValue == null) {
                        return null;
                    }
                } else {
                    currentQName = toIdentity(predicateStr.substring(0, indexOfEqualityMark), element, schemaContext);
                    if (currentQName == null || predicateValue == null) {
                        return null;
                    }
                }
                predicates.put(currentQName, predicateValue);
            }
        }

        if (predicates.isEmpty()) {
            return new InstanceIdentifier.NodeIdentifier(mainQName);
        } else {
            return new InstanceIdentifier.NodeIdentifierWithPredicates(mainQName, predicates);
        }

    }

    private QName toIdentity(String xPathArgument, Element element, SchemaContext schemaContext) {
        String xPathPart = getIdAndPrefixAsStr(xPathArgument);
        String xPathPartTrimmed = xPathPart.trim();
        if (xPathPartTrimmed.isEmpty()) {
            return null;
        }
        String[] prefixAndIdentifier = xPathPartTrimmed.split(":");
        // it is not "prefix:value"
        if (prefixAndIdentifier.length != 2) {
            return null;
        }
        String prefix = prefixAndIdentifier[0].trim();
        String identifier = prefixAndIdentifier[1].trim();
        if (prefix.isEmpty() || identifier.isEmpty()) {
            return null;
        }
        URI namespace = null;
        String namespaceStr = null;
        try {
            namespaceStr = element.lookupNamespaceURI(prefix);
            namespace = new URI(namespaceStr);
        } catch (URISyntaxException e) {
            new Exception("It wasn't possible to convert " + namespaceStr + " to URI object.");
        } catch (NullPointerException e) {
            new Exception("I wasn't possible to get namespace for prefix " + prefix);
        }
        Module youngestModule = findYoungestModuleByNamespace(schemaContext, namespace);

        return QName.create(namespace, youngestModule.getRevision(), identifier);
    }

    private Module findYoungestModuleByNamespace(SchemaContext schemaContext, URI namespace) {
        Module result = null;
        for (Module module : schemaContext.getModules()) {
            if (namespace.equals(module.getNamespace())) {
                if (result != null) {
                    if (module.getRevision().after(result.getRevision())) {
                        result = module;
                    }
                } else {
                    result = module;
                }
            }
        }
        return result;
    }

    private static String toPredicateValue(String predicatedValue) {
        String predicatedValueTrimmed = predicatedValue.trim();
        if ((predicatedValueTrimmed.startsWith(DQUOTE) || predicatedValueTrimmed.startsWith(SQUOTE))
                && (predicatedValueTrimmed.endsWith(DQUOTE) || predicatedValueTrimmed.endsWith(SQUOTE))) {
            return predicatedValueTrimmed.substring(1, predicatedValueTrimmed.length() - 1);
        }
        return null;
    }

    private static void writeIdentifierWithNamespacePrefix(Element element, StringBuilder textContent, QName qName,
            Map<String, String> prefixes) {
        String namespace = qName.getNamespace().toString();
        String prefix = prefixes.get(namespace);
        if (prefix == null) {
            prefix = qName.getPrefix();
            if (prefix == null || prefix.isEmpty() || prefixes.containsValue(prefix)) {
                prefix = generateNewPrefix(prefixes.values());
            }
        }

        element.setAttribute("xmlns:" + prefix, namespace.toString());
        textContent.append(prefix);
        prefixes.put(namespace, prefix);

        textContent.append(":");
        textContent.append(qName.getLocalName());
    }

    private static String generateNewPrefix(Collection<String> prefixes) {
        StringBuilder result = null;
        Random random = new Random();
        do {
            result = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int randomNumber = 0x61 + (Math.abs(random.nextInt()) % 26);
                result.append(Character.toChars(randomNumber));
            }
        } while (prefixes.contains(result.toString()));

        return result.toString();
    }
}
