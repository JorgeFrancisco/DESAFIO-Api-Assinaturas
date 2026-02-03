package com.globo.api_assinaturas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Plan {

	@Id
	@Column(length = 32)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "max_screens", nullable = false)
	private Integer maxScreens;
}