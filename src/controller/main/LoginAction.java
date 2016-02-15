package controller.main;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.CustomerDAO;
import model.EmployeeDAO;
import model.Model;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.JsonObject;

import util.Log;
import databean.CustomerBean;
import databean.EmployeeBean;
import formbean.LoginForm;

public class LoginAction extends Action {

	private static final String LOGIN_JSP = "login.jsp";
	private static final String NAME = "login";
	private static final String EMPLOYEE_VIEW = "employee_search_customer.do";
	private static final String CUSTOMER_VIEW = "customer_viewaccount.do";

	private FormBeanFactory<LoginForm> formBeanFactory = FormBeanFactory
	    .getInstance(LoginForm.class);

	private EmployeeDAO employeeDao;
	private CustomerDAO customerDAO;

	public LoginAction(Model model) {
		employeeDao = model.getEmployeeDAO();
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return NAME;
	}

	public JsonObject perform(HttpServletRequest request) {

		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		JsonObject innerObject = new JsonObject();

		try {

			LoginForm form = formBeanFactory.create(request);
			request.setAttribute("form", form);

			if (!form.isPresent()) {
				innerObject.addProperty("message", "Sorry, the login information is not valid!!!");
				return innerObject;
			}

			HttpSession session = request.getSession(true);
			if (session != null) {
				session.setAttribute("employee", null);
				session.setAttribute("customer", null);
			}

			errors.addAll(form.getValidationErrors());
			if (errors.size() != 0) {
				innerObject.addProperty("message", "Errors in login message, please check again!!!");
				return innerObject;
			}
			if (form.isEmployee()) {
				EmployeeBean employee = employeeDao.getEmployeeByUserName(form
				    .getUserName());
				if (employee == null) {
					errors.add("User name not found");
					innerObject.addProperty("message", "User name not found");
					return innerObject;
				}

				if (!employee.getPassword().equals(form.getPassword())) {
					errors.add("Incorrect password");
					innerObject.addProperty("message", "The username/password combination"
							+ "that you entered is not correct");
					return innerObject;
				}
				if (errors.size() != 0) {
					innerObject.addProperty("message", "Errors in login message, please check again!!!");
					return innerObject;
				}

				request.getSession().setAttribute("employee", employee);
				Log.i(
				    "In Login Action",
				    "employee is "
				        + request.getSession().getAttribute("employee")
				        + " employe name is "
				        + ((EmployeeBean) request.getSession().getAttribute("employee"))
				            .getUserName());
				innerObject.addProperty("message", "Welcome!!!" + 
				            ((EmployeeBean) request.getSession().getAttribute("employee"))
			            .getUserName());
				return innerObject;
			} else if (form.isCustomer()) {

				CustomerBean customer = customerDAO.getCustomerByUserName(form
				    .getUserName());

				if (customer == null) {
					errors.add("User name not found");
					innerObject.addProperty("message", "User name not found");
					return innerObject;
				}
				System.out.println("customer name" + customer.getUserName());
				System.out.println("customer:" + customer);
				System.out.println("customer password:" + customer.getPassword());
				System.out.println("form password" + form.getPassword());
				if (!customer.getPassword().equals(form.getPassword())) {
					errors.add("Incorrect password");
					innerObject.addProperty("message", "The username/password combination"
							+ "that you entered is not correct");
					return innerObject;
				}

				request.getSession().setAttribute("customer", customer);

				innerObject.addProperty("message", "Welcome!!!");
				return innerObject;
			}
			//return "";
		} catch (RollbackException e) {
			errors.add(e.getMessage());
			innerObject.addProperty("message", "Errors in login message, please check again!!!");
			return innerObject;
		} catch (FormBeanException e) {
			errors.add(e.getMessage());
			innerObject.addProperty("message", "Errors in login message, please check again!!!");
			return innerObject;
		} finally {
			if (Transaction.isActive()) {
				Transaction.rollback();
			}
		}
	}
}
