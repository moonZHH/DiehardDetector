package soot.jimple.infoflow.methodSummary.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import soot.jimple.infoflow.methodSummary.data.summary.SummaryMetaData;

/**
 * Class for parsing summary meta data from XML files
 * 
 * @author Steven Arzt
 *
 */
public class MetaDataReader extends AbstractXMLReader {

	// XML stuff incl. Verification against XSD
	private static final String XSD_FILE_PATH = "schema/SummaryMetaData.xsd";

	private enum State {
		summaryMetaData, exclusiveModels, exclusiveModel
	}

	/**
	 * Reads a meta data XML file and returns the corresponding data object. This
	 * method closes the reader.
	 * 
	 * @param reader The reader from which to read the summary meta data
	 * @return The summary data object read from the given reader
	 * @return XMLStreamException Thrown in case of a syntax error in the input file
	 * @throws IOException Thrown if the reader could not be read
	 */
	public SummaryMetaData read(Reader reader) throws XMLStreamException, SummaryXMLException, IOException {
		SummaryMetaData metaData = new SummaryMetaData();
		XMLStreamReader xmlreader = null;
		try {
			xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(reader);

			String name = "";
			String type = "";

			State state = State.summaryMetaData;
			while (xmlreader.hasNext()) {
				// Read the next tag
				xmlreader.next();
				if (!xmlreader.hasName())
					continue;

				final String localName = xmlreader.getLocalName();
				if (localName.equals(XMLMetaDataConstants.TREE_EXCLUSIVE_MODELS) && xmlreader.isStartElement()) {
					if (state == State.summaryMetaData)
						state = State.exclusiveModels;
					else
						throw new SummaryXMLException();
				} else if (localName.equals(XMLMetaDataConstants.TREE_EXCLUSIVE_MODEL) && xmlreader.isStartElement()) {
					if (state == State.exclusiveModels) {
						state = State.exclusiveModel;

						name = getAttributeByName(xmlreader, XMLMetaDataConstants.ATTRIBUTE_NAME);
						type = getAttributeByName(xmlreader, XMLMetaDataConstants.ATTRIBUTE_TYPE);
					} else
						throw new SummaryXMLException();
				} else if (localName.equals(XMLMetaDataConstants.TREE_EXCLUSIVE_MODEL) && xmlreader.isEndElement()) {
					if (state == State.exclusiveModel) {
						state = State.exclusiveModels;

						// Write the data object
						if (type != null && name != null && !name.isEmpty()) {
							switch (type) {
							case XMLMetaDataConstants.VALUE_CLASS:
								metaData.getExclusiveClasses().add(name);
								break;
							case XMLMetaDataConstants.VALUE_PACKAGE:
								metaData.getExclusivePackages().add(name);
							}
						}
					} else
						throw new SummaryXMLException();
				} else if (localName.equals(XMLMetaDataConstants.TREE_EXCLUSIVE_MODELS) && xmlreader.isEndElement()) {
					if (state == State.exclusiveModels)
						state = State.summaryMetaData;
					else
						throw new SummaryXMLException();
				}
			}

			return metaData;
		} finally {
			if (xmlreader != null)
				xmlreader.close();
		}
	}

	/**
	 * Reads a summary meta data xml file and returns the corresponding data object
	 * 
	 * @param fileName The file from which to read the method meta data
	 * @return The summary data object read from the given file
	 * @return XMLStreamException Thrown in case of a syntax error in the input file
	 * @throws IOException Thrown if the file could not be read
	 */

	public SummaryMetaData read(File fileName) throws XMLStreamException, SummaryXMLException, IOException {
		FileReader rdr = null;
		try {
			rdr = new FileReader(fileName);
			if (!verifyXML(rdr, XSD_FILE_PATH)) {
				throw new RuntimeException("The XML-File isn't valid");
			}
		} finally {
			if (rdr != null)
				rdr.close();
		}

		return read(new FileReader(fileName));
	}

}
