package member.auth.command;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import member.auth.service.LoginFailException;
import member.auth.service.LoginService;
import member.auth.service.User;
import mvc.command.CommandHandler;

public class LoginHandler implements CommandHandler {
	
	private static final String FORM_VIEW = "./view/loginForm.jsp";
	private LoginService loginService = new LoginService();

	@Override
	public String process(HttpServletRequest req, HttpServletResponse res) throws Exception {
		//req를 통한 요청이 GET방식이면 processForm을 호출 POST방식이면 processSubmit 호출.
		if (req.getMethod().equalsIgnoreCase("GET")) {
			return processForm(req, res);
		} else if (req.getMethod().equalsIgnoreCase("POST")) {
			return processSubmit(req, res);
		} else {
			res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return null;
		}
	} //process 종료.

	private String processForm(HttpServletRequest req, HttpServletResponse res) {
		//res객체를 통해 경로를 전달해서 loginForm을 띄운다.
		return FORM_VIEW;
	}

	private String processSubmit(HttpServletRequest req, HttpServletResponse res) throws Exception {
		String id = trim(req.getParameter("id"));
		String password = trim(req.getParameter("password"));

		Map<String, Boolean> errors = new HashMap<>();
		req.setAttribute("errors", errors);

		if (id == null || id.isEmpty()) {
			errors.put("id", Boolean.TRUE);
		} 

		if (password == null || password.isEmpty()) {
			errors.put("password", Boolean.TRUE);	
		}

		if (!errors.isEmpty()) {
			return  FORM_VIEW;
		}

		try {
			User user = loginService.login(id, password);
			req.getSession().setAttribute("authUser", user);
			res.sendRedirect(req.getContextPath() + "/index.jsp");
			return null;
		} catch (LoginFailException e) {
			errors.put("idOrPwNotMatch" , Boolean.TRUE);
			return FORM_VIEW;
		}
	}

	private String trim(String str) {
		return str == null ? null : str.trim();
	}
}