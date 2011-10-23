package org.jdom2.test.cases.output;

/* Please run replic.pl on me ! */
/**
 * Please put a description of your test here.
 * 
 * @author unascribed
 * @version 0.1
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;

import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.IllegalDataException;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.UncheckedJDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.AbstractXMLOutputProcessor;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputProcessor;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.junit.runner.JUnitCore;

@SuppressWarnings("javadoc")
public final class TestXMLOutputter {

    /**
     * The main method runs all the tests in the text ui
     */
    public static void main (String args[]) 
     {
        JUnitCore.runClasses(TestXMLOutputter.class);
    }


    @Test
    public void test_HighSurrogatePair() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root>&#x10000; &#x10000;</root>"));
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      StringWriter sw = new StringWriter();
      outputter.output(doc, sw);
      String xml = sw.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.getLineSeparator() +
                   "<root>&#x10000; &#x10000;</root>" + format.getLineSeparator(), xml);
    }

    @Test
    public void test_HighSurrogatePairDecimal() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root>&#x10000; &#65536;</root>"));
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      outputter.output(doc, baos);
      String xml = baos.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.getLineSeparator() +
                   "<root>&#x10000; &#x10000;</root>" + format.getLineSeparator(), xml);
    }

    @Test
    public void test_HighSurrogateAttPair() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root att=\"&#x10000; &#x10000;\" />"));
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      outputter.output(doc, baos);
      String xml = baos.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.getLineSeparator() +
                   "<root att=\"&#x10000; &#x10000;\" />" + format.getLineSeparator(), xml);
    }

    @Test
    public void test_HighSurrogateAttPairDecimal() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root att=\"&#x10000; &#65536;\" />"));
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      outputter.output(doc, baos);
      String xml = baos.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.getLineSeparator() +
                   "<root att=\"&#x10000; &#x10000;\" />" + format.getLineSeparator(), xml);
    }

    // Construct a raw surrogate pair character and confirm it outputs hex escaped
    @Test
    public void test_RawSurrogatePair() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root>\uD800\uDC00</root>"));
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      outputter.output(doc, baos);
      String xml = baos.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.getLineSeparator() +
                   "<root>&#x10000;</root>" + format.getLineSeparator(), xml);
    }

    // Construct a raw surrogate pair character and confirm it outputs hex escaped, when UTF-8 too
    @Test
    public void test_RawSurrogatePairUTF8() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root>\uD800\uDC00</root>"));
      Format format = Format.getCompactFormat().setEncoding("UTF-8");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      outputter.output(doc, baos);
      String xml = baos.toString();
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + format.getLineSeparator() +
                   "<root>&#x10000;</root>" + format.getLineSeparator(), xml);
    }

    // Construct illegal XML and check if the parser notices
    @Test
    public void test_ErrorSurrogatePair() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root></root>"));
      try {
        doc.getRootElement().setText("\uD800\uDBFF");
        fail("Illegal surrogate pair should have thrown an exception");
      }
      catch (IllegalDataException e) {
    	  // do nothing
      } catch (Exception e) {
    	  fail ("Unexpected exception " + e.getClass());
      }
    }

    // Manually construct illegal XML and make sure the outputter notices
    @Test
    public void test_ErrorSurrogatePairOutput() throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(true);
      Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?><root></root>"));
      Text t = new UncheckedJDOMFactory().text("\uD800\uDBFF");
      doc.getRootElement().setContent(t);
      Format format = Format.getCompactFormat().setEncoding("ISO-8859-1");
      XMLOutputter outputter = new XMLOutputter(format);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        outputter.output(doc, baos);
        fail("Illegal surrogate pair output should have thrown an exception");
      }
      catch (IllegalDataException e) {
    	  // do nothing
      } catch (Exception e) {
    	  fail ("Unexpected exception " + e.getClass());
      }
    }
    
    
	@Test
	public void testXMLOutputter() {
		XMLOutputter out = new XMLOutputter();
		TestFormat.checkEquals(out.getFormat(), Format.getRawFormat());
	}


	@Test
	public void testXMLOutputterFormat() {
		Format mine = Format.getCompactFormat();
		mine.setEncoding("US-ASCII");
		XMLOutputter out = new XMLOutputter(mine);
		TestFormat.checkEquals(mine, out.getFormat());
	}

	@Test
	public void testXMLOutputterXMLOutputter() {
		Format mine = Format.getCompactFormat();
		XMLOutputProcessor xoutp = new XMLOutputter().getXMLOutputProcessor();
		mine.setEncoding("US-ASCII");
		// double-constcut it.
		XMLOutputter out = new XMLOutputter(new XMLOutputter(mine));
		TestFormat.checkEquals(mine, out.getFormat());
		assertTrue(xoutp == out.getXMLOutputProcessor());
	}

	@Test
	public void testXMLOutputterXMLOutputProcessor() {
		XMLOutputProcessor xoutp = new AbstractXMLOutputProcessor() {
			// nothing;
		};
		// double-constrcut it.
		XMLOutputter out = new XMLOutputter(xoutp);
		TestFormat.checkEquals(Format.getRawFormat(), out.getFormat());
		assertTrue(xoutp == out.getXMLOutputProcessor());
	}

	@Test
	public void testFormat() {
		Format mine = Format.getCompactFormat();
		mine.setEncoding("US-ASCII");
		// double-constcut it.
		XMLOutputter out = new XMLOutputter();
		TestFormat.checkEquals(Format.getRawFormat(), out.getFormat());
		out.setFormat(mine);
		TestFormat.checkEquals(mine, out.getFormat());
	}

	@Test
	public void testXMLOutputProcessor() {
		XMLOutputProcessor xoutp = new AbstractXMLOutputProcessor() {
			// nothing;
		};
		// double-constcut it.
		XMLOutputter out = new XMLOutputter();
		XMLOutputProcessor xop = out.getXMLOutputProcessor();
		out.setXMLOutputProcessor(xoutp);
		assertTrue(xoutp != xop);
		assertTrue(xoutp == out.getXMLOutputProcessor());
	}

	@Test
	public void testEscapeAttributeEntities() {
		Map<String,String> totest = new LinkedHashMap<String,String>();
		
		totest.put("\"", "&quot;");
		totest.put("&", "&amp;");
		totest.put("<", "&lt;");
		totest.put(">", "&gt;");
		totest.put("\t", "&#x9;");
		totest.put("\r", "&#xD;");
		totest.put("\n", "&#xA;");
		totest.put("\nz\n\n", "&#xA;z&#xA;&#xA;");
		
		totest.put("'", "'");

		totest.put("Frodo's Journey", "Frodo's Journey");
		
		
		XMLOutputter out = new XMLOutputter();
		
		for (Map.Entry<String,String> me : totest.entrySet()) {
			if (!me.getValue().equals(out.escapeAttributeEntities(me.getKey()))) {
				assertEquals("Failed attempt to escape '" + me.getKey() + "'", 
						me.getValue(), out.escapeAttributeEntities(me.getKey()));
			}
		}
	}

	@Test
	public void testEscapeElementEntities() {
		Map<String,String> totest = new LinkedHashMap<String,String>();
		
		totest.put("\"", "\"");
		totest.put("&", "&amp;");
		totest.put("<", "&lt;");
		totest.put(">", "&gt;");
		totest.put("> >>", "&gt; &gt;&gt;");
		totest.put("\t", "\t");
		totest.put("\r", "&#xD;");
		totest.put("\n", "\r\n");
		
		totest.put("'", "'");

		totest.put("Frodo's Journey", "Frodo's Journey");
		
		
		XMLOutputter out = new XMLOutputter();
		
		for (Map.Entry<String,String> me : totest.entrySet()) {
			if (!me.getValue().equals(out.escapeElementEntities(me.getKey()))) {
				assertEquals("Failed attempt to escape '" + me.getKey() + "'", 
						me.getValue(), out.escapeElementEntities(me.getKey()));
			}
		}
	}


	@Test
	public void testOutputText() {
		checkOutput(new Text(" hello  there  "), " hello  there  ", "hello there", "hello  there", " hello  there  ");
	}

	@Test
	public void testOutputCDATA() {
		String indata = "   hello   there  bozo !   ";
		String rawcdata   = "<![CDATA[   hello   there  bozo !   ]]>";
		String compdata   = "<![CDATA[hello there bozo !]]>";
		String prettydata = "<![CDATA[hello   there  bozo !]]>";
		String trimdata   = "<![CDATA[   hello   there  bozo !   ]]>";
		
		checkOutput(new CDATA(indata), rawcdata, compdata, prettydata, trimdata);
	}

	@Test
	public void testOutputComment() {
		String incomment = "   hello   there  bozo !   ";
		String outcomment = "<!--" + incomment + "-->";
		checkOutput(new Comment(incomment), outcomment, outcomment, outcomment, outcomment);
	}

	@Test
	public void testOutputProcessingInstructionSimple() {
		ProcessingInstruction inpi = new ProcessingInstruction("jdomtest", "");
		String outpi = "<?jdomtest?>";
		checkOutput(inpi, outpi, outpi, outpi, outpi);
	}

	@Test
	public void testOutputProcessingInstructionData() {
		String pi = "  hello   there  ";
		ProcessingInstruction inpi = new ProcessingInstruction("jdomtest", pi);
		String outpi = "<?jdomtest " + pi + "?>";
		checkOutput(inpi, outpi, outpi, outpi, outpi);
	}

	@Test
	public void testOutputEntityRef() {
		checkOutput(new EntityRef("name", "publicID", "systemID"),
				"&name;", "&name;", "&name;", "&name;");
	}

	@Test
	public void testOutputElementSimple() {
		String txt = "<root />";
		checkOutput(new Element("root"), txt, txt, txt, txt);
	}

	@Test
	public void testOutputElementAttribute() {
		String txt = "<root att=\"val\" />";
		checkOutput(new Element("root").setAttribute("att", "val"), txt, txt, txt, txt);
	}

	@Test
	public void testOutputElementCDATA() {
		String txt = "<root><![CDATA[xx]]></root>";
		Element root = new Element("root");
		root.addContent(new CDATA("xx"));
		checkOutput(root, txt, txt, txt, txt);
	}

	@Test
	public void testOutputElementExpandEmpty() {
		String txt = "<root></root>";
		FormatSetup setup = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(new Element("root"), setup, txt, txt, txt, txt);
	}

	@Test
	public void testOutputElementPreserveSpace() {
		String txt = "<root xml:space=\"preserve\">    <child xml:space=\"default\">abc</child> </root>";
		Element root = new Element("root");
		root.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
		root.addContent("    ");
		Element child = new Element("child");
		child.setAttribute("space", "default", Namespace.XML_NAMESPACE);
		child.addContent("abc");
		root.addContent(child);
		root.addContent(" ");
		checkOutput(root, txt, txt, txt, txt);
	}
	
	@Test
	public void testOutputElementIgnoreTrAXEscapingPIs() {
		Element root = new Element("root");
		root.addContent(new Text("&"));
		root.addContent(new ProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, ""));
		root.addContent(new Text(" && "));
		root.addContent(new ProcessingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, ""));
		root.addContent(new Text("&"));
		String expect = "<root>&amp; && &amp;</root>";
		String excompact = "<root>&amp;&&&amp;</root>";
		String expretty = "<root>\r\n  &amp;\r\n  \r\n  &&\r\n  \r\n  &amp;\r\n</root>";
		String extfw = "<root>\r\n  &amp;\r\n  \r\n   && \r\n  \r\n  &amp;\r\n</root>";
		checkOutput(root,
				expect, 
				excompact, 
				expretty, 
				extfw);
	}
	

	@Test
	public void testOutputElementMultiText() {
		Element root = new Element("root");
		root.addContent(new CDATA(" "));
		root.addContent(new Text(" xx "));
		root.addContent(new Text("yy"));
		root.addContent(new Text("    "));
		root.addContent(new Text("zz"));
		root.addContent(new Text("  ww"));
		root.addContent(new EntityRef("amp"));
		root.addContent(new Text("vv"));
		root.addContent(new Text("  "));
		checkOutput(root,
				"<root><![CDATA[ ]]> xx yy    zz  ww&amp;vv  </root>", 
				"<root>xx yy zz ww&amp;vv</root>",
				"<root>xx yy    zz  ww&amp;vv</root>",
				// This should be changed with issue #31.
				// The real value should have one additional
				// space at the beginning and two at the end
				// for now we leave the broken test here because it
				// helps with the coverage reports.
				// the next test is added to be a failing test.
				"<root><![CDATA[ ]]> xx yy    zz  ww&amp;vv  </root>");
	}

	@Test
	public void testOutputElementMultiAllWhite() {
		Element root = new Element("root");
		root.addContent(new CDATA(" "));
		root.addContent(new Text(" "));
		root.addContent(new Text("    "));
		root.addContent(new Text(""));
		root.addContent(new Text(" "));
		root.addContent(new Text("  \n \n "));
		root.addContent(new Text("  \t "));
		root.addContent(new Text("  "));
		checkOutput(root,
				"<root><![CDATA[ ]]>        \r\n \r\n   \t   </root>", 
				"<root />",
				"<root />",
				"<root />");
	}

	@Test
	public void testOutputElementMultiAllWhiteExpandEmpty() {
		Element root = new Element("root");
		root.addContent(new CDATA(" "));
		root.addContent(new Text(" "));
		root.addContent(new Text("    "));
		root.addContent(new Text(""));
		root.addContent(new Text(" "));
		root.addContent(new Text("  \n \n "));
		root.addContent(new Text("  \t "));
		root.addContent(new Text("  "));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><![CDATA[ ]]>        \r\n \r\n   \t   </root>", 
				"<root></root>",
				"<root></root>",
				"<root></root>");
	}

	@Test
	public void testOutputElementMultiMostWhiteExpandEmpty() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		Element root = new Element("root");
		root.addContent(new CDATA(" "));
		root.addContent(new Text(" "));
		root.addContent(new Text("    "));
		root.addContent(new Text(""));
		root.addContent(new Text(" "));
		root.addContent(new Text("  \n \n "));
		root.addContent(new Comment("Boo"));
		root.addContent(new Text("  \t "));
		root.addContent(new Text("  "));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><![CDATA[ ]]>        \r\n \r\n <!--Boo-->  \t   </root>", 
				"<root><!--Boo--></root>",
				"<root>\r\n  <!--Boo-->\r\n</root>",
				"<root>\r\n  <!--Boo-->\r\n</root>");
	}

	@Test
	public void testOutputElementMixedMultiCDATA() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		Element root = new Element("root");
		root.addContent(new Comment("Boo"));
		root.addContent(new Text(" "));
		root.addContent(new CDATA("A"));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><!--Boo--> <![CDATA[A]]></root>", 
				"<root><!--Boo--><![CDATA[A]]></root>",
				"<root>\r\n  <!--Boo-->\r\n  <![CDATA[A]]>\r\n</root>",
				"<root>\r\n  <!--Boo-->\r\n   <![CDATA[A]]>\r\n</root>");
	}

	@Test
	public void testOutputElementMixedMultiEntityRef() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		Element root = new Element("root");
		root.addContent(new Comment("Boo"));
		root.addContent(new Text(" "));
		root.addContent(new EntityRef("aer"));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><!--Boo--> &aer;</root>", 
				"<root><!--Boo-->&aer;</root>",
				"<root>\r\n  <!--Boo-->\r\n  &aer;\r\n</root>",
				"<root>\r\n  <!--Boo-->\r\n   &aer;\r\n</root>");
	}

	@Test
	public void testOutputElementMixedMultiText() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		Element root = new Element("root");
		root.addContent(new Comment("Boo"));
		root.addContent(new Text(" "));
		root.addContent(new Text("txt"));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><!--Boo--> txt</root>", 
				"<root><!--Boo-->txt</root>",
				"<root>\r\n  <!--Boo-->\r\n  txt\r\n</root>",
				"<root>\r\n  <!--Boo-->\r\n   txt\r\n</root>");
	}

	@Test
	public void testOutputElementMixedMultiZeroText() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		Element root = new Element("root");
		root.addContent(new Comment("Boo"));
		root.addContent(new Text(""));
		root.addContent(new Text(" "));
		root.addContent(new Text(""));
		root.addContent(new Text("txt"));
		root.addContent(new Text(""));
		FormatSetup fs = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setExpandEmptyElements(true);
			}
		};
		checkOutput(root, fs,  
				"<root><!--Boo--> txt</root>", 
				"<root><!--Boo-->txt</root>",
				"<root>\r\n  <!--Boo-->\r\n  txt\r\n</root>",
				"<root>\r\n  <!--Boo-->\r\n   txt\r\n</root>");
	}

	@Test
	public void testOutputElementMultiEntityLeftRight() {
		Element root = new Element("root");
		root.addContent(new EntityRef("erl"));
		root.addContent(new Text(" "));
		root.addContent(new Text("    "));
		root.addContent(new EntityRef("err"));
		checkOutput(root,
				"<root>&erl;     &err;</root>", 
				"<root>&erl; &err;</root>",
				"<root>&erl;     &err;</root>",
				"<root>&erl;     &err;</root>");
	}

	@Test
	public void testOutputElementMultiTrimLeftRight() {
		Element root = new Element("root");
		root.addContent(new Text(" tl "));
		root.addContent(new Text(" mid "));
		root.addContent(new Text(" tr "));
		checkOutput(root,
				"<root> tl  mid  tr </root>", 
				"<root>tl mid tr</root>",
				"<root>tl  mid  tr</root>",
				"<root> tl  mid  tr </root>");
	}

	@Test
	public void testOutputElementMultiCDATALeftRight() {
		Element root = new Element("root");
		root.addContent(new CDATA(" tl "));
		root.addContent(new Text(" mid "));
		root.addContent(new CDATA(" tr "));
		checkOutput(root,
				"<root><![CDATA[ tl ]]> mid <![CDATA[ tr ]]></root>", 
				"<root><![CDATA[tl]]> mid <![CDATA[tr]]></root>",
				"<root><![CDATA[tl ]]> mid <![CDATA[ tr]]></root>",
				"<root><![CDATA[ tl ]]> mid <![CDATA[ tr ]]></root>");
	}

	@Test
	public void testTrimFullWhite() {
		// See issue #31.
		// https://github.com/hunterhacker/jdom/issues/31
		// This tests should pass when issue 31 is resolved.
		Element root = new Element("root");
		root.addContent(new Text(" "));
		root.addContent(new Text("x"));
		root.addContent(new Text(" "));
		Format mf = Format.getRawFormat();
		mf.setTextMode(TextMode.TRIM_FULL_WHITE);
		XMLOutputter xout = new XMLOutputter(mf);
		String output = xout.outputString(root);
		assertEquals("<root> x </root>", output);
	}

	@Test
	public void testOutputElementNamespaces() {
		String txt = "<ns:root xmlns:ns=\"myns\" xmlns:ans=\"attributens\" xmlns:two=\"two\" ans:att=\"val\" />";
		Element emt = new Element("root", Namespace.getNamespace("ns", "myns"));
		Namespace ans = Namespace.getNamespace("ans", "attributens");
		emt.setAttribute(new Attribute("att", "val", ans));
		emt.addNamespaceDeclaration(Namespace.getNamespace("two", "two"));
		checkOutput(emt,
				txt, txt, txt, txt);
	}

	@Test
	public void testOutputDocTypeSimple() {
		checkOutput(new DocType("root"), "<!DOCTYPE root>", "<!DOCTYPE root>", "<!DOCTYPE root>", "<!DOCTYPE root>");
	}

	@Test
	public void testOutputDocTypeInternalSubset() {
		String dec = "<!DOCTYPE root [\r\ninternal]>";
		DocType dt = new DocType("root");
		dt.setInternalSubset("internal");
		checkOutput(dt, dec, dec, dec, dec);
	}

	@Test
	public void testOutputDocTypeSystem() {
		String dec = "<!DOCTYPE root SYSTEM \"systemID\">";
		checkOutput(new DocType("root", "systemID"), dec, dec, dec, dec);
	}

	@Test
	public void testOutputDocTypePublic() {
		String dec = "<!DOCTYPE root PUBLIC \"publicID\">";
		checkOutput(new DocType("root", "publicID", null), dec, dec, dec, dec);
	}

	@Test
	public void testOutputDocTypePublicSystem() {
		String dec = "<!DOCTYPE root PUBLIC \"publicID\" \"systemID\">";
		checkOutput(new DocType("root", "publicID", "systemID"), dec, dec, dec, dec);
	}

	@Test
	public void testOutputDocumentSimple() {
		Document doc = new Document();
		doc.addContent(new Element("root"));
		String xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String rtdec = "<root />";
		checkOutput(doc, 
				xmldec + "\r\n" + rtdec + "\r\n", 
				xmldec + "\r\n" + rtdec + "\r\n",
				xmldec + "\r\n" + rtdec + "\r\n",
				xmldec + "\r\n" + rtdec + "\r\n");
	}

	@Test
	public void testOutputDocumentOmitEncoding() {
		Document doc = new Document();
		doc.addContent(new Element("root"));
		String xmldec = "<?xml version=\"1.0\"?>";
		FormatSetup setup = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setOmitEncoding(true);
			}
		};
		String rtdec = "<root />";
		checkOutput(doc, setup,
				xmldec + "\r\n" + rtdec + "\r\n", 
				xmldec + "\r\n" + rtdec + "\r\n",
				xmldec + "\r\n" + rtdec + "\r\n",
				xmldec + "\r\n" + rtdec + "\r\n");
	}

	@Test
	public void testOutputDocumentOmitDeclaration() {
		Document doc = new Document();
		doc.addContent(new Element("root"));
		FormatSetup setup = new FormatSetup() {
			@Override
			public void setup(Format fmt) {
				fmt.setOmitDeclaration(true);
			}
		};
		String rtdec = "<root />";
		checkOutput(doc, setup,
				rtdec + "\r\n", 
				rtdec + "\r\n",
				rtdec + "\r\n",
				rtdec + "\r\n");
	}

	@Test
	public void testOutputDocumentFull() {
		DocType dt = new DocType("root");
		Comment comment = new Comment("comment");
		ProcessingInstruction pi = new ProcessingInstruction("jdomtest", "");
		Element root = new Element("root");
		Document doc = new Document();
		doc.addContent(dt);
		doc.addContent(comment);
		doc.addContent(pi);
		doc.addContent(root);
		String xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String dtdec = "<!DOCTYPE root>";
		String commentdec = "<!--comment-->";
		String pidec = "<?jdomtest?>";
		String rtdec = "<root />";
		String lf = "\r\n";
		checkOutput(doc, 
				xmldec + lf + dtdec + lf + commentdec + pidec + rtdec + lf, 
				xmldec + lf + dtdec + lf + commentdec + pidec + rtdec + lf,
				xmldec + lf + dtdec + lf + lf + commentdec + lf + pidec  + lf + rtdec + lf,
				xmldec + lf + dtdec + lf + lf + commentdec + lf + pidec  + lf + rtdec + lf);
	}
	
	@Test
	public void testDeepNesting() {
		// need to get beyond 16 levels of XML.
		DocType dt = new DocType("root");
		Element root = new Element("root");
		Document doc = new Document();
		doc.addContent(dt);
		doc.addContent(root);
		String xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String dtdec = "<!DOCTYPE root>";
		String lf = "\r\n";
		
		String base = xmldec + lf + dtdec + lf;
		StringBuilder raw = new StringBuilder(base);
		StringBuilder pretty = new StringBuilder(base);
		raw.append("<root>");
		pretty.append(lf);
		pretty.append("<root>");
		pretty.append(lf);
		final int depth = 40;
		int cnt = depth;
		Parent parent = root;
		StringBuilder indent = new StringBuilder();
		while (--cnt > 0) {
			Element emt = new Element("emt");
			parent.getContent().add(emt);
			parent = emt;
			raw.append("<emt>");
			indent.append("  ");
			pretty.append(indent.toString());
			pretty.append("<emt>");
			pretty.append(lf);
		}
		
		parent.getContent().add(new Element("bottom"));
		raw.append("<bottom />");
		pretty.append(indent.toString());
		pretty.append("  <bottom />");
		pretty.append(lf);
		
		cnt = depth;
		while (--cnt > 0) {
			raw.append("</emt>");
			pretty.append(indent.toString());
			pretty.append("</emt>");
			indent.setLength(indent.length() - 2);
			pretty.append(lf);
		}
		raw.append("</root>");
		raw.append(lf);
		pretty.append("</root>");
		pretty.append(lf);
		
		checkOutput(doc, raw.toString(), raw.toString(), pretty.toString(), pretty.toString()); 
	}
	
	@Test
	public void testOutputElementContent() {
		Element root = new Element("root");
		root.addContent(new Element("child"));
		checkOutput(root, "outputElementContent", Element.class, null, "<child />", "<child />", "<child />\r\n", "<child />\r\n");
	}

	@Test
	public void testOutputList() {
		List<Object> c = new ArrayList<Object>();
		c.add(new Element("root"));
		checkOutput(c, "output", List.class, null, "<root />", "<root />", "<root />\r\n", "<root />\r\n");
	}

	@Test
	public void testClone() {
		XMLOutputter xo = new XMLOutputter();
		assertTrue(xo != xo.clone());
	}

	@Test
	public void testToString() {
		Format fmt = Format.getCompactFormat();
		fmt.setLineSeparator("\r\n\t ");
		XMLOutputter out = new XMLOutputter(fmt);
		assertNotNull(out.toString());
	}
	
	private interface FormatSetup {
		public void setup(Format fmt);
	}
	
	private void checkOutput(Object content, String raw, String compact, String pretty, String trimfw) {
		Class<?> clazz = content.getClass();
		checkOutput(content, "output", clazz, null, raw, compact, pretty, trimfw);
	}
	private void checkOutput(Object content, FormatSetup setup, String raw, String compact, String pretty, String trimfw) {
		Class<?> clazz = content.getClass();
		checkOutput(content, "output", clazz, setup, raw, compact, pretty, trimfw);
	}
	
	/**
	 * The following method will run the output data through each of the three base
	 * formatters, raw, compact, and pretty. It will also run each of those
	 * formatters as the outputString(content), output(content, OutputStream)
	 * and output(content, Writer).
	 * 
	 * The expectation is that the results of the three output forms (String,
	 * OutputStream, and Writer) will be identical, and that it will match
	 * the expected value for the appropriate formatter.
	 * 
	 * @param content The content to output
	 * @param methodprefix What the methods are called
	 * @param clazz The class used as the parameter for the methods.
	 * @param setup A callback mechanism to modify the formatters
	 * @param raw  What we expect the content to look like with the RAW format
	 * @param compact What we expect the content to look like with the COMPACT format
	 * @param pretty What we expect the content to look like with the PRETTY format
	 * @param trimfw What we expect the content to look like with the TRIM_FULL_WHITE format
	 */
	private void checkOutput(Object content, String methodprefix, Class<?> clazz, 
			FormatSetup setup, String raw, String compact, String pretty, String trimfw) {
		Method mstring = getMethod(methodprefix + "String", clazz);
		Method mstream = getMethod(methodprefix, clazz, OutputStream.class);
		Method mwriter = getMethod(methodprefix, clazz, Writer.class);
		
		String[] descn   = new String[] {"Raw", "Compact", "Pretty", "TrimFullWhite"};
		Format ftrimfw = Format.getPrettyFormat();
		ftrimfw.setTextMode(TextMode.TRIM_FULL_WHITE);
		Format[] formats = new Format[] {
				getFormat(setup, Format.getRawFormat()), 
				getFormat(setup, Format.getCompactFormat()),
				getFormat(setup, Format.getPrettyFormat()),
				getFormat(setup, ftrimfw)};
		String[] result  = new String[] {raw, compact, pretty, trimfw};
		
		for (int i = 0; i < 4; i++) {
			XMLOutputter out = new XMLOutputter(formats[i]);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(result[i].length() * 2);
			CharArrayWriter caw = new CharArrayWriter(result[i].length() + 2);
			try {
				if (mstring != null) {
					String rstring = (String) mstring.invoke(out, content);
					assertEquals("outputString Format " + descn[i], result[i], rstring);
				}
				if (mstream != null) {
					mstream.invoke(out, content, baos);
					String rstream = new String(baos.toByteArray());
					assertEquals("output OutputStream Format " + descn[i], result[i], rstream);
				}
				if (mwriter != null) {
					mwriter.invoke(out, content, caw);
					String rwriter = String.valueOf(caw.toCharArray());
					assertEquals("output Writer Format " + descn[i], result[i], rwriter);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Failed to process " + descn[i] + " on content " + clazz + ": " + e.getMessage());
			}
		}
	}
	
	private Format getFormat(FormatSetup setup, Format input) {
		if (setup == null) {
			return input;
		}
		setup.setup(input);
		return input;
	}
	
	private Method getMethod(String name, Class<?>...classes) {
		try {
			return XMLOutputter.class.getMethod(name, classes);
		} catch (Exception e) {
			// ignore.
			System.out.println("Can't find " + name + ": " + e.getMessage());
		}
		return null;
	}

    
}