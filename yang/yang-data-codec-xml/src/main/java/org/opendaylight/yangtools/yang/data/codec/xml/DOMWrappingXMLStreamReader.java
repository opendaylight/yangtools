package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DOMWrappingXMLStreamReader implements XMLStreamReader, NamespaceContext, XMLStreamConstants {

    private final static int INT_SPACE = 0x0020;

    final private static int MASK_GET_TEXT =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
                    | (1 << COMMENT) | (1 << DTD) | (1 << ENTITY_REFERENCE);

    final private static int MASK_GET_TEXT_XXX =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT);

    final private static int MASK_GET_ELEMENT_TEXT =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
                    | (1 << ENTITY_REFERENCE);

    final private static int MASK_TYPED_ACCESS_BINARY =
            (1 << START_ELEMENT) //  note: END_ELEMENT handled separately
                    | (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE);

    /**
     * Current state not START_ELEMENT, should be
     */
    private final static int ERR_STATE_NOT_START_ELEM = 1;

    /**
     * Current state not START_ELEMENT or END_ELEMENT, should be
     */
    private final static int ERR_STATE_NOT_ELEM = 2;

    /**
     * Current state not PROCESSING_INSTRUCTION
     */
    private final static int ERR_STATE_NOT_PI = 3;

    /**
     * Current state not one where getText() can be used
     */
    private final static int ERR_STATE_NOT_TEXTUAL = 4;

    /**
     * Current state not one where getTextXxx() can be used
     */
    private final static int ERR_STATE_NOT_TEXTUAL_XXX = 5;

    private final static int ERR_STATE_NOT_TEXTUAL_OR_ELEM = 6;

    private final static int ERR_STATE_NO_LOCALNAME = 7;

    private int depth = 0;

    /**
     * In coalescing mode, we may need to combine textual content
     * from multiple adjacent nodes. Since we shouldn't be modifying
     * the underlying DOM tree, need to accumulate it into a temporary
     * variable
     */
    private String coalescedText;

    private int currEvent = START_DOCUMENT;

    private final Node rootNode;

    /**
     * Whether stream reader is to be namespace aware (as per property
     * {@link XMLInputFactory#IS_NAMESPACE_AWARE}) or not
     */
    private boolean _cfgNsAware = true; // chceme to konfigurovat alebo to bude vzdy aktivovane ? zatial dam true

    /**
     * Whether stream reader is to coalesce adjacent textual
     * (CHARACTERS, SPACE, CDATA) events (as per property
     * {@link XMLInputFactory#IS_COALESCING}) or not
     */
    private boolean coalescing = true; // chceme to konfigurovat alebo to bude vzdy aktivovane ? zatial dam true

    /**
     * By default we do not force interning of names: can be
     * reset by sub-classes.
     */
    private boolean _cfgInternNames = false;

    /**
     * By default we do not force interning of namespace URIs: can be
     * reset by sub-classes.
     */
    private boolean _cfgInternNsURIs = false;

    /**
     * Current node is the DOM node that contains information
     * regarding the current event.
     */
    private Node currNode;
    
    /* DOM, alas, does not distinguish between namespace declarations
     * and attributes (due to its roots prior to XML namespaces?).
     * Because of this, two lists need to be separated. Since this
     * information is often not needed, it will be lazily generated.
     */

    /**
     * Lazily instantiated List of all actual attributes for the
     * current (start) element, NOT including namespace declarations.
     * As such, elements are {@link org.w3c.dom.Attr} instances.
     *<p>
     */
    private List<Node> attrList = null;

    /**
     * Lazily instantiated String pairs of all namespace declarations for the
     * current (start/end) element. String pair means that for each
     * declarations there are two Strings in the list: first one is prefix
     * (empty String for the default namespace declaration), and second
     * URI it is bound to.
     */
    private List<String> nsDeclList = null;

    private TextBuffer _textBuffer = new TextBuffer();

    private DOMWrappingXMLStreamReader(final DOMSource src) throws XMLStreamException {
        Node treeRoot = src.getNode();
        if (treeRoot == null) {
            throw new IllegalArgumentException("Can not pass null Node for constructing a DOM-based XMLStreamReader");
        }
        //_cfgNsAware = nsAware;
        //coalescing = coalescing;
        //_systemId = src.getSystemId();

        /* Ok; we need a document node; or an element node; or a document
         * fragment node.
         */
        switch (treeRoot.getNodeType()) {
            case Node.DOCUMENT_NODE: // fine
            /* Should try to find encoding, version and stand-alone
             * settings... but is there a standard way of doing that?
             */
            case Node.ELEMENT_NODE: // can make sub-tree... ok
                // But should we skip START/END_DOCUMENT? For now, let's not

            case Node.DOCUMENT_FRAGMENT_NODE: // as with element...

                // Above types are fine
                break;

            default: // other Nodes not usable
                throw new XMLStreamException("Can not create an XMLStreamReader for a DOM node of type "
                        + treeRoot.getClass());
        }

        rootNode = currNode = treeRoot;
    }

    public static DOMWrappingXMLStreamReader createFrom(final DOMSource xmlSource) throws XMLStreamException {
        return new DOMWrappingXMLStreamReader(xmlSource);
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null; // toto nepotrebujeme
    }

    @Override
    public int next() throws XMLStreamException {
        coalescedText = null;

        /* For most events, we just need to find the next sibling; and
         * that failing, close the parent element. But there are couple
         * of special cases, which are handled first:
         */
        switch (currEvent) {

            case START_DOCUMENT: // initial state
            /* What to do here depends on what kind of node we started
             * with...
             */
                switch (currNode.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                    case Node.DOCUMENT_FRAGMENT_NODE:
                        // For doc, fragment, need to find first child
                        currNode = currNode.getFirstChild();
                        // as per [WSTX-259], need to handle degenerate case of empty fragment, too
                        if (currNode == null) {
                            return (currEvent = END_DOCUMENT);
                        }
                        break;

                    case Node.ELEMENT_NODE:
                        // For element, curr node is fine:
                        return (currEvent = START_ELEMENT);

                    default:
                        throw new XMLStreamException("Internal error: unexpected DOM root node type "+currNode.getNodeType()+" for node '"+currNode+"'");
                }
                break;

            case END_DOCUMENT: // end reached: should not call!
                throw new java.util.NoSuchElementException("Can not call next() after receiving END_DOCUMENT");

            case START_ELEMENT: // element returned, need to traverse children, if any
                ++depth;
                attrList = null; // so it will not get reused accidentally
            {
                Node firstChild = currNode.getFirstChild();
                if (firstChild == null) { // empty? need to return virtual END_ELEMENT
                    /* Note: need not clear namespace declarations, because
                     * it'll be the same as for the start elem!
                     */
                    return (currEvent = END_ELEMENT);
                }
                nsDeclList = null;

                /* non-empty is easy: let's just swap curr node, and
                 * fall through to regular handling
                 */
                currNode = firstChild;
                break;
            }

            case END_ELEMENT:

                --depth;
                // Need to clear these lists
                attrList = null;
                nsDeclList = null;

            /* One special case: if we hit the end of children of
             * the root element (when tree constructed with Element,
             * instead of Document or DocumentFragment). If so, it'll
             * be END_DOCUMENT:
             */
                if (currNode == rootNode) {
                    return (currEvent = END_DOCUMENT);
                }
                // Otherwise need to fall through to default handling:

            default:
            /* For anything else, we can and should just get the
             * following sibling.
             */
            {
                Node next = currNode.getNextSibling();
                // If sibling, let's just assign and fall through
                if (next != null) {
                    currNode = next;
                    break;
                }
                /* Otherwise, need to climb up _the stack and either
                 * return END_ELEMENT (if parent is element) or
                 * END_DOCUMENT (if not; needs to be root, then)
                 */
                currNode = currNode.getParentNode();
                int type = currNode.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    return (currEvent = END_ELEMENT);
                }
                // Let's do sanity check; should really be Doc/DocFragment
                if (currNode != rootNode ||
                        (type != Node.DOCUMENT_NODE && type != Node.DOCUMENT_FRAGMENT_NODE)) {
                    throw new XMLStreamException("Internal error: non-element parent node ("+type+") that is not the initial root node");
                }
                return (currEvent = END_DOCUMENT);
            }
        }

        // Ok, need to determine current node type:
        switch (currNode.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
                if (coalescing) {
                    coalesceText(CDATA);
                } else {
                    currEvent = CDATA;
                }
                break;
            case Node.COMMENT_NODE:
                currEvent = COMMENT;
                break;
            case Node.DOCUMENT_TYPE_NODE:
                currEvent = DTD;
                break;
            case Node.ELEMENT_NODE:
                currEvent = START_ELEMENT;
                break;
            case Node.ENTITY_REFERENCE_NODE:
                currEvent = ENTITY_REFERENCE;
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                currEvent = PROCESSING_INSTRUCTION;
                break;
            case Node.TEXT_NODE:
                if (coalescing) {
                    coalesceText(CHARACTERS);
                } else {
                    currEvent = CHARACTERS;
                }
                break;

            // Should not get other nodes (notation/entity decl., attr)
            case Node.ATTRIBUTE_NODE:
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
                throw new XMLStreamException("Internal error: unexpected DOM node type "+currNode.getNodeType()+" (attr/entity/notation?), for node '"+currNode+"'");

            default:
                throw new XMLStreamException("Internal error: unrecognized DOM node type "+currNode.getNodeType()+", for node '"+currNode+"'");
        }

        return currEvent;
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        // NOOP toto nepotrebujeme
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if(getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException(
                    "parser must be on START_ELEMENT to read next text", getLocation());
        }
        int eventType = next();
        StringBuffer content = new StringBuffer();
        while(eventType != XMLStreamConstants.END_ELEMENT ) {
            if(eventType == XMLStreamConstants.CHARACTERS
                    || eventType == XMLStreamConstants.CDATA
                    || eventType == XMLStreamConstants.SPACE
                    || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                content.append(getText());
            } else if(eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT) {
                // skipping
            } else if(eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException("unexpected end of document when reading element text content");
            } else if(eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "elementGetText() function expects text only elment but START_ELEMENT was encountered.", getLocation());
            } else {
                throw new XMLStreamException(
                        "Unexpected event type "+ eventType, getLocation());
            }
            eventType = next();
        }
        return content.toString();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
                // skip whitespace
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT
                ) {
            eventType = next();
        }

        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException(
                    "found: " + getEventTypeString(eventType)
                            + ", expected " + getEventTypeString(XMLStreamConstants.START_ELEMENT)
                            + " or " + getEventTypeString(XMLStreamConstants.END_ELEMENT),
                    getLocation());
        }

        return eventType;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return currEvent != END_DOCUMENT;
    }

    @Override
    public void close() throws XMLStreamException {
        // Since DOM tree has no real input source, nothing to do
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix.length() == 0) { // def NS
            return currNode.lookupNamespaceURI(null);
        }

        return currNode.lookupNamespaceURI(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        String prefix = currNode.lookupPrefix(namespaceURI);
        if (prefix == null) { // maybe default NS?
            String defURI = currNode.lookupNamespaceURI(null);
            if (defURI != null && defURI.equals(namespaceURI)) {
                return "";
            }
        }
        return prefix;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return null; // toto nepotrebujeme
    }

    @Override
    public boolean isStartElement() {
        return currEvent == START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return currEvent == END_ELEMENT;
    }

    @Override
    public boolean isCharacters() {
        return currEvent == CHARACTERS;
    }

    @Override
    public boolean isWhiteSpace() {
        // toto potrebujeme kvoli metode nextTag()
        if (currEvent == CHARACTERS || currEvent == CDATA) {
            String text = getText();
            for (int i = 0, len = text.length(); i < len; ++i) {
                /* !!! If xml 1.1 was to be handled, should check for
                 *   LSEP and NEL too?
                 */
                if (text.charAt(i) > INT_SPACE) {
                    return false;
                }
            }
            return true;
        }
        return currEvent == SPACE;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return null; // tuto verziu tejto metody nepotrebujeme
    }

    @Override
    public int getAttributeCount() {
        if (currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (attrList == null) {
            calcNsAndAttrLists(true);
        }
        return attrList.size();
    }

    @Override
    public QName getAttributeName(int index) {
        return null; // toto nepotrebujeme
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (attrList == null) {
            calcNsAndAttrLists(true);
        }
        if (index >= attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) attrList.get(index);
        return internNsURI(attr.getNamespaceURI());
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (attrList == null) {
            calcNsAndAttrLists(true);
        }
        if (index >= attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) attrList.get(index);
        return internName(safeGetLocalName(attr));
    }

    @Override
    public String getAttributePrefix(int index) {
        return null; // toto nepotrebujeme
    }

    @Override
    public String getAttributeType(int index) {
        return null; // toto nepotrebujeme
    }

    @Override
    public String getAttributeValue(int index) {
        if (currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (attrList == null) {
            calcNsAndAttrLists(true);
        }
        if (index >= attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) attrList.get(index);
        return attr.getValue();
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return false;
    }

    @Override
    public int getNamespaceCount() {
        return 0;
    }

    @Override
    public String getNamespacePrefix(int index) {
        return null;
    }

    @Override
    public String getNamespaceURI(int index) {
        return null;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this;
    }

    @Override
    public int getEventType() {
        return currEvent;
    }

    @Override
    public String getText() {
        if (coalescedText != null) {
            return coalescedText;
        }
        if (((1 << currEvent) & MASK_GET_TEXT) == 0) {
            reportWrongState(ERR_STATE_NOT_TEXTUAL);
        }
        return currNode.getNodeValue();
    }

    @Override
    public char[] getTextCharacters() {
        return new char[0];
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return 0;
    }

    @Override
    public int getTextStart() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public Location getLocation() {
        // tu budeme asi musiet vratit nejaku dummy Location, lebo pracujeme s DOMSource a tym padom nevieme povedat
        // kde presne v XMLku sa nachadzame (stlpec a riadok)
        return new Location() {
            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public int getColumnNumber() {
                return -1;
            }

            @Override
            public int getCharacterOffset() {
                return -1;
            }

            @Override
            public String getPublicId() {
                return null;
            }

            @Override
            public String getSystemId() {
                return null;
            }
        };
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public String getLocalName() {
        if (currEvent == START_ELEMENT || currEvent == END_ELEMENT) {
            return internName(safeGetLocalName(currNode));
        }
        if (currEvent != ENTITY_REFERENCE) {
            reportWrongState(ERR_STATE_NO_LOCALNAME);
        }
        return internName(currNode.getNodeName());
    }

    @Override
    public boolean hasName() {
        return false;
    }

    @Override
    public String getNamespaceURI() {
        if (currEvent != START_ELEMENT && currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        return internNsURI(currNode.getNamespaceURI());
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public String getVersion() {
        /* No standard way to figure it out from a DOM Document node;
         * have to return null
         */
        return null;
    }

    @Override
    public boolean isStandalone() {
        /* No standard way to figure it out from a DOM Document node;
         * have to return false
         */
        return false;
    }

    @Override
    public boolean standaloneSet() {
        /* No standard way to figure it out from a DOM Document node;
         * have to return false
         */
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
         /* No standard way to figure it out from a DOM Document node;
         * have to return null
         */
        return null;
    }

    @Override
    public String getPITarget() {
        return null; // toto nepotrebujem
    }

    @Override
    public String getPIData() {
        return null; // toto nepotrebujem
    }

    private static String getEventTypeString(int eventType) {
        switch (eventType){
            case XMLEvent.START_ELEMENT:
                return "START_ELEMENT";
            case XMLEvent.END_ELEMENT:
                return "END_ELEMENT";
            case XMLEvent.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLEvent.CHARACTERS:
                return "CHARACTERS";
            case XMLEvent.COMMENT:
                return "COMMENT";
            case XMLEvent.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLEvent.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLEvent.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLEvent.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLEvent.DTD:
                return "DTD";
            case XMLEvent.CDATA:
                return "CDATA";
            case XMLEvent.SPACE:
                return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE, " + String.valueOf(eventType);
    }

    void coalesceText(int initialType) {
        _textBuffer.reset();
        _textBuffer.append(currNode.getNodeValue());

        Node n;
        while ((n = currNode.getNextSibling()) != null) {
            int type = n.getNodeType();
            if (type != Node.TEXT_NODE && type != Node.CDATA_SECTION_NODE) {
                break;
            }
            currNode = n;
            _textBuffer.append(currNode.getNodeValue());
        }
        coalescedText = _textBuffer.get();

        // Either way, type gets always set to be CHARACTERS
        currEvent = CHARACTERS;
    }

    void reportWrongState(int errorType) {
        throw new IllegalStateException(findErrorDesc(errorType, currEvent));
    }

    /**
     * Due to differences in how namespace-aware and non-namespace modes
     * work in DOM, different methods are needed. We may or may not be
     * able to detect namespace-awareness mode of the source Nodes
     * directly; but at any rate, should contain some logic for handling
     * problem cases.
     */
    private String safeGetLocalName(Node n)
    {
        String ln = n.getLocalName();
        if (ln == null) {
            ln = n.getNodeName();
        }
        return ln;
    }

    /**
     * Method used to locate error message description to use.
     * Calls sub-classes <code>findErrorDesc()</code> first, and only
     * if no message found, uses default messages defined here.
     */
    private String findErrorDesc(int errorType, int currEvent) {
        String evtDesc = eventTypeDesc(currEvent);
        switch (errorType) {
            case ERR_STATE_NOT_START_ELEM:
                return "Current event "+evtDesc+", needs to be START_ELEMENT";
            case ERR_STATE_NOT_ELEM:
                return "Current event "+evtDesc+", needs to be START_ELEMENT or END_ELEMENT";
            case ERR_STATE_NO_LOCALNAME:
                return "Current event ("+evtDesc+") has no local name";
            case ERR_STATE_NOT_PI:
                return "Current event ("+evtDesc+") needs to be PROCESSING_INSTRUCTION";

            case ERR_STATE_NOT_TEXTUAL:
                return "Current event ("+evtDesc+") not a textual event";
            case ERR_STATE_NOT_TEXTUAL_OR_ELEM:
                return "Current event ("+evtDesc+" not START_ELEMENT, END_ELEMENT, CHARACTERS or CDATA";
            case ERR_STATE_NOT_TEXTUAL_XXX:
                return "Current event "+evtDesc+", needs to be one of CHARACTERS, CDATA, SPACE or COMMENT";
        }
        // should never happen, but it'd be bad to throw another exception...
        return "Internal error (unrecognized error type: "+errorType+")";
    }

    /**
     * Method that converts given standard Stax event type into
     * textual representation.
     */
    static String eventTypeDesc(int type) {
        switch (type) {
            case START_ELEMENT:
                return "START_ELEMENT";
            case END_ELEMENT:
                return "END_ELEMENT";
            case START_DOCUMENT:
                return "START_DOCUMENT";
            case END_DOCUMENT:
                return "END_DOCUMENT";

            case CHARACTERS:
                return "CHARACTERS";
            case CDATA:
                return "CDATA";
            case SPACE:
                return "SPACE";

            case COMMENT:
                return "COMMENT";
            case PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case DTD:
                return "DTD";
            case ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
        }
        return "["+type+"]";
    }

    /**
     * @param attrsToo Whether to include actual attributes too, or
     *   just namespace declarations
     */
    private void calcNsAndAttrLists(boolean attrsToo) {
        NamedNodeMap attrsIn = currNode.getAttributes();

        // A common case: neither attrs nor ns decls, can use short-cut
        int len = attrsIn.getLength();
        if (len == 0) {
            attrList = Collections.emptyList();
            nsDeclList = Collections.emptyList();
            return;
        }

        if (!_cfgNsAware) {
            attrList = new ArrayList<Node>(len);
            for (int i = 0; i < len; ++i) {
                attrList.add(attrsIn.item(i));
            }
            nsDeclList = Collections.emptyList();
            return;
        }

        // most should be attributes... and possibly no ns decls:
        ArrayList<Node> attrsOut = null;
        ArrayList<String> nsOut = null;

        for (int i = 0; i < len; ++i) {
            Node attr = attrsIn.item(i);
            String prefix = attr.getPrefix();

            // Prefix?
            if (prefix == null || prefix.length() == 0) { // nope
                // default ns decl?
                if (!"xmlns".equals(attr.getLocalName())) { // nope
                    if (attrsToo) {
                        if (attrsOut == null) {
                            attrsOut = new ArrayList<Node>(len - i);
                        }
                        attrsOut.add(attr);
                    }
                    continue;
                }
                prefix = null;
            } else { // explicit ns decl?
                if (!"xmlns".equals(prefix)) { // nope
                    if (attrsToo) {
                        if (attrsOut == null) {
                            attrsOut = new ArrayList<Node>(len - i);
                        }
                        attrsOut.add(attr);
                    }
                    continue;
                }
                prefix = attr.getLocalName();
            }
            if (nsOut == null) {
                nsOut = new ArrayList<String>((len - i) * 2);
            }
            nsOut.add(internName(prefix));
            nsOut.add(internNsURI(attr.getNodeValue()));
        }

        attrList = (attrsOut == null) ? Collections.<Node>emptyList() : attrsOut;
        nsDeclList = (nsOut == null) ? Collections.<String>emptyList() : nsOut;
    }

    private void handleIllegalAttrIndex(int index) {
        Element elem = (Element) currNode;
        NamedNodeMap attrs = elem.getAttributes();
        int len = attrs.getLength();
        String msg = "Illegal attribute index "+index+"; element <"+elem.getNodeName()+"> has "+((len == 0) ? "no" : String.valueOf(len))+" attributes";
        throw new IllegalArgumentException(msg);
    }

    /**
     * Method called to do additional intern()ing for a name, if and as
     * necessary
     */
    private String internName(String name)
    {
        if (name == null) {
            return "";
        }
        return _cfgInternNames ? name.intern() : name;
    }

    private String internNsURI(String uri)
    {
        if (uri == null) {
            return "";
        }
        return _cfgInternNsURIs ? uri.intern() : uri;
    }

    /**
     * Helper class used to simplify text gathering while keeping
     * it as efficient as possible.
     */
    private final static class TextBuffer {
        private String mText = null;

        private StringBuilder mBuilder = null;

        private void reset() {
            mText = null;
            mBuilder = null;
        }

        private void append(final String text) {
            final int len = text.length();
            if (len > 0) {
                // Any prior text?
                if (mText != null) {
                    mBuilder = new StringBuilder(mText.length() + len);
                    mBuilder.append(mText);
                    mText = null;
                }
                if (mBuilder != null) {
                    mBuilder.append(text);
                } else {
                    mText = text;
                }
            }
        }

        private String get() {
            if (mText != null) {
                return mText;
            }

            if (mBuilder != null) {
                return mBuilder.toString();
            }

            return "";
        }

        private boolean isEmpty() {
            return mText == null && mBuilder == null;
        }
    }
}
