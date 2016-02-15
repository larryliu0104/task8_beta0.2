package controller.customer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.CustomerDAO;
import model.FundDAO;
import model.Model;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
//import org.mybeans.form.FormBeanException;
//import org.mybeans.form.FormBeanFactory;

import com.google.gson.JsonObject;

import util.Util;
import controller.main.Action;
import databean.CustomerBean;
import databean.FundBean;
//import formbean.BuyFundForm;

public class CustomerBuyFundAction extends Action {

	// private final String CURRENT_OPERATION_JSP = "customerbuyfund.jsp";
	// private final String SUCCESS_JSP = "customer_success.jsp";
	public static final String NAME = "depositCheck";
	private FundDAO FundDAO;
	private CustomerDAO customerDAO;
	JsonObject innerObject;
	// private FormBeanFactory<BuyFundForm> formBeanFactory;
	private Model model;

	public CustomerBuyFundAction(Model model) {
		this.model = model;
		FundDAO = model.getFundDAO();
		customerDAO = model.getCustomerDAO();
		// formBeanFactory = FormBeanFactory.getInstance(BuyFundForm.class);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public JsonObject perform(HttpServletRequest request) {
		innerObject = new JsonObject();
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		try {
			String fundIdStr = request.getParameter("fundSymbol");
			String amount = request.getParameter("cashValue");

			int id = Integer.valueOf(fundIdStr);
			FundBean fund = FundDAO.read(id);
			if (fund == null) {
				request.setAttribute("message", "No funds");
				innerObject.addProperty("message", "I’m sorry you are not authorized to preform that action");
				return innerObject;
			}
			request.setAttribute("fund", fund);
			CustomerBean currentCustomer = (CustomerBean) request.getSession().getAttribute("customer");
			currentCustomer = customerDAO.read(currentCustomer.getId());
			request.setAttribute("customer", currentCustomer);
			if (currentCustomer == null) {
				innerObject.addProperty("message", "You must log in prior to making this request");
				return innerObject;
			}
			double[] arr = model.getAmount(currentCustomer.getId());
			double currentAmount = arr[0] / 100;
			double validAmount = arr[2] / 100;
			request.setAttribute("currentAmount", Util.formatNumber(currentAmount, "###,###,###,###,###,##0.00"));
			request.setAttribute("validAmount", Util.formatNumber(validAmount, "###,###,###,###,###,##0.00"));
			// BuyFundForm form = formBeanFactory.create(request);
			/*
			 * if (!form.isPresent()) { return CURRENT_OPERATION_JSP; }
			 * errors.addAll(form.getValidationErrors()); if (errors.size() !=
			 * 0) { return CURRENT_OPERATION_JSP; }
			 */
			if (Double.parseDouble(amount) > validAmount) {
				innerObject.addProperty("message",
						"I’m sorry, you must first deposit sufficient funds in your account in order to make this purchase");
				return innerObject;
			}

			model.commitBuyTransaction(fund.getId(), amount, currentCustomer.getId());

			innerObject.addProperty("message", "The purchase was successfully completed");

			request.setAttribute("message", "The purchase was successfully completed");
			return innerObject;
		} /*
			 * catch (FormBeanException e) { errors.add(e.toString()); return
			 * null; }
			 */ catch (RollbackException e) {
			errors.add(e.getMessage());
			innerObject.addProperty("message", "I’m sorry you are not authorized to preform that action");
			return innerObject;
		} finally {
			if (Transaction.isActive()) {
				Transaction.rollback();
			}
		}

	}
}
