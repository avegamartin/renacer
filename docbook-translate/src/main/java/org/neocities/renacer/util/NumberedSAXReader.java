/**
 * 
 */
package org.neocities.renacer.util;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

import javafx.scene.control.TreeItem;

/**
 * SAXReader extendido con información de localización de nodos por número de
 * línea en el fichero XML.
 * 
 * @author avega
 *
 */
public class NumberedSAXReader extends SAXReader {

	/**
	 * Default constructor, that sets the line numbers aware document factory.
	 */
	public NumberedSAXReader() {
		super();
		this.setDocumentFactory(new LocatorAwareDocumentFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dom4j.io.SAXReader#setDocumentFactory(org.dom4j.DocumentFactory)
	 */
	@Override
	public void setDocumentFactory(DocumentFactory documentFactory) {
		super.setDocumentFactory(documentFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dom4j.io.SAXReader#createContentHandler(org.xml.sax.XMLReader)
	 */
	@Override
	protected SAXContentHandler createContentHandler(XMLReader reader) {
		return new NumberedSAXContentHandler(getDocumentFactory(), getDispatchHandler());
	}

	static class NumberedSAXContentHandler extends SAXContentHandler {

		private Locator locator;

		// A private SAXContentHandler
		private DocumentFactory documentFactory;

		public NumberedSAXContentHandler(DocumentFactory documentFactory, ElementHandler elementHandler) {
			super(documentFactory, elementHandler);
			this.documentFactory = documentFactory;
		}

		@Override
		public void setDocumentLocator(Locator documentLocator) {
			super.setDocumentLocator(documentLocator);
			this.locator = documentLocator;
			if (documentFactory instanceof LocatorAwareDocumentFactory) {
				((LocatorAwareDocumentFactory) documentFactory).setLocator(documentLocator);
			}
		}

		public Locator getLocator() {
			return locator;
		}
	}

	static class LocatorAwareDocumentFactory extends DocumentFactory {

		private Locator locator;

		public LocatorAwareDocumentFactory() {
			super();
		}

		public void setLocator(Locator locator) {
			this.locator = locator;
		}

		@Override
		public Element createElement(QName qname) {
			LocationAwareElement element = new LocationAwareElement(qname);
			if (locator != null)
				element.setLineNumber(locator.getLineNumber());
			return element;
		}
	}

	/**
	 * An Element that is aware of it location (line number in) in the source
	 * document
	 */
	public static class LocationAwareElement extends DefaultElement {

		private int lineNumber = -1;
		private boolean nodeWithErrors = false;
		private LocationAwareElement relatedNode = null;
		private TreeItem<LocationAwareElement> visualNode = null;

		public LocationAwareElement(QName qname) {
			super(qname);
		}

		public LocationAwareElement(QName qname, int attributeCount) {
			super(qname, attributeCount);
		}

		public LocationAwareElement(String name, Namespace namespace) {
			super(name, namespace);
		}

		public LocationAwareElement(String name) {
			super(name);
		}

		/**
		 * @return the lineNumber
		 */
		public int getLineNumber() {
			return lineNumber;
		}

		/**
		 * @param lineNumber the lineNumber to set
		 */
		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}

		/**
		 * @return Indica si en el nodo se da una condición de error de traducción.
		 */
		public boolean isNodeWithErrors() {
			return nodeWithErrors;
		}

		/**
		 * @param nodeWithErrors Establece si en el nodo se da una condición de error de traducción.
		 */
		public void setNodeWithErrors(boolean nodeWithErrors) {
			this.nodeWithErrors = nodeWithErrors;
		}

		/**
		 * @return Nodo traducción del actual.
		 */
		public LocationAwareElement getRelatedNode() {
			return relatedNode;
		}

		/**
		 * @param relatedNode Nodo traducción del actual.
		 */
		public void setRelatedNode(LocationAwareElement relatedNode) {
			this.relatedNode = relatedNode;
		}
		
		/**
		 * @return Ítem de árbol JavaFX que representa el actual nodo de traducción.
		 */
		public TreeItem<LocationAwareElement> getVisualNode() {
			return visualNode;
		}

		/**
		 * @param visualRelatedNode Ítem de árbol JavaFX que representa el actual nodo de traducción.
		 */
		public void setVisualNode(TreeItem<LocationAwareElement> visualNode) {
			this.visualNode = visualNode;
		}

		/* (non-Javadoc)
		 * @see org.dom4j.tree.AbstractElement#toString()
		 */
		public String toString() {
			return "<" + this.getName() + "> " + GestiónDOM.compactaCadenaXML(this.getText());			
		}
	}
}
