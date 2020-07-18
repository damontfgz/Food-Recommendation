package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import db.MySQLConnection;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendationItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<> ();
		MySQLConnection conn = new MySQLConnection();
		
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
		Map<String, Integer> categoryCount = new HashMap<> ();
		for (String id : favoriteItemIds) {
			for (String category : conn.getCategories(id)) {
				categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
			}
		}
		List<Map.Entry<String, Integer>> list = new ArrayList<> (categoryCount.entrySet());
		Collections.sort(list, (a, b) -> b.getValue() - a.getValue());
		Set<String> visitedItemIds = new HashSet<> ();
		for (Map.Entry<String , Integer> entry : list) {
			List<Item> items = conn.searchItems(lat, lon, entry.getKey());
			for (Item i : items) {
				if (visitedItemIds.add(i.getItemId()) && !favoriteItemIds.contains(i)) {
					recommendedItems.add(i);
				}
			}
		}
		conn.close();
		return recommendedItems;
	}
}
