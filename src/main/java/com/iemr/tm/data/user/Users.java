package com.iemr.tm.data.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "m_user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Users implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UserID")
	private Long userID;
	@Column(name = "UserName")
	private String userName;
	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean deleted;
}
