package br.com.paulork.xmlutils;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

public class XMLUtilsTest {

    XMLUtils xml;

    @Before
    public void setUp() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<teste>");
        sb.append("  <cod>001</cod>");
        sb.append("  <msg>Hello world 1</msg>");
        sb.append("  <cod>002</cod>");
        sb.append("  <msg>Hello world 2</msg>");
        sb.append("  <cod>003</cod>");
        sb.append("  <msg>Hello world 3</msg>");
        sb.append("  <inner>");
        sb.append("    <str charset=\"utf-8\">String de teste 1</str>");
        sb.append("    <str charset=\"iso\">String de teste 2</str>");
        sb.append("  </inner>");
        sb.append("</teste>");
        xml = new XMLUtils(sb);
    }

    @Test
    public void testGetValue() {
        String tag = "cod";

        String expResult = "001";
        String result = xml.getValue(tag);
        assertEquals(expResult, result);

    }

    @Test
    public void testGetValues() throws Exception {
        String tag = "cod";

        String[] expResult = {"001", "002", "003"};
        String[] result = xml.getValues(tag);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testGetAttributes_String() throws Exception {
        String tag = "str";
        String[] expResult = {"charset"};
        String[] result = xml.getAttributes(tag);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testGetAttributes_String_int() throws Exception {
        String tag = "str";
        for (int i = 0; i < xml.getNumOccur(tag); i++) {
            String[] expResult = {"charset"};
            String[] result = xml.getAttributes(tag, i);
            assertArrayEquals(expResult, result);
        }
    }

    @Test
    public void testGetAttributeValues_String() throws Exception {
        String tag = "str";
        String[] expResult = {"utf-8"};
        String[] result = xml.getAttributeValues(tag);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testGetAttributeValues_String_int() throws Exception {
        String tag = "str";
        for (int i = 0; i < xml.getNumOccur(tag); i++) {
            if (i == 0) {
                String[] expResult = {"utf-8"};
                String[] result = xml.getAttributeValues(tag, i);
                assertArrayEquals(expResult, result);
            } else if (i == 1) {
                String[] expResult = {"iso"};
                String[] result = xml.getAttributeValues(tag, i);
                assertArrayEquals(expResult, result);
            }
        }
    }

    @Test
    public void testGetMapChilds() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<teste>");
        sb.append("  <cod>001</cod>");
        sb.append("  <msg>Hello world 1</msg>");
        sb.append("</teste>");
        XMLUtils xml = new XMLUtils(sb);

        String tag = "teste";
        HashMap<String, String> expResult = new HashMap<>();
        expResult.put("cod", "001");
        expResult.put("msg", "Hello world 1");
        HashMap<String, String> result = xml.getMapChilds(tag);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMapAttributes_String() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<teste>");
        sb.append("  <cod str1=\"001\" str2=\"002\">001</cod>");
        sb.append("  <msg>Hello world 1</msg>");
        sb.append("</teste>");
        XMLUtils xml = new XMLUtils(sb);

        String tag = "cod";
        HashMap<String, String> expResult = new HashMap<>();
        expResult.put("str1", "001");
        expResult.put("str2", "002");
        HashMap<String, String> result = xml.getMapAttributes(tag);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNumOccur() throws Exception {
        String tag = "cod";
        Integer expResult = 3;
        Integer result = xml.getNumOccur(tag);
        assertEquals(expResult, result);
    }

    @Test
    public void testStrToDoc() throws Exception {
        Document expResult = xml.getDocument();
        Document result = xml.strToDoc(xml.toString());
        assertSame(expResult, result);
    }

    @Test
    public void testDocToStr_Document() {
        String expResult = xml.toString();
        String result = xml.docToStr(xml.getDocument());
        assertEquals(expResult, result);
    }

    @Test
    public void testDocToStr_3args() {
        String expResult = xml.toString();
        String result = xml.docToStr(xml.getDocument(), true, true);
        assertEquals(expResult, result);
    }

}
