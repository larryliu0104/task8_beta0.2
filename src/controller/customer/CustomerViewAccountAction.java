package controller.customer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.CustomerDAO;
import model.FundPriceDAO;
import model.Model;
import model.PositionDAO;
import model.TransactionDAO;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;

//import com.google.gson.Gson;
import com.google.gson.JsonObject;

import util.Log;
import util.Util;
import controller.main.Action;
import databean.CustomerBean;
import databean.FundPriceBean;
import databean.PositionBean;
import databean.ShareInformationBean;

public class CustomerViewAccountAction extends Action {
	private PositionDAO positionDAO;
	private FundPriceDAO fundPriceDAO;
	public static final String NAME = "viewPortfolio";
	private CustomerDAO customerDAO;
	private TransactionDAO transactionDAO;
	String message = "You don't have any funds at this time";
	ArrayList<ViewPortfolio> listPort = new ArrayList<ViewPortfolio>();
	JsonObject innerObject;
	private static final String FORMAT_STRING = "#,##0.00";
	private Model model;

	public CustomerViewAccountAction(Model model) {
		this.model = model;
		positionDAO = model.getPositionDAO();
		fundPriceDAO = model.getFundPriceDAO();
		customerDAO = model.getCustomerDAO();
		transactionDAO = model.getTransactionDAO();

	}

	public String getName() {
		return NAME;
	}

	@Override
	public JsonObject perform(HttpServletRequest request) {

		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);

		try {
			innerObject = new JsonObject();
			CustomerBean customer = (CustomerBean) request.getSession().getAttribute("customer");
			customer = customerDAO.read(customer.getId());
			if (customer == null) {
				innerObject.addProperty("message", "You must log in prior to making this request");
				return innerObject;
			}
			double currentAmount = Double.parseDouble(customer.getCashTwoDecimal());
			double pendingAmount = (double) transactionDAO.getPendingAmount(customer.getId()) / 100.0;
			double cash = currentAmount - pendingAmount;
			Log.i("Customer View Account Action", "valid Amount " + cash);
			Log.i("Customer View Account Action", "current Amount " + currentAmount);
			Log.i("Customer View Account Action", "pending Amount " + pendingAmount);
			request.setAttribute("currentAmount", Util.formatNumber(currentAmount, FORMAT_STRING));
			request.setAttribute("validAmount", Util.formatNumber(cash, FORMAT_STRING));

			PositionBean[] positionList = positionDAO.getPositionsByCustomerId(customer.getId());
			if (positionList == null) {
				JsonObject innerObject = new JsonObject();
				innerObject.addProperty("message", "You don't have any funds at this time");
				return innerObject;
			}
			ShareInformationBean[] shareList = model.getShares(customer.getId());
			// String lastTransactionDay = null;
			FundPriceBean fundPrice = fundPriceDAO.getCurrentFundPrice(customer.getId());
			double currentFundPrice = fundPrice.getPrice();

			// lastTransactionDay =
			// transactionDAO.getUsersLastTransactionDay(customer.getId());
			/*
			 * request.setAttribute("lastTransactionDay", lastTransactionDay);
			 * request.setAttribute("shareList", shareList);
			 */
			List<ShareInformationBean> lstShareList = new ArrayList<ShareInformationBean>();
			for (int i = 0; i < shareList.length; i++) {
				lstShareList.add(shareList[i]);
			}

			ViewPortfolio vpf = new ViewPortfolio();
			for (int i = 0; i < shareList.length; i++) {
				vpf.setName(shareList[i].getFundName());
				vpf.setShares(shareList[i].getShare());
				vpf.setPrice(currentFundPrice);

			}
			if (lstShareList.isEmpty()) {
				innerObject.addProperty("message", message);
			}
			listPort.add(vpf);
			String str = listPort.toString();
			innerObject.addProperty("cash", cash);

			innerObject.addProperty("Funds", str);

			return innerObject;
		} catch (RollbackException e) {

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
