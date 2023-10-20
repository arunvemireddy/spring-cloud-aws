package com.aws.consumer.DTO;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Widget {

	private String id;
	private String owner;
	private String description;
	private List<OtherAttribute> otherAttributes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<OtherAttribute> getOtherAttributes() {
		return otherAttributes;
	}

	public void setOtherAttributes(List<OtherAttribute> otherAttributes) {
		this.otherAttributes = otherAttributes;
	}

	@Override
	public String toString() {
		return "Widget [id=" + id + ", owner=" + owner + ", description=" + description + ", otherAttributes="
				+ otherAttributes + "]";
	}

}
