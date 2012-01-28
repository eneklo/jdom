/*--

 Copyright (C) 2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

package org.jdom2.test.cases.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.NamespaceAware;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.test.util.UnitTestUtil;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathDiagnostic;
import org.jdom2.xpath.XPathFactory;

@SuppressWarnings({"javadoc"})
public abstract class AbstractTestXPathCompiled {
	
	private final Document doc = new Document();
	
	private final Comment doccomment = new Comment("doc comment");
	private final ProcessingInstruction docpi = new ProcessingInstruction("jdomtest", "doc");
	
	private final Element main = new Element("main");
	private final Attribute mainatt = new Attribute("atta", "vala");
	private final Comment maincomment = new Comment("main comment");
	private final ProcessingInstruction mainpi = new ProcessingInstruction("jdomtest", "pi data");
	private final Text maintext1 = new Text(" space1 ");
	private final Element child1emt = new Element("child");
	private final Text child1text = new Text("child1text");
	private final Text maintext2 = new Text(" space2 ");
	private final Element child2emt = new Element("child");
	
	private final Namespace child3nsa = Namespace.getNamespace("c3nsa", "jdom:c3nsa");
	private final Namespace child3nsb = Namespace.getNamespace("c3nsb", "jdom:c3nsb");
	private final Element child3emt = new Element("child", child3nsa);
	private final Attribute child3attint = new Attribute("intatt", "-123", child3nsb);
	private final Attribute child3attdoub = new Attribute("doubatt", "-123.45", child3nsb);
	private final Text child3txt = new Text("c3text");
	
	private final String mainvalue = " space1 child1text space2 c3text";
	
	public AbstractTestXPathCompiled() {
		doc.addContent(doccomment);
		doc.addContent(docpi);
		doc.addContent(main);
		main.setAttribute(mainatt);
		main.addContent(maincomment);
		main.addContent(mainpi);
		main.addContent(maintext1);
		child1emt.addContent(child1text);
		main.addContent(child1emt);
		main.addContent(maintext2);
		main.addContent(child2emt);
		child3emt.setAttribute(child3attint);
		child3emt.setAttribute(child3attdoub);
		child3emt.addContent(child3txt);
		main.addContent(child3emt);
	}
	
	/**
	 * Create an instance of an XPath.
	 * Override this method to create the type of XPath instance we want to test.
	 * The method should add the supplied Namspace keys, and set the given variables
	 * on the XPath.
	 * @param path
	 * @param variables
	 * @param namespaces
	 * @return
	 * @throws JDOMException
	 */
	abstract XPathFactory getFactory();
	
	private <T> XPathExpression<T> setupXPath(Filter<T> filter, String path, Map<String, Object> variables, Object context, Namespace... namespaces) {
		
		XPathBuilder<T> xpath = new XPathBuilder<T>(path, filter);
		
		assertFalse(xpath.equals(null));
		assertFalse(xpath.equals(new Object()));
		UnitTestUtil.checkEquals(xpath, xpath);
		
		assertEquals("getXPath()", path, xpath.getExpression());
		
		
		if (variables != null) {
			for (Map.Entry<String, Object> me : variables.entrySet()) {
				xpath.setVariable(me.getKey(), me.getValue());
			}
		}
		if (context instanceof NamespaceAware) {
			xpath.setNamespaces(((NamespaceAware)context).getNamespacesInScope());
		}
		for (Namespace n : namespaces) {
			xpath.setNamespace(n);
		}
		
		return xpath.compileWith(getFactory());
		
	}
	
	private static final void checkDiagnostic(XPathExpression<?> xpc, Object context, List<?> result, XPathDiagnostic<?> diag) {
		assertTrue(xpc == diag.getXPathExpression());
		assertTrue(context == diag.getContext());
		
		assertTrue(null != diag.toString());
		
		assertFalse(diag.isFirstOnly());
		
		final List<?> dresult = diag.getResult();
		final List<?> draw = diag.getRawResults();
		final List<?> dfilt = diag.getFilteredResults();
		
		assertTrue(dresult.size() == result.size());
		
		for (int i = 0; i < result.size(); i++) {
			assertEquals(dresult.get(i), result.get(i));
		}
		assertTrue(dresult.size() + dfilt.size() == draw.size());
		
		int r = 0;
		int f = 0;
		for (int i = 0; i < draw.size(); i++) {
			if (dresult.get(r) == draw.get(i)) {
				r++;
			} else if (dfilt.get(f)== draw.get(i)) {
				f++;
			} else {
				fail (draw.get(i) + " is neither a result nor a filtered (or is in the wrong place)");
			}
		}
		
	}
	
	/**
	 * A mechanism for exercising the XPath system.
	 * @param xpath The xpath to run.
	 * @param context The context on which to run the XPath
	 * @param string What we expect the 'xpath' string value of the result to be (or null to skip test).
	 * @param number What we expect the xpath to resolve the result to be as an xpath 'Number' (or null to skip test);
	 * @param expect The nodes we expect from the XPath selectNodes query
	 */
	private static void checkXPath(XPathExpression<?> xpath, Object context, Object...expect) {
		
		// Check the selectNodes operation.
		List<?> result = xpath.evaluate(context);
		if (result == null) {
			fail ("Got a null result from selectNodes()");
		}
		checkDiagnostic(xpath, context, result, xpath.diagnose(context, false));
				
		String sze = result.size() == expect.length ? "" : 
			(" Also Different Sizes: expect=" + expect.length + " actual=" + result.size());
		int pos = 0;
		for (Object o : result) {
			if (pos >= expect.length) {
				fail ("Results contained additional content at position " + 
						pos + " for xpath '" + xpath + "': " + o + sze);
			}
			if (o != expect[pos]) {
				assertEquals("Failed result at position " + pos + 
						" for xpath '" + xpath + "'." + sze, expect[pos], o);
			}
			pos++;
		}
		if (pos < expect.length) {
			fail ("Results are missing " + (expect.length - pos) + 
					" content at position " + pos + " for xpath '" + xpath + 
					"'. First missing content is: " + expect[pos] + sze);
		}
		
		// Check the selectSingleNode operation.
		Object o = xpath.evaluateFirst(context);
		//checkDiagnostic(Collections.singletonList(o), xpath.diagnose(context, true));
		if (expect.length == 0 && o != null) {
			fail("Expected XPath.selectSingleNode() to return nothing, " +
					"but it returned " + o + sze);
		}
		if (expect.length > 0 && o == null) {
			fail("XPath.selectSingleNode() returned nothing, but it should " +
					"have returned " + expect[0] + sze);
		}
		if (expect.length > 0 && o != expect[0]) {
			assertEquals("XPath.selectSingleNode() was expected to return " + 
					expect[0] + "' but instead it returned '" + o + "'" + sze,
					expect[0], o);
		}
		
	}
	
	private void checkXPath(String xpath, Object context, String value, Object...expect) {
		checkXPath(setupXPath(Filters.fpassthrough(), xpath, null, context), context, expect);
		if (value != null) {
			String npath = "string(" + xpath + ")";
			checkXPath(setupXPath(Filters.fstring(), npath, null, context), context, value);
		}
	}

	private void checkComplexXPath(String xpath, Object context, Map<String, Object> variables, 
			Collection<Namespace> namespaces, String value, Number number, Object...expect) {
		HashSet<Namespace> nset = new HashSet<Namespace>();
		if (namespaces != null) {
			nset.addAll(namespaces);
		}
		if (context instanceof NamespaceAware) {
			nset.addAll(((NamespaceAware)context).getNamespacesInScope());
		}
				
		Namespace[] nsa = nset.toArray(new Namespace[0]);
		checkXPath(setupXPath(Filters.fpassthrough(), xpath, variables, context, nsa), context, expect);
		if (value != null) {
			String npath = "string(" + xpath + ")";
			checkXPath(setupXPath(Filters.fstring(), npath, variables, context, nsa), context, value);
		}
		if (number != null) {
			String npath = "number(" + xpath + ")";
			checkXPath(setupXPath(Filters.fdouble(), npath, variables, context, nsa), context, number);
		}
	}

