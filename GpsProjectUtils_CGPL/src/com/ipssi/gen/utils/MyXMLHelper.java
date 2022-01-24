/**
This class loads up  an XML Document and saves an XML document
**/
//CHANGED ON 12/22 to use Oracle's XML parser - otherwise JSPs were not running (boo hoo)
// JAXP packages
package com.ipssi.gen.utils;
//import oracle.xml.parser.v2.*;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
//import org.apache.xerces.dom3.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.*;
import org.apache.xpath.*;
import org.apache.commons.lang.*;

public class MyXMLHelper {

    static final String oututEncoding = "UTF-8";
    private Writer out;
    private InputStream inp;
    private int indent = 0;
    private boolean printIndentation = true;
    private final String basicIndent = "      ";
    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static public  double getAttribAsDouble(Element node, String attribName) {
       String t = node.getAttribute(attribName);
       if ((t != null) && (t.length() != 0))
          return Double.parseDouble(t);
       else
          return Misc.getUndefDouble();
    }

    static public String getAttribAsString(Element node, String attribName) {
       String t = node.getAttribute(attribName);
       if ((t != null) && (t.length() != 0))
          return t;
       else
          return null;
    }

    public static boolean hasChildElem(Element node) {
       for (Node n=node.getFirstChild(); n != null;n=n.getNextSibling())
          if (n.getNodeType() == 1)
             return true;
       return false;
    }

    static public  long getAttribAsLong(Element node, String attribName) {
      String t = node.getAttribute(attribName);
       if ((t != null) && (t.length() != 0))
          return Long.parseLong(t);
       else
          return (long) Misc.getUndefInt();

    }
    static public  int getAttribAsInt(Element node, String attribName) {
      String t = node.getAttribute(attribName);
       if ((t != null) && (t.length() != 0))
          return Integer.parseInt(t);
       else
          return Misc.getUndefInt();

    }
    public static Element getChildElementById(Element node, String idName, String idVal) {
       if (idVal == null || idName == null || node == null)
          return null;
       for (Node ch = node.getFirstChild();ch != null; ch = ch.getNextSibling()) {
          if (ch.getNodeType() != Node.ELEMENT_NODE)
             continue;
          Element elem = (Element) ch;
          if (elem.getAttribute(idName).equals(idVal))
             return elem;
       }
       return null;
    }

    public static Element getElementById(Element node, String tagName, String idName, String idVal) {
       if (idVal == null)
         return null;
       NodeList childNodes = node.getElementsByTagName(tagName);
       int i, count;
       
       for (i=0,count=childNodes.getLength();i<count;i++) {
          Node n = childNodes.item(i);
          if (n.getNodeType() != Node.ELEMENT_NODE)
             continue;
          Element elem = (Element) n;
          String idNameVal = elem.getAttribute(idName);
          if (idVal.equals(elem.getAttribute(idName)))
             return elem;
       }
       return null;
    }

    public static Element getFirstChild(Element node) {
       NodeList childNodes = node.getChildNodes();
       int i, count;
       for (i=0,count=childNodes.getLength();i<count;i++) {
          Node n = childNodes.item(i);
          if (n.getNodeType() != Node.ELEMENT_NODE)
             continue;
          return ((Element)n);
       }
       return null;
    }

    public static Element getChildElementByTagName(Element node, String tagName) {
       NodeList childNodes = node.getElementsByTagName(tagName);
       if (childNodes.getLength() > 0)
          return (Element) childNodes.item(0);
       return null;       
    }

    public static int getElemCount(Element elem) {
       NodeList childNodes = elem.getChildNodes();
       int retval=0;
       for (int i=0,count = childNodes.getLength();i<count;i++)
          if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE)
             retval++;
       return retval;

    }

    public MyXMLHelper(InputStream inp, Writer out) {
        this.inp = inp;
        this.out = out;
    }

    public static Element copyElemFrom(Document copyIntoDoc, Element copyOver) {
        if (copyOver == null)
           return null;
        Element retval = copyIntoDoc.createElement(copyOver.getTagName());
        NamedNodeMap attribs = copyOver.getAttributes();
        for (int i=0,is = attribs.getLength();i<is;i++) {
           Node attrib = attribs.item(i);
           retval.setAttribute(attrib.getNodeName(), attrib.getNodeValue());
        }
        for (Node ch = copyOver.getFirstChild();ch != null; ch = ch.getNextSibling()) {
           if (ch.getNodeType() != 1)
              continue;
           Element chCopy = copyElemFrom(copyIntoDoc, (Element)ch);
           retval.appendChild(chCopy);
        }
        return retval;
    }

