package com.sdaniel.rssapp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import com.sdaniel.rssapp.models.repository.FeedRepository;

@Service("feedService")
public class FeedServiceImpl implements FeedService {
	
	@Autowired
	private FeedRepository feedRepository;
	
	@Autowired
	private ItemService itemService;
	
	@Override
	public Feed findById(String id) {
		return feedRepository.findById(id).orElse(null);
	}
	
	@Override
	public Feed findByUrl(String url) {
		return feedRepository.findFeedByUrl(url);
	}
	
	@Override
	public List<Feed> findAll() {
		return feedRepository.findAll();
	}
	
	@Override
	public Boolean existsByUrl(String url) {
		return feedRepository.existsFeedByUrl(url);
	}
	
	@Override
	public Boolean existsById(String id) {
		return feedRepository.existsFeedById(id);
	}
	
	@Override
	public Feed save(Feed feed) {
		return feedRepository.save(feed);
	}
	
	@Override
	public Feed saveByUrl(String url) throws Exception {
		return feedRepository.save(getFeedAsEntity(url));
	}
	
	@Override
	public void deleteById(String id) {
		feedRepository.deleteById(id);
	}
	
	@Override
	public void deleteFeedItems(String id) {
		itemService.deleteItemsByFeedId(id);
	}
	
	@Override
	public List<Item> refreshFeedItems(Feed feed) throws Exception {
		
		if(feed == null) {
			throw new Exception("No feed found.");
		}
		
		List<Item> items = new ArrayList<>();
		items = getItemsAsEntities(feed);
		
		List<Item> newItems = new ArrayList<>();
		
		if(!items.isEmpty()) {
			if(feed.getLastItemDate() != null) {
				newItems = items.stream()
						.filter(i -> i.getPublishDate().compareTo(feed.getLastItemDate()) > 0)
						.collect(Collectors.toList());
				
				if(!newItems.isEmpty()) {
					itemService.saveAll(newItems);
					refreshFeedLastItemDate(feed, newItems);
				}
			} else {
				// si el feed no tiene fecha de publicación, es feed nuevo
				// por lo que añadimos todos los items
				itemService.saveAll(items);
				refreshFeedLastItemDate(feed, items);
			}
		}
		
		return newItems;
		/* Antes de guardar hay que comparar la fecha más reciente
		   que tenemos en la BD (habría que mantenerla actualizada
		   en la coleción de Feeds así no la tenemos que buscar...)
		   con la fecha más reciente de la lista de items (si no la
		   tiene, no aceptar el Feed).
		   Si en la lista hay articulos más nuevos, los añadimos y
		   actualizamos la fecha más reciente en el Feed para la 
		   próxima comprobacion.
		*/
	}
	
