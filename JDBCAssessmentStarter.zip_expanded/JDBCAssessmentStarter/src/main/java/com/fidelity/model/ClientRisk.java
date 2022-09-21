package com.fidelity.model;

public enum ClientRisk {
	LOW("L"), MEDIUM("M"), HIGH("H"), POLITICAL("P");

	private String code;

	private ClientRisk(String code) {
		this.code = code;
	}

	public static ClientRisk of(String code) {
		for (ClientRisk ct: values()) {
			if (ct.code.equals(code)) {
				return ct;
			}
		}
		throw new IllegalArgumentException("Unknown code");
	}

	public String getCode() {
		return code;
	}
}
