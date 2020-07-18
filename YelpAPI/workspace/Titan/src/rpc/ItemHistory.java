package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MySQLConnection connection = new MySQLConnection();

		try {
//			JSONObject input = RpcHelper.readJsonObject(request);
//			String userId = input.getString("user_id");
//			JSONArray array = input.getJSONArray("favorite");
//			List<String> items = new ArrayList<> ();
//			for (int i = 0; i < array.length(); i++) {
//				items.add(array.getString(i));
//			}
			String userId = request.getParameter("user_id");
			Set<Item> favorite = connection.getFavoriteItems(userId);
			JSONArray array = new JSONArray();
			for (Item item : favorite) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", true);
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MySQLConnection conn = new MySQLConnection();
		try {
			JSONObject obj = RpcHelper.readJsonObject(request);
			String userId = obj.getString("user_id");
			JSONArray array = obj.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<> ();
			for (int i = 0; i < array.length(); i++) {
				itemIds.add(array.getString(i));
			}
			conn.setFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MySQLConnection connection = new MySQLConnection();
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");
			JSONArray array = input.getJSONArray("favorite");
			List<String> items = new ArrayList<> ();
			for (int i = 0; i < array.length(); i++) {
				items.add(array.getString(i));
			}
			connection.unsetFavoriteItems(userId, items);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result","SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

}