	private List<Item> getItemsAsEntities(Feed feed) throws Exception {
		List<Item> items = new ArrayList<>();
		
		XmlReader reader = new XmlReader(new URL(feed.getUrl()));
		SyndFeed feedXml = new SyndFeedInput().build(reader);
		
		if(feedXml != null) {
			for(SyndEntry entry: feedXml.getEntries()) {
				
				String title = "", description = "", imgUrl = "", link = "";
				Date pubDate;
				
				// FIRST: XML must have publishDate, title and link
				link = entry.getLink();
				try { new URL(link).toURI(); } catch (Exception e) { continue; } // only valid link formats
				title = entry.getTitle();
				pubDate = entry.getPublishedDate();
				if(pubDate == null || link.isEmpty() || title.isEmpty()) {
					System.out.println("date: "+pubDate+" - link: "+link+" - title: "+title);
					continue; // skip item if does not have the minimum data
				}
								
				// SECOND: try to get the rest of the data from XML
				description = getDescription(entry);
				imgUrl = getImgUrl(entry);
				
				// THIRD: if some data is missing, try to get it from OG
				if(!link.isEmpty() && (title.isEmpty() || description.isEmpty() || imgUrl.isEmpty())) {
					try {
						Document doc = Jsoup.connect(entry.getLink()).userAgent("Mozilla/5.0").get();
					
						Elements metaTags = doc.getElementsByTag("meta");
					
						for (Element metaTag : metaTags) {
							String property = metaTag.attr("property");
							String content = metaTag.attr("content");
							
							if("og:title".equals(property) && title.isEmpty()) {
								title = content;
							}
							if("og:description".equals(property) && (description.isEmpty() || description.length() < 20)) {
								description = content;
							}
							if("og:image".equals(property) && imgUrl.isEmpty()) {
								imgUrl = content;
							}
						}
					} catch (Exception e) {
						System.out.println("Jsoup connection error to item link...");
						continue;
					}
				}
				
				// FINALLY ensure that we have the minimum data
				if(pubDate == null || link.isEmpty() || title.isEmpty()) {
					System.out.println("date: "+pubDate+" - link: "+link+" - title: "+title);
					continue; // skip item if does not have the minimum data
				}
				
				Item item = new Item();
				
				item.setLink(link);
				item.setTitle(title);
				item.setDescription(description);
				item.setImage(imgUrl);
				item.setPublishDate(pubDate);
				item.setFeedId(feed.getId());
				
				items.add(item);
			}
		}
		return items;
	}
	
	private String getDescription(SyndEntry entry) {
		String description = "";
		
		// look for 'description' tag
		String html = entry.getDescription().getValue();
		Document doc = Jsoup.parse(html);
		int size = doc.text().length() > 200 ? 200 : doc.text().length();
		description = doc.text().substring(0, size) + "...";
		
		// look for 'media:description' tag
		if(description.isEmpty() || description.length() < 20) {
			MediaEntryModule m = (MediaEntryModule) entry.getModule( MediaEntryModule.URI );
			if (m != null) {
				description = m.getMetadata().getDescription();
			}
		}
		return description;
	}
	
	private String getImgUrl(SyndEntry entry) throws Exception {
		String imgUrl = "";
		
		List<SyndEnclosure> sE = (List<SyndEnclosure>) entry.getEnclosures();
		
		if (sE.size() > 0) { // look for image enclosure
			if("image/jpeg".equals(sE.get(0).getType())) {
				imgUrl = sE.get(0).getUrl();
			}
        } else { // look for 'media:content type="image/jpeg" medium="image"' tag
			MediaEntryModule m = (MediaEntryModule) entry.getModule( MediaEntryModule.URI );
			if (m != null) {
				if(m.getMediaContents().length > 0) {
					if("image/jpeg".equals(m.getMediaContents()[0].getType()) ||
							"image".equals(m.getMediaContents()[0].getMedium())) {
						UrlReference ref = (UrlReference) m.getMediaContents()[0].getReference();
						imgUrl = ref.getUrl().toString();
					}
				}
			}
        }
		
		// look for first image inside 'description' tag
		if(imgUrl.isEmpty()) {
			String html = entry.getDescription().getValue();
			Document doc = Jsoup.parse(html);
			try {
				imgUrl = doc.select("img").first().attr("src");
			} catch(Exception e) {
				// el doc.select puede dar un NullPointerException...
			}
		}
		
		return imgUrl;
	}
	
	private void refreshFeedLastItemDate(Feed feed, List<Item> items) {
		Date maxDate = items.stream().map(i -> i.getPublishDate()).max(Date::compareTo).get();
		feed.setLastItemDate(maxDate);
		feedRepository.save(feed);
	}
	
	private Feed getFeedAsEntity(String url) throws Exception {
		Feed feed = new Feed();
		
		XmlReader reader = new XmlReader(new URL(url));
		SyndFeed feedXml = new SyndFeedInput().build(reader);
		
		if(feedXml != null) {
			feed.setName(feedXml.getTitle());
			feed.setUrl(url);
		} else {
			throw new Exception("Invalid XML source");
		}
		
		return feed;
	}

}
