package com.zxt.dlna.dmc;

import static org.fourthline.cling.model.XMLUtil.appendNewElement;
import static org.fourthline.cling.model.XMLUtil.appendNewElementIfNotNull;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.zxt.dlna.dmp.ContentItem;


public class GenerateXml {
	final private static Logger log = Logger.getLogger(GenerateXml.class
			.getName());

	public static final String UNKNOWN_TITLE = "Unknown Title";

	public String generate(ContentItem content) throws Exception {
		return generate(content, false);
	}

	public String generate(ContentItem content, boolean nestedItems)
			throws Exception {
		return documentToString(buildDOM(content, nestedItems), true);
	}

	protected String documentToString(Document document, boolean omitProlog)
			throws Exception {
		TransformerFactory transFactory = TransformerFactory.newInstance();

		// Indentation not supported on Android 2.2
		// transFactory.setAttribute("indent-number", 4);

		Transformer transformer = transFactory.newTransformer();

		if (omitProlog) {
			// TODO: UPNP VIOLATION: Terratec Noxon Webradio fails when DIDL
			// content has a prolog
			// No XML prolog! This is allowed because it is UTF-8 encoded and
			// required
			// because broken devices will stumble on SOAP messages that contain
			// (even
			// encoded) XML prologs within a message body.
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}

		// Again, Android 2.2 fails hard if you try this.
		// transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter out = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(out));
		return out.toString();
	}

	protected Document buildDOM(ContentItem content, boolean nestedItems)
			throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		Document d = factory.newDocumentBuilder().newDocument();

		generateRoot(content, d, nestedItems);

