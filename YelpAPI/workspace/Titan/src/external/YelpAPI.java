package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class YelpAPI {
	private static final String HOST = "https://api.yelp.com";
	private static final String ENDPOINT = "/v3/businesses/search";
	private static final String DEFAULT_TERM = "";
	private static final int SEARCH_LIMIT = 20;
	
	private static final String TOKEN_TYPE = "Bearer";
	private static final String API_KEY = "KPPUm4gpR455eyB3LPgFoxhkS7XoOJe_T5fwjPnaLQ70mBGZpVY08HxGlB_tzjUG_r5Rs82rt-W_RZX2-uXlwv-zWgMH20gg4yN7pT9H7wk92AadiUCuMXsR100FX3Yx";

	private void queryAPI(double lat, double lon) {
		List<Item> items = search(lat, lon, null);
		try {
			for (int i = 0; i < items.size(); i++) {
				JSONObject item = items.get(i).toJSONObject();
				System.out.println(item.toString(2));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public List<Item> search(double lat, double lon, String term) {
		if (term == null || term.length() == 0) {
			term = DEFAULT_TERM;
		}
		try {
			term = URLEncoder.encode(term, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s",  term, lat, lon, SEARCH_LIMIT);
		String url = HOST+ENDPOINT + "?" +query;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization",  TOKEN_TYPE + " "+ API_KEY);
			
			int responseCode = connection.getResponseCode();
			System.out.println("Sending request to URL:" + url);
			System.out.println("Response code:" + responseCode);
			
			if (responseCode != 200) {
				return new ArrayList();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine = "";
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("businesses")) {
				return getItemList(obj.getJSONArray("businesses"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}
	
	//***************************************** Helper Function **************************************************//
	private List<Item> getItemList(JSONArray restaurants) throws JSONException {
		List<Item> itemList = new ArrayList<> ();
		for (int i = 0; i < restaurants.length(); i++) {
			ItemBuilder builder = new ItemBuilder();
			JSONObject restaurant = restaurants.getJSONObject(i);
			if (!restaurant.isNull("distance")) {
				builder.setDistance(restaurant.getDouble("distance"));
			}
			
			if (!restaurant.isNull("image_url")) {
				builder.setImageUrl(restaurant.getString("image_url"));
			}
			
			if (!restaurant.isNull("url")) {
				builder.setUrl(restaurant.getString("url"));
			}
			
			if (!restaurant.isNull("id")) {
				builder.setItemId(restaurant.getString("id"));
			}
			
			if (!restaurant.isNull("name")) {
				builder.setName(restaurant.getString("name"));
			}
			
			if (!restaurant.isNull("rating")) {
				builder.setRating(restaurant.getDouble("rating"));
			}
			
			builder.setCategories(getCategory(restaurant));
			builder.setAddress(getAddress(restaurant));
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	private Set<String> getCategory(JSONObject restaurant) throws JSONException {
		Set<String> set = new HashSet<> ();
		if (!restaurant.isNull("categories")) {
			JSONArray array = restaurant.getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (!obj.isNull("alias")) {
					String cat = obj.getString("alias");
//					System.out.println(cat);
					set.add(cat);
				}
			}
		}
		return set;
	}
	private String getAddress(JSONObject restaurant) throws JSONException {
		StringBuilder sb = new StringBuilder();
		if (!restaurant.isNull("location")) {
			JSONObject obj = restaurant.getJSONObject("location");
			if (!obj.isNull("display_address")) {
				JSONArray completeAddress = obj.getJSONArray("display_address");
				for (int i = 0; i < completeAddress.length(); i++) {
					sb.append(completeAddress.getString(i));
					sb.append(", ");
				}
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static void main (String[] args) {
		YelpAPI test = new YelpAPI();
		test.queryAPI(37.38, -122.08);
	}
}
