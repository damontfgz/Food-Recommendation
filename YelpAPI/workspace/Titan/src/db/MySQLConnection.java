package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import entity.Item;
import entity.Item.ItemBuilder;
import external.YelpAPI;


public class MySQLConnection {
	private Connection conn;
	
	public MySQLConnection() {
		//establist the connection to database
		try {
			System.out.println("Connection to " + MySQLDBUtil.URL);
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		//close connection 
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			System.out.println(userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();

				System.out.println(itemId);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			}
	}
	
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection fail");
			return;
		}
		try {
			String sql = "DELETE FROM history WHERE user_id=? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);

			for (String id : itemIds) {
				ps.setString(2, id);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection fail");
			return new HashSet<> ();
		}
		Set<String> favorite = new HashSet<> ();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				favorite.add(rs.getString("item_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return favorite;
	}
	
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection fail");
			return new HashSet<> ();
		}
		Set<Item> items = new HashSet<> ();
		Set<String> favoriteIds = getFavoriteItemIds(userId);
		try {
			String sql = "SELECT * FROM items WHERE item_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			for (String itemId : favoriteIds) {
				ps.setString(1, itemId);
				ResultSet rs = ps.executeQuery();
				
				ItemBuilder builder = new ItemBuilder();
				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setUrl(rs.getString("url"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setRating(rs.getDouble("rating"));
					builder.setDistance(rs.getDouble("distance"));
					builder.setCategories(getCategories(itemId));
					items.add(builder.build());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}
	
	/*verify if user login properly*/
	
	public boolean verifyLogin(String userId, String psw) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "SELECT * FROM users WHERE user_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				if (psw.equals(rs.getNString("password"))) {
					System.out.println("login successfully");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("user do not exist or password do not match");
		return false;
	}
	
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		StringBuilder fullName = new StringBuilder();
		try {
			String sql = "SELECT * FROM users WHERE user_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setNString(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				fullName.append(rs.getNString("first_name"));
				fullName.append(" " + rs.getNString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fullName.toString();
	}
	
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> categories = new HashSet<> ();
		try {
			String sql = "SELECT category FROM categories WHERE item_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			
			
			while (rs.next()) {
				categories.add(rs.getNString("category"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return categories;
	}
	
	public List<Item> searchItems(double lat, double lon, String term) {
		YelpAPI api = new YelpAPI();
		List<Item> items = api.search(lat, lon, term);
		for (Item i : items) {
			saveItem(i);
		}
		return items;
	}
	
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection fail");
			return;
		}
		// "SELECT * FROM users WHERE name = '" + userName + "' and pw = '"+ passWord +"';"
		// userName = "1' OR '1'='1";
		// passWord = "1' OR '1'='1";
		// "SELECT * FROM users WHERE name = '1' OR '1'='1' and pw = '1' OR '1'='1';"
		try {
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getStoreName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getUrl());
			ps.setString(6, item.getImgUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			
			for (String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