/**
    MyXMLHelper(InputStream inp) {
        MyXMLHelper(inp,null);
    }

    MyXMLHelper(PrintWriter out) {
        MyXMLHelper(null,out);
    }
**/

    /**
     * Indent to the current level in multiples of basicIndent
     */
    private void outputIndentation() {
      try {
        for (int i = 0; i < indent; i++) {
            out.write(basicIndent);
        }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
    }
public void save(Node n, boolean prettyPrintOutput) {
		// Indent to the current level before printing anything
		try {
			if (prettyPrintOutput) 
				if (printIndentation)
					outputIndentation();

			int type = n.getNodeType();
			switch (type) {
				case Node.ATTRIBUTE_NODE:
					String name = n.getNodeName();
					String value = n.getNodeValue();
					//HACK ..
					value = escapedStr(value);
					if (name == null)
						break;

					if (value == null)
						value = Integer.toString(Misc.getUndefInt()); //to be safe - otherwise it
					StringBuilder tempStr = new StringBuilder(" ");
					tempStr.append(name.trim()).append("=\"").append(value.trim()).append("\"");
					out.write(tempStr.toString());
					break;
				case Node.DOCUMENT_NODE:
					// out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+Misc.LINE_SEPARATOR_STRING);
					// to avoid duplication above commented
					break;
				case Node.ELEMENT_NODE:
					out.write("<");
					out.write(n.getNodeName().trim());
					// Print attributes if any.  Note: element attributes are not
					// children of ELEMENT_NODEs but are properties of their
					// associated ELEMENT_NODE.  For this reason, they are printed
					// with 2x the indent level to indicate this.
					NamedNodeMap atts = n.getAttributes();
					printIndentation = false;
					for (int i = 0; i < atts.getLength(); i++) {
						Node att = atts.item(i);
						save(att, prettyPrintOutput);
					}
					if (prettyPrintOutput)
						out.write(">" + Misc.LINE_SEPARATOR_STRING);
					else
						out.write(">");
					printIndentation = true;
					break;
				case Node.TEXT_NODE:
					if (prettyPrintOutput)
						out.write(n.getNodeValue().trim() + Misc.LINE_SEPARATOR_STRING);
					else
						out.write(n.getNodeValue().trim());
					break;
				default:
					System.err.println("Unknown node type");
					//throw error;
					break;
			}

			// Print children if any
			indent++;
			if (type != Node.ATTRIBUTE_NODE) { // hack ...
				for (Node child = n.getFirstChild(); child != null;
					 child = child.getNextSibling()) {
					save(child, prettyPrintOutput);
				}
			}
			indent--;
			if (type == Node.ELEMENT_NODE) {
				if (prettyPrintOutput)
					if (printIndentation)
						outputIndentation();
				if (prettyPrintOutput)
					out.write("</" + n.getNodeName().trim() + ">" + Misc.LINE_SEPARATOR_STRING);
				else
					out.write("</" + n.getNodeName().trim() + ">");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void save(Node n) {
     // Indent to the current level before printing anything
      try {
        if (printIndentation)
          outputIndentation();
        int type = n.getNodeType();
        switch (type) {
        case Node.ATTRIBUTE_NODE:
//            String t1, t2;
//            t1 = n.getNodeName();
//            t2 = n.getNodeValue();
//            System.out.println(t1+","+t2);
           String name = n.getNodeName();
           String value = n.getNodeValue();
           //HACK ..
             value = escapedStr(value);
//           value = value.replace('&','+');
//           value = value.replace('!','-');
//           value = value.replace('<',' ');
//           value = value.replace('>',' ');
//           value = java.net.URLEncoder.encode(value);
           if (name == null)
              break;

           if (value == null)
              value = Integer.toString(Misc.getUndefInt()); //to be safe - otherwise it
              StringBuilder tempStr = new StringBuilder("  ");
              tempStr.append(name.trim()).append("=\"").append(value.trim()).append("\"");
              out.write(tempStr.toString());
            break;
        case Node.DOCUMENT_NODE:
           // out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+Misc.LINE_SEPARATOR_STRING);
           // to avoid duplication above commented
            break;
        case Node.ELEMENT_NODE:
            out.write("<");
            out.write(n.getNodeName().trim());


            // Print attributes if any.  Note: element attributes are not
            // children of ELEMENT_NODEs but are properties of their
            // associated ELEMENT_NODE.  For this reason, they are printed
            // with 2x the indent level to indicate this.
            NamedNodeMap atts = n.getAttributes();
            printIndentation = false;
            for (int i = 0; i < atts.getLength(); i++) {
                Node att = atts.item(i);
                save(att);
            }
            out.write(">"+Misc.LINE_SEPARATOR_STRING);
            printIndentation = true;
            break;
        case Node.TEXT_NODE:
            out.write(n.getNodeValue().trim()+Misc.LINE_SEPARATOR_STRING);
            break;
        default:
            System.err.println("Unknown node type");
            //throw error;
            break;
        }

        // Print children if any
        indent++;
        if (type != Node.ATTRIBUTE_NODE) { // hack ...
          for (Node child = n.getFirstChild(); child != null;
               child = child.getNextSibling()) {
              save(child);
          }
        }
        indent--;
        if (type == Node.ELEMENT_NODE) {
            if (printIndentation)
                outputIndentation();
            out.write("</"+n.getNodeName().trim()+">"+Misc.LINE_SEPARATOR_STRING);
        }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
    }
    public static String escapedStr(String s) {
       if (s == null)
          return s;
       StringBuilder retval = new StringBuilder();

       for (int i=0,is=s.length();i<is;i++) {
          char ch = s.charAt(i);
          if (ch == '&' && !(s.startsWith("&amp;",i) || s.startsWith("&lt;",i) || s.startsWith("&gt;",i) || s.startsWith("&quot;",i)))
             retval.append("&amp;");
          else if (ch == '<')
             retval.append("&lt;");
          else if (ch == '>')
             retval.append("&gt;");
          else if (ch == '"')
             retval.append("&quot;");
//          else if (ch == '!')
//             retval.append('-');
          else if ((int)ch < 32)
             retval.append(' ');
          else if ((int)ch > 122)
             retval.append(' ');
          else
             retval.append(ch);
       }
       return retval.toString();
    }
    public static String wellFormedXMLString(String s) {
    	 if (s == null)
             return s;
          StringBuilder retval = new StringBuilder();
          int posStart = 0;
          if (!(s.substring(0, 3).equals("<?x"))) {
				retval.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			}
		    else {//exists check if there actually is an encoding there
		         int endQ = s.indexOf("?>");
		         int encPos = s.indexOf("encoding");
		         if (encPos >= 0 && encPos < endQ) {//there exists valid
		         }
		         else {
		            retval.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		            posStart = endQ+2;
		         }
		    }
          boolean inQuotes = false;
          int lastQuotePos = -1;
          int initSzAdj = retval.length()-posStart;
          for (int i=posStart,is=s.length();i<is;i++) {
             char ch = s.charAt(i);
             if (ch == '=') {
            	 retval.append(ch);
            	 inQuotes = false;
            	 lastQuotePos = -1;
             }
             else if (ch == '\'' || ch=='"') {
            	if (inQuotes) {
            		if (lastQuotePos >= 0) {
            			retval.replace(lastQuotePos, lastQuotePos+1, " ");
            		}
            		retval.append(ch);
            		lastQuotePos = i+initSzAdj;
            	}
            	else  {
            		retval.append(ch);
            		inQuotes = true;
            		lastQuotePos = -1;
            	}
             }
             else
                retval.append(ch);
          }
          return retval.toString();
    	
    }
	public static Document loadFromString(String inpStr) {
		//TODO fix this somehow to use non-deprecated features
		if ((inpStr == null) || (inpStr.length() == 0))
			return MyXMLHelper.create();
		else {
			//if (!(inpStr.substring(0, 3).equals("<?x"))) {
			//	inpStr = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + inpStr;
			//}
		    //else {//exists check if there actually is an encoding there
		    //     int endQ = inpStr.indexOf("?>");
		    //     int encPos = inpStr.indexOf("encoding");
		    //     if (encPos >= 0 && encPos < endQ) {//there exists valid
		    //     }
		    //     else {
		    //        inpStr = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+(inpStr.substring(endQ+2, inpStr.length()));
		    //     }
		    //}
		}
		inpStr = wellFormedXMLString(inpStr);
		//StringBuilderInputStream xmlDataStream = new StringBuilderInputStream(inpStr);
		ByteArrayInputStream xmlDataStream = null;
		try {
			xmlDataStream = new ByteArrayInputStream(inpStr.getBytes("ISO-8859-1"));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		if (xmlDataStream != null) {
		    MyXMLHelper xmlLoader = new MyXMLHelper (xmlDataStream,null);
		    Document taskDoc = xmlLoader.load();
		    return taskDoc;
		}
		else {
		    return MyXMLHelper.create();
		}
    }

    public static Document create() {
    /** ----- for checking Oracle **/
    /*
    XMLDocument retval = null;
    try {
      retval = new XMLDocument();
    }
    catch (Exception e) {
       e.printStackTrace();
    }
    return retval;
    */
    /** --- End check Oracle **/
   // /** ----  Commented out - Checking Oracle parser
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        // Set the validation mode to either: no validation, DTD
        // validation, or XSD validation
        dbf.setValidating(false);
        // Optional: set various configuration options
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(false);
        // The opposite of creating entity ref nodes is expanding them inline
        dbf.setExpandEntityReferences(true);

        // Step 2: create a DocumentBuilder that satisfies the constraints
        // specified by the DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder();

        // Set an ErrorHandler before parsing
        OutputStreamWriter errorWriter =
            new OutputStreamWriter(System.err, oututEncoding);
        db.setErrorHandler(
            new MyErrorHandler(new PrintWriter(errorWriter, true)));

        // Step 3: parse the input file
        Document doc = db.newDocument();
        return doc;
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
 //---- end - to undo check for Oracle Parser uncomment till this point     */

    }
    public Document load() {
    /** ----- Oracle Check **/
    /*
        XMLDocument theXMLDoc     = null;

    // Create an oracle.xml.parser.v2.DOMParser to parse the document.
    DOMParser theParser = new DOMParser();

    theParser.setPreserveWhitespace(false);


    // Set the parser to work in non-Validating mode
    theParser.setValidationMode(false);

    try {

      // Parse the document from the InputStream
      theParser.parse( inp );

      // Get the parsed XML Document from the parser
      theXMLDoc = theParser.getDocument();


    }
    catch (SAXParseException s) {
      System.out.println(xmlError(s));
//      throw s;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return theXMLDoc;
    */
    /** End Oracle Check ---- */

///** ------------- Comment begin for Old parser use
      try {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);

        // Set the validation mode to either: no validation, DTD
        // validation, or XSD validation
        dbf.setValidating(false);

        // Optional: set various configuration options
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(false);
        // The opposite of creating entity ref nodes is expanding them inline
        dbf.setExpandEntityReferences(true);

        // Step 2: create a DocumentBuilder that satisfies the constraints
        // specified by the DocumentBuilderFactory
		DocumentBuilder db = dbf.newDocumentBuilder();
        // Set an ErrorHandler before parsing
        OutputStreamWriter errorWriter =
            new OutputStreamWriter(System.err, oututEncoding);
        db.setErrorHandler(
            new MyErrorHandler(new PrintWriter(errorWriter, true)));

        // Step 3: parse the input file
        Document doc = db.parse(inp);
        return doc;
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
//----      ****/
    }



    public static void main(String[] args) throws Exception {
        String filenameIn = "testinput.xml";
        String filenameOut = "testoutut.xml";
        if (args.length > 0)
            filenameIn = args[0];
        if (args.length > 1)
            filenameOut = args[1];

        FileInputStream inp = new FileInputStream(filenameIn);
        FileWriter out = new FileWriter(filenameOut);
        PrintWriter outw = new PrintWriter(out, true);


        MyXMLHelper test = new MyXMLHelper(inp, outw);

      //  MyXMLHelper test = new MyXMLHelper(inp, new PrintWriter(System.err, true));
        Document doc = test.load();
        test.save(doc);

        out.close();
      //  new MyXMLHelper(new PrintWriter(outWriter, true)).echo(doc);
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
    private static String xmlError(SAXParseException s) {
     int lineNum = s.getLineNumber();
     int  colNum = s.getColumnNumber();
     String file = s.getSystemId();
     String  err = s.getMessage();

     return "XML parse error in file " + file +
            "\n" + "at line " + lineNum + ", character " + colNum +
            "\n" + err;

  }

  public void setOut(Writer out) {
     this.out = out;
  }

  public void setInp(InputStream inp) {
     this.inp = inp;
  }
  
  public static Element getChildElementByTagNameOld(Element node, String tagName) {
       NodeList childNodes = node.getChildNodes();
       int i, count;

       //System.out.println("In GetChildElementByTagName " + tagName );
       //System.out.println("ChildNodes Num ");
       //System.out.println(childNodes.getLength());
       //MyXMLHelper temp = new MyXMLHelper(null,new PrintWriter(System.out, true));
       for (i=0,count=childNodes.getLength();i<count;i++) {
          //System.out.println("Printing child node"+Integer.toString(i));
          //temp.save(childNodes.item(i));
          Node n = childNodes.item(i);
//          int nodeType = n.getNodeType();
//          String nodeName = n.getNodeName();
//          System.out.print(nodeName);
//          System.out.print("   ");
//         System.out.println(nodeType);
          if (n.getNodeType() != Node.ELEMENT_NODE)
             continue;
          Element elem = (Element) n;
//          System.out.println("Tagname is:" + elem.getTagName());
          if (elem.getTagName().equals(tagName)) {
//             System.out.println("Found match\n");
             return elem;
          }
       }
//       System.out.println("Didn't find match");
       return null;
    }

	public static Element getOrCreateChildElement1(Document doc, Element parentElem, String tag) throws Exception {
		Element childElem = null;
		try {
			childElem = (Element)XPathAPI.selectSingleNode(parentElem, "tag");
			if (childElem == null) {
				childElem = doc.createElement(tag);
				parentElem.appendChild(childElem);
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		return childElem;
	}

	public static Element getOrCreateChildElement(Document doc, Element parentElem, String tag) throws Exception {
		Element childElem = null;
		try {
			for (Node childNode = parentElem.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
				if (childNode.getNodeType() != 1)
					continue;
				Element ch = (Element)childNode;
				if (ch.getTagName().equals(tag)) {
					childElem = ch;
					break;
				}
			}

			if (childElem == null) {
				childElem = doc.createElement(tag);
				parentElem.appendChild(childElem);
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		return childElem;
	}

	public static Element getFirstLevelChildElementByTagName(Element node, String tagName) throws Exception{
		Element childElem = null;
		try {
			for (Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
				if (childNode.getNodeType() != 1)
					continue;
				Element ch = (Element)childNode;
				if (ch.getTagName().equals(tagName)) {
					childElem = ch;
					break;
				}
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		return childElem;
	}

	public static Element getChildElementById(Element node, String childTag, String idName, String idVal) {
		if (idVal == null || idName == null || node == null || childTag == null)
			return null;
		for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (ch.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element elem = (Element)ch;
			if (!elem.getTagName().equals(childTag))
				continue;

			if (elem.getAttribute(idName).equals(idVal))
				return elem;
		}
		return null;
	}
	public static Pair<Document, Element> getDocument(String root){
		 DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder documentBuilder = null;
		 Document document = null;
		 Element rootElement = null;
		 Pair<Document, Element> retval = null;
		 try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		    document = documentBuilder.newDocument();
			rootElement = document.createElement(root);
			document.appendChild(rootElement);
			retval = new Pair<Document, Element>(document, rootElement);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		 return retval;
	}
	public static Element addElement(Document doc, Element parent, String elementName){
		Element em = null;
		if(doc != null && parent != null){
		em = doc.createElement(elementName);
		parent.appendChild(em);
		}
		return em;
	}	
	public static void addDataNode(Document doc, Element parent, String elementVal){
		if(doc != null && parent != null){
		parent.appendChild(doc.createTextNode(elementVal));
		}
	}
	public static void addAttribute(Element parent, String attrName, String attrVal){
		if(parent != null){
			parent.setAttribute(attrName, attrVal);
		}
	}
	public static void getStreamXMLData(Document document, ServletOutputStream stream){
		TransformerFactory transformerFactory = null;
		Transformer transformer = null;
		DOMSource source = null;
		try {
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			source = new DOMSource(document);
			StreamResult result =  new StreamResult(stream);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
