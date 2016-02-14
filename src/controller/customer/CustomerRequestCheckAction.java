package controller.customer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.CustomerDAO;
import model.Model;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
//import org.mybeans.form.FormBeanException;
//import org.mybeans.form.FormBeanFactory;
import com.google.gson.JsonObject;

import util.Log;
import util.Util;
import controller.main.Action;
import databean.CustomerBean;
//import formbean.RequestCheckForm;

public class CustomerRequestCheckAction extends Action {

	private static final String FORMAT_STRING = "###,###,###,###,###,##0.00";
	//private static final String ACTION_NAME = "customer-request-check.do";
	//private static final String REQUES_CHECK_JSP = "customer-request-check.jsp";
	//private FormBeanFactory<RequestCheckForm> formBeanFactory;
	private CustomerDAO customerDAO;
	private Model model;
	public static final String NAME = "requestCheck";

	public CustomerRequestCheckAction(Model model) {
		customerDAO = model.getCustomerDAO();
		//formBeanFactory = FormBeanFactory.getInstance(RequestCheckForm.class);
		this.model = model;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public JsonObject perform(HttpServletRequest request) {
		String amount = request.getParameter("cashValue");
		Log.i("DC Action", "cashValue " + amount);
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		try {
			//RequestCheckForm form;
			//form = formBeanFactory.create(request);

			CustomerBean customer = (CustomerBean) request.getSession().getAttribute("customer");
			customer = customerDAO.read(customer.getId());
			request.setAttribute("customer", customer);
			double[] arr = model.getAmount(customer.getId());
			double currentAmount = arr[0] / 100;
			double validAmount = arr[2] / 100;
			request.setAttribute("currentAmount", Util.formatNumber(currentAmount, FORMAT_STRING));
			request.setAttribute("validAmount", Util.formatNumber(validAmount, FORMAT_STRING));

			/*
			 * if (!form.isPresent()) { return REQUES_CHECK_JSP; }
			 * 
			 * errors.addAll(form.getValidationErrors()); if (errors.size() !=
			 * 0) { return REQUES_CHECK_JSP; }
			 */
			model.commitRequestCheck(customer.getId(), ((long) (Double.parseDouble(amount) * 100)));
			JsonObject innerObject = new JsonObject();
			innerObject.addProperty("message", "The withdrawal was successfully completed");

			request.setAttribute("message", "The withdrawal was successfully completed");
			return innerObject;
		} /*catch (FormBeanException e) {
			e.printStackTrace();
			return null;
		}*/ catch (RollbackException e) {
			errors.add(e.getMessage());
			return null;
		} catch (NumberFormatException e) {
			errors.add(e.toString());
			return null;
		} finally {
			if (Transaction.isActive()) {
				Transaction.rollback();
			}
		}
	}
}