//	@Test
//	public void testSerialization() {
//		XPath xpath = setupXPath("//main", null);
//		XPath xser  = UnitTestUtil.deSerialize(xpath);
//		assertTrue(xpath != xser);
//		// TODO JaxenXPath has useless equals(). See issue #43
//		// Additionally, all XPath deserialization is done on the default
//		// factory... will never be equals() if the factory used to create
//		// the xpath is different.
//		// UnitTestUtil.checkEquals(xpath, xser);
//		assertEquals(xpath.toString(), xser.toString());
//	}
	
	@Test
	public void testNullQuery() {
		try {
			getFactory().compile(null, Filters.element());
			fail("excpected NPE");
		} catch (NullPointerException noe) {
			// great
		}
	}
	
	@Test
	public void testNullFilter() {
		try {
			getFactory().compile("/", null);
			fail("excpected NPE");
		} catch (NullPointerException noe) {
			// great
		}
	}
	
	@Test
	public void testNullNamespace() {
		try {
			getFactory().compile("/", Filters.element(), null, Namespace.NO_NAMESPACE, null, Namespace.XML_NAMESPACE);
			fail("excpected NPE");
		} catch (NullPointerException noe) {
			// great
		}
	}
	
	@Test
	public void testNullNamespaceArray() {
		Namespace[] nsa = null;
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), null, nsa);
		assertEquals("", xp.getNamespace(""));
	}
	
	@Test
	public void testNullVariableName() {
		try {
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put(null, "");
			vars.put("a", "b");
			getFactory().compile("/", Filters.element(), vars);
			fail("excpected NPE");
		} catch (NullPointerException noe) {
			// great
		}
	}
	
	@Test
	public void testDuplicatePrefix() {
		try {
			Namespace nsa = Namespace.getNamespace("pfx", "one");
			Namespace nsb = Namespace.getNamespace("pfx", "two");
			getFactory().compile("/", Filters.element(), null, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testRedefineNO_PREFIX() {
		try {
			Namespace nsa = Namespace.getNamespace("pfx", "one");
			Namespace nsb = Namespace.getNamespace("", "two");
			getFactory().compile("/", Filters.element(), null, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testDuplicateVariable() {
		try {
			Map<String,Object> vars = new HashMap<String, Object>();
			vars.put("pfa:name", "dupa");
			vars.put("pfb:name", "dupb");
			Namespace nsa = Namespace.getNamespace("pfa", "ns");
			Namespace nsb = Namespace.getNamespace("pfb", "ns");
			getFactory().compile("/", Filters.element(), vars, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testBadVariablePrefix() {
		try {
			Map<String,Object> vars = new HashMap<String, Object>();
			vars.put("pfa : name", "dupa");
			vars.put("pfb:name", "dupb");
			Namespace nsa = Namespace.getNamespace("pfa", "ns");
			Namespace nsb = Namespace.getNamespace("pfb", "ns");
			getFactory().compile("/", Filters.element(), vars, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testBadVariableName1() {
		try {
			Map<String,Object> vars = new HashMap<String, Object>();
			vars.put("pfa:123", "dupa");
			vars.put("pfb:name", "dupb");
			Namespace nsa = Namespace.getNamespace("pfa", "ns");
			Namespace nsb = Namespace.getNamespace("pfb", "ns");
			getFactory().compile("/", Filters.element(), vars, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testBadVariableName2() {
		try {
			Map<String,Object> vars = new HashMap<String, Object>();
			vars.put("pfa: ", "dupa");
			vars.put("pfb:name", "dupb");
			Namespace nsa = Namespace.getNamespace("pfa", "ns");
			Namespace nsb = Namespace.getNamespace("pfb", "ns");
			getFactory().compile("/", Filters.element(), vars, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testBadVariableNamespace() {
		try {
			Map<String,Object> vars = new HashMap<String, Object>();
			// pfd is not defined.
			vars.put("pfd:name", "dupa");
			vars.put("pfb:name", "dupb");
			Namespace nsa = Namespace.getNamespace("pfa", "ns");
			Namespace nsb = Namespace.getNamespace("pfb", "ns");
			getFactory().compile("/", Filters.element(), vars, Namespace.NO_NAMESPACE, nsa, Namespace.XML_NAMESPACE, nsb);
			fail("excpected IAE");
		} catch (IllegalArgumentException noe) {
			// great
		}
	}
	
	@Test
	public void testGetNamespace1() {
		assertEquals("", getFactory().compile("/").getNamespace(""));
	}
	
	@Test
	public void testGetNamespace2() {
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), null, Namespace.getNamespace("x", "y"));
		assertEquals("y", xp.getNamespace("x"));
	}
	
	@Test
	public void testGetNamespace3() {
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), null, Namespace.getNamespace("x", "y"));
		try {
			xp.getNamespace("hello");
			fail("expected IAE");
		} catch (IllegalArgumentException ise) {
			// good.
		}
	}
	
	@Test
	public void testGetVariable1() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("nsa:one", 2);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		
		assertEquals(1, xp.getVariable("", "one"));
		assertEquals(2, xp.getVariable("zzz", "one"));
	}
	
	@Test
	public void testGetVariable2() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("three", 3);
		vars.put("nsa:one", 2);
		vars.put("nsa:two", 2);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		
		try {
			xp.getVariable("", "two");
			fail("expected IAE");
		} catch (IllegalArgumentException ise) {
			// good.
		}
		try {
			xp.getVariable("zzz", "three");
			fail("expected IAE");
		} catch (IllegalArgumentException ise) {
			// good.
		}
	}
	
	@Test
	public void testGetNullVariableValue() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("nsa:one", null);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		
		assertEquals(1, xp.getVariable("", "one"));
		assertEquals(null, xp.getVariable("zzz", "one"));
	}
	
	@Test
	public void testSetNullVariableValue() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("nsa:one", 2);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		
		assertEquals(1, xp.getVariable("", "one"));
		assertEquals(2, xp.getVariable("zzz", "one"));
		
		assertEquals(2, xp.setVariable("zzz", "one", null));
		assertEquals(1, xp.getVariable("", "one"));
		assertEquals(null, xp.getVariable("zzz", "one"));
		
		assertEquals(null, xp.setVariable("zzz", "one", 3));
		assertEquals(1, xp.getVariable("", "one"));
		assertEquals(3, xp.getVariable("zzz", "one"));
	}
	
	@Test
	public void testGetFilter() {
		Filter<Element> filter = Filters.element();
		XPathExpression<Element> xp = getFactory().compile("/", filter);
		assertTrue(filter == xp.getFilter());
	}
	
	@Test
	public void testToString() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("nsa:one", 2);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		assertTrue(null != xp.toString());
		
	}
	
	@Test
	public void testClone() {
		Map<String,Object> vars = new HashMap<String, Object>();
		vars.put("one", 1);
		vars.put("nsa:one", 2);
		XPathExpression<Element> xp = getFactory().compile("/", Filters.element(), vars, Namespace.getNamespace("nsa", "zzz"));
		XPathExpression<Element> xq = xp.clone();
		assertTrue(xp != xq);
		assertTrue(xp.getExpression() == xq.getExpression());
		assertTrue(xp.getVariable("", "one") == xq.getVariable("", "one"));
		assertTrue(xp.getVariable("zzz", "one") == xq.getVariable("zzz", "one"));
		assertTrue(xq.getVariable("", "one") == xp.setVariable("", "one", "newval"));
		assertEquals("newval", xp.getVariable("", "one"));
		assertEquals(1, xq.getVariable("", "one"));
		
	}
	
	@Test
	public void testSelectDocumentDoc() {
		checkXPath("/", doc, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentMain() {
		checkXPath("/", main, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentAttr() {
		checkXPath("/", child3attint, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentPI() {
		checkXPath("/", mainpi, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentText() {
		checkXPath("/", child1text, mainvalue, doc);
	}

	@Test
	public void testSelectMainByName() {
		checkXPath("main", doc, mainvalue, main);
	}

	@Test
	public void testSelectMainFromDoc() {
		checkXPath("//main", doc, mainvalue, main);
	}

	@Test
	public void testAncestorsFromRoot() {
		checkXPath("ancestor::node()", doc, "");
	}

	@Test
	public void testAncestorsFromMain() {
		checkXPath("ancestor::node()", main, mainvalue, doc);
	}

	@Test
	public void testAncestorsFromChild() {
		checkXPath("ancestor::node()", child1emt, mainvalue, doc, main);
	}

	@Test
	public void testAncestorOrSelfFromRoot() {
		checkXPath("ancestor-or-self::node()", doc, mainvalue, doc);
	}

	@Test
	public void testAncestorOrSelfFromMain() {
		checkXPath("ancestor-or-self::node()", main, mainvalue, doc, main);
	}

	@Test
	public void testAncestorOrSelfFromMainAttribute() {
		checkXPath("ancestor-or-self::node()", mainatt, mainvalue, doc, main, mainatt);
	}

	@Test
	public void testAncestorOrSelfFromNamespace() {
		checkXPath("ancestor-or-self::node()", child3nsa, null, child3nsa);
	}

	@Test
	public void testAncestorOrSelfFromChild() {
		checkXPath("ancestor-or-self::node()", child1emt, mainvalue, doc, main, child1emt);
	}

		
	/* *************************************
	 * Boolean/Double/String tests.
	 * ************************************* */
	
	@Test
	public void getXPathDouble() {
		checkXPath("count( //* )", doc, null, Double.valueOf(4));
	}

	@Test
	public void getXPathString() {
		checkXPath("string( . )", child1emt, null, child1text.getText());
	}

	@Test
	public void getXPathBoolean() {
		checkXPath("count (//*) > 1", child1emt, null, Boolean.TRUE);
	}

	/* *************************************
	 * Element tests.
	 * ************************************* */
	
	@Test
	public void getXPathElementName() {
		checkXPath("//*[name() = 'main']", doc, null, main);
	}

	@Test
	public void getXPathElementText() {
		checkXPath("//*[string() = 'child1text']", doc, null, child1emt);
	}

	
	/* *************************************
	 * Processing Instruction tests.
	 * ************************************* */
	
	@Test
	public void getXPathProcessingInstructionAll() {
		checkXPath("//processing-instruction()", doc, null, docpi, mainpi);
	}

	@Test
	public void getXPathProcessingInstructionByTarget() {
		checkXPath("//processing-instruction()[name() = 'jdomtest']", doc, null, docpi, mainpi);
	}

	@Test
	public void getXPathProcessingInstructionByData() {
		checkXPath("//processing-instruction()[string() = 'doc']", doc, null, docpi);
	}

	/* *************************************
	 * Attribute tests.
	 * ************************************* */
	
	@Test
	public void getXPathAttributeAll() {
		checkXPath("//@*", doc, null, mainatt, child3attint, child3attdoub);
	}

	@Test
	public void getXPathAttributeByName() {
		checkXPath("//@*[name() = 'atta']", doc, null, mainatt);
	}

	@Test
	public void getXPathAttributeByValue() {
		checkXPath("//@*[string() = '-123']", doc, null, child3attint);
	}

	/* *************************************
	 * XPath Variable tests.
	 * ************************************* */

	@Test
	public void testSetVariable() {
		HashMap<String,Object> hm = new HashMap<String, Object>();
		String attval = mainatt.getValue();
		hm.put("valvar", attval);
		checkComplexXPath("//@*[string() = $valvar]", doc, hm, null, attval, null, mainatt);
	}

	/* *************************************
	 * XPath namespace tests.
	 * ************************************* */
	@Test
	public void testAttributeNamespaceAsNumberToo() {
		checkComplexXPath("//@c3nsb:intatt", child3emt, null, null, 
				"-123", Double.valueOf(-123), child3attint);
		checkComplexXPath("//@c3nsb:doubatt", child3emt, null, null, 
				"-123.45", Double.valueOf(-123.45), child3attdoub);
	}

	@Test
	public void testAddNamespaceNamespace() {
		checkComplexXPath("//c3nsa:child", doc, null, Collections.singleton(child3nsa),
				child3emt.getValue(), null, child3emt);
	}
	
	@Test
	public void testGetALLNamespaces() {
		//Namespace.NO_NAMESPACE is declared earlier in documentOrder.
		// so it comes first.
		checkXPath("//c3nsa:child/namespace::*", child3emt, "jdom:c3nsa", 
				child3nsa, Namespace.NO_NAMESPACE, child3nsb, Namespace.XML_NAMESPACE);
	}
	
	@Test
	// This fails the Jaxen Builder because the returned attributes are not in document order.
	public void testAttributesNamespace() {
		checkComplexXPath("//@*[namespace-uri() = 'jdom:c3nsb']", doc, null, null, 
				"-123", Double.valueOf(-123), child3emt.getAttributes().toArray());
	}
	
	@Test
	public void testXPathDefaultNamespacesFromElement() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3emt, null, null, 
				"-123", Double.valueOf(-123), child3attint);
	}
	
	@Test
	public void testXPathDefaultNamespacesFromAttribute() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3attdoub, null, null, 
				"-123", Double.valueOf(-123), child3attint);
	}
	
	@Test
	public void testXPathDefaultNamespacesFromText() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3txt, null, null, 
				"-123", Double.valueOf(-123), child3attint);
	}
	
	/* *******************************
	 * Axis TestCases
	 * ******************************* */
	
	@Test
	public void testXPathAncestor() {
		checkXPath("ancestor::*", child3txt, null, main, child3emt);
	}
	
	@Test
	public void testXPathAncestorOrSelf() {
		checkXPath("ancestor-or-self::*", child3txt, null, main, child3emt);
	}
	
	@Test
	public void testXPathAncestorNodes() {
		checkXPath("ancestor::node()", child3txt, null, doc, main, child3emt);
	}
	
	@Test
	public void testXPathAncestorOrSelfNodes() {
		checkXPath("ancestor-or-self::node()", child3txt, null, doc, main, child3emt, child3txt);
	}
	
	@Test
	public void testXPathAncestorOrSelfNodesFromAtt() {
		checkXPath("ancestor-or-self::node()", child3attint, null, doc, main, child3emt, child3attint);
	}
	
	@Test
	public void testXPathAttributes() {
		checkXPath("attribute::*", child3emt, null, child3attint, child3attdoub);
	}
	
	@Test
	public void testXPathChild() {
		checkXPath("child::*", main, null, child1emt, child2emt, child3emt);
	}
	
	@Test
	public void testXPathDescendant() {
		checkXPath("descendant::*", doc, null, main, child1emt, child2emt, child3emt);
	}
	
	@Test
	public void testXPathDescendantNode() {
		checkXPath("descendant::node()", doc, null, doccomment, docpi, main,
				maincomment, mainpi, maintext1, child1emt, child1text,
				maintext2, child2emt, child3emt, child3txt);
	}
	
	@Test
	public void testXPathDescendantOrSelf() {
		checkXPath("descendant-or-self::*", doc, null, main, child1emt, child2emt, child3emt);
	}
	
	@Test
	public void testXPathFollowing() {
		checkXPath("following::*", child2emt, null, child3emt);
	}
	
	@Test
	public void testXPathFollowingNode() {
		checkXPath("following::node()", child2emt, null, child3emt, child3txt);
	}
	
	@Test
	public void testXPathFollowingSibling() {
		checkXPath("following-sibling::*", child1emt, null, child2emt, child3emt);
	}
	
	@Test
	public void testXPathFollowingSiblingNode() {
		checkXPath("following-sibling::node()", child1emt, null, maintext2, child2emt, child3emt);
	}
	
	@Test
	public void testXPathNamespaces() {
		checkXPath("namespace::*", child3emt, null, child3nsa, Namespace.NO_NAMESPACE, child3nsb, Namespace.XML_NAMESPACE);
	}
	
	@Test
	public void testXPathNamespacesForText() {
		checkXPath("namespace::*", maintext1, null);
	}
	
	
	@Test
	public void testXPathParent() {
		checkXPath("parent::*", child3emt, null, main);
	}
	
	@Test
	public void testXPathParentNode() {
		checkXPath("parent::node()", child3emt, null, main);
	}
	
	@Test
	public void testXPathPreceding() {
		checkXPath("preceding::*", child2emt, null, child1emt);
	}
	
	@Test
	public void testXPathPrecedingNode() {
		checkXPath("preceding::node()", child2emt, null, doccomment, docpi,
				maincomment, mainpi, maintext1, child1emt, child1text, maintext2);
	}
	
	@Test
	public void testXPathPrecedingSibling() {
		checkXPath("preceding-sibling::*", child3emt, null, child1emt, child2emt);
	}
	
	@Test
	public void testXPathPrecedingSiblingNode() {
		checkXPath("preceding-sibling::node()", child3emt, null, maincomment, 
				mainpi, maintext1, child1emt, maintext2, child2emt);
	}
	
	@Test
	public void testXPathSelf() {
		checkXPath("self::*", child3emt, null, child3emt);
	}
	
	
	
	
	/* *******************************
	 * Negative TestCases
	 * ******************************* */
	
	@Test
	public void testNegativeBrokenPath() {
		try {
			XPathFactory.instance().compile("//badaxis::dummy");
			fail("Expected a JDOMException");
		} catch (IllegalArgumentException jde) {
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
	}

	@Test
	public void testNegativeBrokenExpression() {
		final String path = "//node()[string() = $novar]";
		XPathExpression<Object> xp = XPathFactory.instance().compile(path);
		assertEquals(xp.getExpression(), path);
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.evaluateFirst(doc);
			fail("Expected a JDOMException");
		} catch (IllegalStateException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.evaluate(doc);
			fail("Expected a JDOMException");
		} catch (IllegalStateException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.diagnose(doc, true);
			fail("Expected a JDOMException");
		} catch (IllegalStateException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
	}

}