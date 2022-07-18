package com.sdaniel.feed.services;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sdaniel.feed.entity.Entry;

@Service("feedService")
public class FeedServiceImpl implements FeedService {

	@Override
	public List<Entry> getEntries(String url) {
		
		List<Entry> entries = new ArrayList<Entry>();
		
		try {
			
			Boolean rss = true;
			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(url);
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			NodeList nodeList = (NodeList) xPath.compile("//item").evaluate(document, XPathConstants.NODESET);
			if(nodeList.getLength() < 1) {
				nodeList = (NodeList) xPath.compile("//entry").evaluate(document, XPathConstants.NODESET);
				rss = false;
			}
			
			for(int i = 0; i < nodeList.getLength(); i++) {
				Entry entry = new Entry();
				entry.setTitle(xPath.compile("./title").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
				
				if(rss) {
					entry.setDescription(xPath.compile("./description").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setPubDate(xPath.compile("./pubDate").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setCategory(xPath.compile("./category").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setLink(xPath.compile("./link").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setImgUrl(xPath.compile("./image").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
				} else {
					entry.setDescription(xPath.compile("./summary").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setPubDate(xPath.compile("./published").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setCategory("default");
					entry.setLink(xPath.compile("./link[@type='text/html']/@href").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
					entry.setImgUrl(xPath.compile("./link[@rel='enclosure']/@href").evaluate(nodeList.item(i), XPathConstants.STRING).toString());
				}
				
				entries.add(entry);
			}
		} catch (Exception e) {
			entries = null;
		}
		
		return entries;
	}
}
