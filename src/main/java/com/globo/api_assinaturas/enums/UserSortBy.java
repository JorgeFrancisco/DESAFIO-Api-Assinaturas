package com.globo.api_assinaturas.enums;

public enum UserSortBy {

	ID("id"), NAME("name"), EMAIL("email"), CREATED_AT("createdAt");

	private final String property;

	UserSortBy(String property) {
		this.property = property;
	}

	public String property() {
		return property;
	}
}