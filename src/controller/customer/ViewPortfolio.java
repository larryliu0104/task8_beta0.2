package controller.customer;

import java.util.List;
import java.util.ArrayList;

public class ViewPortfolio {
	private String name;
	private String shares;
	private double price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShares() {
		return shares;
	}

	public void setShares(String shares) {
		this.shares = shares;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String toString() {
		return "name: " + name + " , shares: " + shares + " , price: " + price;
	}
}
