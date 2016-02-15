package controller.main;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

public class LogoutAction extends Action {

	private static final String NAME = "logout";
	private static final String PERFORM_PAGE = "login.jsp";

	public LogoutAction() {
	}

	public String getName() {
		return NAME;
	}

	public JsonObject perform(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		JsonObject innerObject = new JsonObject();
		if (session != null) {
			session.invalidate();
		}
		innerObject.addProperty("message", "You've been logged out");
		return innerObject;
	}
}