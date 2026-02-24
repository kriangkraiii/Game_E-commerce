package com.ecom.model;

public class OrderRequest {

	private String firstName;
	private String lastName;
	private String email;
	private String paymentType;

	// Default constructor
	public OrderRequest() {
	}

	// All args constructor
	public OrderRequest(String firstName, String lastName, String email, String paymentType) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.paymentType = paymentType;
	}

	// Getters and Setters
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	@Override
	public String toString() {
		return "OrderRequest{" +
				"firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", email='" + email + '\'' +
				", paymentType='" + paymentType + '\'' +
				'}';
	}
}
