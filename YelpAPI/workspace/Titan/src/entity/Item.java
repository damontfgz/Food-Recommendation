package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Item {
	public static class ItemBuilder {
		private String itemId;
		private String storeName;
		private String address;
		private String imgUrl;
		private double rating;
		private double distance;
		private Set<String> categories;
		private String url;
		
		public ItemBuilder setItemId(String itemId) {
			this.itemId = itemId;
			return this;
		}
		public ItemBuilder setName(String name) {
			this.storeName = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageUrl) {
			this.imgUrl = imageUrl;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public Item build() {
			return new Item(this); // call Item's private constructor
		}
	}
	
	private String itemId;
	private String storeName;
	private String address;
	private String imgUrl;
	private double rating;
	private double distance;
	private Set<String> categories;
	private String url;
	
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.storeName = builder.storeName;
		this.rating = builder.rating;
		this.address = builder.address;
		this.distance = builder.distance;
		this.categories = builder.categories;
		this.imgUrl = builder.imgUrl;
		this.url = builder.url;
	}
	
	public String getItemId() {
		return itemId;
	}
	public String getStoreName() {
		return storeName;
	}
	public String getAddress() {
		return address;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public double getRating() {
		return rating;
	}
	public double getDistance() {
		return distance;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getUrl() {
		return url;
	}
	
	//necessary because front-end only understand JSON instead of java class
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("item_id", itemId);
			obj.put("storeName", storeName);
			obj.put("address", address);
			obj.put("imgUrl", imgUrl);
			obj.put("rating", rating);
			obj.put("distance", distance);
			obj.put("categories", new JSONArray(categories));
			obj.put("url", url);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
		
	}
}
