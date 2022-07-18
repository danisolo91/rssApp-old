package com.sdaniel.rssapp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sdaniel.rssapp.models.entity.Feed;
import com.sdaniel.rssapp.models.entity.Item;
import com.sdaniel.rssapp.models.repository.ItemRepository;

@Service("itemService")
public class ItemServiceImpl /*implements ItemService*/ {

	@Autowired
	private ItemRepository itemRepository;
	
	//@Override
	public List<Item> findAll() {
		return itemRepository.findAll();
	}

	//@Override
	public List<Item> findAllByFeedId(String feedId) {
		return itemRepository.findAllByFeedId(feedId);
	}
	
	//@Override
	public List<Item> saveAll(List<Item> items) {
		return itemRepository.saveAll(items);
	}
	
	//@Override
	public void deleteItemsByFeedId(String feedId) {
		itemRepository.deleteItemByFeedId(feedId);
	}

	//@Override
	public void refreshFeedItems(Feed feed) throws Exception {
		
		if(feed == null) {
			throw new Exception("No feed found.");
		}
		
		List<Item> items = new ArrayList<>();
		
		items = getItemsAsEntities(feed);
		items = itemRepository.saveAll(items);
	}
	
	private List<Item> getItemsAsEntities(Feed feed) throws Exception {
		
		List<Item> items = new ArrayList<>();
		
		XmlReader reader = new XmlReader(new URL(feed.getUrl()));
		SyndFeed feedXml = new SyndFeedInput().build(reader);
		
		if(feedXml != null) {
			for(SyndEntry entry: feedXml.getEntries()) {
				Item item = new Item();
				item.setLink(entry.getLink());
				item.setTitle(entry.getTitle());
				item.setDescription(getDescription(entry));
				try {
					item.setPublishDate(entry.getPublishedDate());
				} catch (Exception e) {
					System.out.println("PublishedDate error: " + e.getMessage());
					item.setPublishDate(new Date());
				}
				item.setImage(getImgUrl(entry));
				item.setFeedId(feed.getId());
				items.add(item);
			}
		}
		return items;
	}

	private String getDescription(SyndEntry entry) {
		String description = "";
		
		try {
			String html = entry.getDescription().getValue();
			
			Document doc = Jsoup.parse(html);
			
			int size = doc.text().length() > 200 ? 200 : doc.text().length();
			
			description = doc.text().substring(0, size) + "...";
		} catch (Exception e) {
			System.out.println("Description error: " + e.getMessage());
		}
		
		if(description.isEmpty()) {
			try {
				MediaEntryModule m = (MediaEntryModule) entry.getModule( MediaEntryModule.URI );
				if (null != m) {
					System.out.println(m);
					description = m.getMetadata().getDescription();
				}
			} catch (Exception e) {
				System.out.println("Error media contents description: " + e.getMessage());
			}
		}
		return description;
	}
	
	private String getImgUrl(SyndEntry entry) {
		
		String imgUrl = "";
		
		List<SyndEnclosure> sE = (List<SyndEnclosure>) entry.getEnclosures();
		
		if (sE.size() != 0) {
			imgUrl = sE.get(0).getUrl();
        } else {
			try {
				MediaEntryModule m = (MediaEntryModule) entry.getModule( MediaEntryModule.URI );
				if (null != m) {
					//System.out.println(m);
					if("image/jpeg".equals(m.getMediaContents()[0].getType()) ||
							"image".equals(m.getMediaContents()[0].getMedium())) {
						UrlReference ref = (UrlReference) m.getMediaContents()[0].getReference();
						imgUrl = ref.getUrl().toString();
					}
				}
			} catch (Exception e) {
				System.out.println("Error media contents: " + e.getMessage());
			}
        }
		
		if(imgUrl.isEmpty()) {
			try {
				String html = entry.getDescription().getValue();
				Document doc = Jsoup.parse(html);
				imgUrl = doc.select("img").first().attr("src");
			} catch (Exception e) {
				System.out.println("Error selector Jsoup: " + e.getMessage());
			}
		}
		
		/*if(imgUrl.isEmpty()) {
			List<SyndContent> syndContents = entry.getContents();
			for(SyndContent syndContent : syndContents) {
				System.out.println(syndContent.getMode());
                System.out.println("This is content " + syndContent.getValue());
            }
		}*/
		
		return imgUrl;
	}
}
