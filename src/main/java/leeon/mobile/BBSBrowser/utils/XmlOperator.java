package leeon.mobile.BBSBrowser.utils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import leeon.mobile.BBSBrowser.actions.BBSBodyParseHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlOperator {
	
	
	/**
	 * 读取函数,
	 * @param file
	 */
	public static Document readDocument(byte[] body) {		
		InputStream is = new ByteArrayInputStream(body);
		return readDocument(is);
	}
	
	/**
	 * 读取函数,
	 * @param file
	 */
	public static Document readDocument(InputStream is) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			
			InputSource iss = new InputSource(is);
			iss.setEncoding(BBSBodyParseHelper.BBS_CHARSET);
			
			Document document = docBuilder.parse(iss);
			return document;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取一个节点下的tagName节点的text值
	 * @param element
	 * @param tagName
	 * @return
	 */
	public static String getTextValueByTagName(Element element, String tagName) {
		List<String> list = getTextValuesByTagName(element, tagName);
		if (list.size() != 0)
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * 获取一个节点下的 tagName节点的text值
	 * @param Element
	 * @return
	 */
	public static List<String> getTextValuesByTagName(Element element, String tagName) {
		NodeList nodeList = element.getElementsByTagName(tagName);
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(getTextValue(nodeList.item(i)));
		}
		return list;
	}

	
	/**
	 * 获取一个node节点下的text的方法
	 * 如果有text返回text,没有text,有cdata,返回第一个cdata
	 * @param node
	 * @return
	 */
	public static String getTextValue(Node node) {
		StringBuffer textValue = new StringBuffer();
		int length = node.getChildNodes().getLength();
		for (int i = 0; i < length; i ++) {
			Node c = node.getChildNodes().item(i);
			if (c.getNodeType() == Node.TEXT_NODE) {
				textValue.append(c.getNodeValue());
			}
		}
		return textValue.toString().trim();
	}
	
	/**
	 * 输出某个node下的xml字符串
	 */
	public static void innerXml(NodeList list, StringBuffer xml) {
		for (int i = 0; i < list.getLength(); i ++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
				xml.append("<![CDATA[").append(node.getNodeValue()).append("]]>");
			} else if (node.getNodeType() == Node.COMMENT_NODE) {
				xml.append("<!--").append(node.getNodeValue()).append("-->");
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				xml.append(replaceChar(node.getNodeValue()));
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				xml.append("<").append(node.getNodeName());
				NamedNodeMap map = ((Element)node).getAttributes();
				for (int j = 0; j < map.getLength(); j ++) {
					Node attr = map.item(j);
					xml.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
				}
				if (node.hasChildNodes()) {
					xml.append(">");
					innerXml(node.getChildNodes(), xml);
					xml.append("</").append(node.getNodeName()).append(">");
				} else
					xml.append("/>");
			}
		}
	}
	
	public static String replaceChar(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;");
	}
	
}
