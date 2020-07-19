package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.MySQLConnection;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MySQLConnection conn = new MySQLConnection();
		try {
			JSONObject obj = new JSONObject();
			HttpSession session = request.getSession(false);
			if (session == null) {
				obj.put("status", "Invalid session");
				response.setStatus(403);
			} else {
				String userId = (String) session.getAttribute("user_id");
				String name = conn.getFullname(userId);
				obj.put("status", "OK");
				obj.put("name", name);
				obj.put("user_id", userId);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
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
			String psw = obj.getString("password");
			
			JSONObject res = new JSONObject();
			if (conn.verifyLogin(userId, psw)) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id",  userId);
				session.setMaxInactiveInterval(10 * 60);
				String name = conn.getFullname(userId);
				res.put("status", "OK");
				res.put("user_id", userId);
				res.put("name", name);
			} else {
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, res);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

}