		return d;
	}

	protected void generateRoot(ContentItem content, Document descriptor,
			boolean nestedItems) {
		Element rootElement = descriptor.createElementNS(
				DIDLContent.NAMESPACE_URI, "DIDL-Lite");
		descriptor.appendChild(rootElement);

		// rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
		// "xmlns:didl", DIDLContent.NAMESPACE_URI);
		rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:upnp", DIDLObject.Property.UPNP.NAMESPACE.URI);
		rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dc",
				DIDLObject.Property.DC.NAMESPACE.URI);
		rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:dlna", DIDLObject.Property.DLNA.NAMESPACE.URI);
		rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:sec", DIDLObject.Property.SEC.NAMESPACE.URI);

		if (null != content.getContainer()) {
			generateContainer(content.getContainer(), descriptor, rootElement,
					nestedItems);
		}
		if (null != content.getItem()) {
			generateItem(content.getItem(), descriptor, rootElement);
		}
		//
		// for (DescMeta descMeta : content.getContainer().getDescMetadata()) {
		// if (descMeta == null)
		// continue;
		// generateDescMetadata(descMeta, descriptor, rootElement);
		// }
	}

	protected void populateDescMetadata(Element descElement, DescMeta descMeta) {
		if (descMeta.getMetadata() instanceof Document) {
			Document doc = (Document) descMeta.getMetadata();

			NodeList nl = doc.getDocumentElement().getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE)
					continue;

				Node clone = descElement.getOwnerDocument().importNode(n, true);
				descElement.appendChild(clone);
			}

		} else {
			log.warning("Unknown desc metadata content, please override populateDescMetadata(): "
					+ descMeta.getMetadata());
		}
	}

	protected void generateContainer(Container container, Document descriptor,
			Element parent, boolean nestedItems) {

		if (container.getClazz() == null) {
			throw new RuntimeException(
					"Missing 'upnp:class' element for container: "
							+ container.getId());
		}

		Element containerElement = appendNewElement(descriptor, parent,
				"container");

		if (container.getId() == null)
			throw new NullPointerException("Missing id on container: "
					+ container);
		containerElement.setAttribute("id", container.getId());

		if (container.getParentID() == null)
			throw new NullPointerException("Missing parent id on container: "
					+ container);
		containerElement.setAttribute("parentID", container.getParentID());

		if (container.getChildCount() != null) {
			containerElement.setAttribute("childCount",
					Integer.toString(container.getChildCount()));
		}

		containerElement.setAttribute("restricted",
				booleanToInt(container.isRestricted()));
		containerElement.setAttribute("searchable",
				booleanToInt(container.isSearchable()));

		String title = container.getTitle();
		if (title == null) {
			title = UNKNOWN_TITLE;
		}

		appendNewElementIfNotNull(descriptor, containerElement, "dc:title",
				title, DIDLObject.Property.DC.NAMESPACE.URI);

		appendNewElementIfNotNull(descriptor, containerElement, "dc:creator",
				container.getCreator(), DIDLObject.Property.DC.NAMESPACE.URI);

		appendNewElementIfNotNull(descriptor, containerElement,
				"upnp:writeStatus", container.getWriteStatus(),
				DIDLObject.Property.UPNP.NAMESPACE.URI);

		appendClass(descriptor, containerElement, container.getClazz(),
				"upnp:class", false);

		for (DIDLObject.Class searchClass : container.getSearchClasses()) {
			appendClass(descriptor, containerElement, searchClass,
					"upnp:searchClass", true);
		}

		for (DIDLObject.Class createClass : container.getCreateClasses()) {
			appendClass(descriptor, containerElement, createClass,
					"upnp:createClass", true);
		}

		appendProperties(descriptor, containerElement, container, "upnp",
				DIDLObject.Property.UPNP.NAMESPACE.class,
				DIDLObject.Property.UPNP.NAMESPACE.URI);
		appendProperties(descriptor, containerElement, container, "dc",
				DIDLObject.Property.DC.NAMESPACE.class,
				DIDLObject.Property.DC.NAMESPACE.URI);

		if (nestedItems) {
			for (Item item : container.getItems()) {
				if (item == null)
					continue;
				generateItem(item, descriptor, containerElement);
			}
		}

		for (Res resource : container.getResources()) {
			if (resource == null)
				continue;
			generateResource(resource, descriptor, containerElement);
		}

		for (DescMeta descMeta : container.getDescMetadata()) {
			if (descMeta == null)
				continue;
			generateDescMetadata(descMeta, descriptor, containerElement);
		}
	}

	protected void generateItem(Item item, Document descriptor, Element parent) {

		if (item.getClazz() == null) {
			throw new RuntimeException(
					"Missing 'upnp:class' element for item: " + item.getId());
		}

		Element itemElement = appendNewElement(descriptor, parent, "item");

		if (item.getId() == null)
			throw new NullPointerException("Missing id on item: " + item);
		itemElement.setAttribute("id", item.getId());

		if (item.getParentID() == null)
			throw new NullPointerException("Missing parent id on item: " + item);
		itemElement.setAttribute("parentID", item.getParentID());

		if (item.getRefID() != null)
			itemElement.setAttribute("refID", item.getRefID());
		itemElement.setAttribute("restricted",
				booleanToInt(item.isRestricted()));

		String title = item.getTitle();
		if (title == null) {
			log.warning("Missing 'dc:title' element for item: " + item.getId());
			title = UNKNOWN_TITLE;
		}

		appendNewElementIfNotNull(descriptor, itemElement, "dc:title", title,
				DIDLObject.Property.DC.NAMESPACE.URI);

		appendNewElementIfNotNull(descriptor, itemElement, "dc:creator",
				item.getCreator(), DIDLObject.Property.DC.NAMESPACE.URI);

		appendNewElementIfNotNull(descriptor, itemElement, "upnp:writeStatus",
				item.getWriteStatus(), DIDLObject.Property.UPNP.NAMESPACE.URI);

		appendClass(descriptor, itemElement, item.getClazz(), "upnp:class",
				false);

		appendProperties(descriptor, itemElement, item, "upnp",
				DIDLObject.Property.UPNP.NAMESPACE.class,
				DIDLObject.Property.UPNP.NAMESPACE.URI);
		appendProperties(descriptor, itemElement, item, "dc",
				DIDLObject.Property.DC.NAMESPACE.class,
				DIDLObject.Property.DC.NAMESPACE.URI);
		appendProperties(descriptor, itemElement, item, "sec",
				DIDLObject.Property.SEC.NAMESPACE.class,
				DIDLObject.Property.SEC.NAMESPACE.URI);

		for (Res resource : item.getResources()) {
			if (resource == null)
				continue;
			generateResource(resource, descriptor, itemElement);
		}

		for (DescMeta descMeta : item.getDescMetadata()) {
			if (descMeta == null)
				continue;
			generateDescMetadata(descMeta, descriptor, itemElement);
		}
	}

	protected void generateResource(Res resource, Document descriptor,
			Element parent) {

		if (resource.getValue() == null) {
			throw new RuntimeException("Missing resource URI value" + resource);
		}
		if (resource.getProtocolInfo() == null) {
			throw new RuntimeException("Missing resource protocol info: "
					+ resource);
		}

		Element resourceElement = appendNewElement(descriptor, parent, "res",
				resource.getValue());
		resourceElement.setAttribute("protocolInfo", resource.getProtocolInfo()
				.toString());
		if (resource.getImportUri() != null)
			resourceElement.setAttribute("importUri", resource.getImportUri()
					.toString());
		if (resource.getSize() != null)
			resourceElement.setAttribute("size", resource.getSize().toString());
		if (resource.getDuration() != null)
			resourceElement.setAttribute("duration", resource.getDuration());
		if (resource.getBitrate() != null)
			resourceElement.setAttribute("bitrate", resource.getBitrate()
					.toString());
		if (resource.getSampleFrequency() != null)
			resourceElement.setAttribute("sampleFrequency", resource
					.getSampleFrequency().toString());
		if (resource.getBitsPerSample() != null)
			resourceElement.setAttribute("bitsPerSample", resource
					.getBitsPerSample().toString());
		if (resource.getNrAudioChannels() != null)
			resourceElement.setAttribute("nrAudioChannels", resource
					.getNrAudioChannels().toString());
		if (resource.getColorDepth() != null)
			resourceElement.setAttribute("colorDepth", resource.getColorDepth()
					.toString());
		if (resource.getProtection() != null)
			resourceElement
					.setAttribute("protection", resource.getProtection());
		if (resource.getResolution() != null)
			resourceElement
					.setAttribute("resolution", resource.getResolution());
	}

	protected void generateDescMetadata(DescMeta descMeta, Document descriptor,
			Element parent) {

		if (descMeta.getId() == null) {
			throw new RuntimeException("Missing id of description metadata: "
					+ descMeta);
		}
		if (descMeta.getNameSpace() == null) {
			throw new RuntimeException(
					"Missing namespace of description metadata: " + descMeta);
		}

		Element descElement = appendNewElement(descriptor, parent, "desc");
		descElement.setAttribute("id", descMeta.getId());
		descElement.setAttribute("nameSpace", descMeta.getNameSpace()
				.toString());
		if (descMeta.getType() != null)
			descElement.setAttribute("type", descMeta.getType());
		populateDescMetadata(descElement, descMeta);
	}

	protected String booleanToInt(boolean b) {
		return b ? "1" : "0";
	}

	protected void appendClass(Document descriptor, Element parent,
			DIDLObject.Class clazz, String element, boolean appendDerivation) {
		Element classElement = appendNewElementIfNotNull(descriptor, parent,
				element, clazz.getValue(),
				DIDLObject.Property.UPNP.NAMESPACE.URI);
		if (clazz.getFriendlyName() != null
				&& clazz.getFriendlyName().length() > 0)
			classElement.setAttribute("name", clazz.getFriendlyName());
		if (appendDerivation)
			classElement.setAttribute("includeDerived",
					Boolean.toString(clazz.isIncludeDerived()));
	}

	protected void appendProperties(Document descriptor, Element parent,
			DIDLObject object, String prefix,
			Class<? extends DIDLObject.Property.NAMESPACE> namespace,
			String namespaceURI) {
		for (DIDLObject.Property<Object> property : object
				.getPropertiesByNamespace(namespace)) {
			Element el = descriptor.createElementNS(namespaceURI, prefix + ":"
					+ property.getDescriptorName());
			parent.appendChild(el);
			property.setOnElement(el);
		}
	}
}
