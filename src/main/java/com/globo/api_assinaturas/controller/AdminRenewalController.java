package com.globo.api_assinaturas.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.globo.api_assinaturas.dto.RenewalRunResponse;
import com.globo.api_assinaturas.scheduler.RenewalRunner;

@RestController
@RequestMapping("/admin/renewals")
@Profile({ "dev", "test" })
public class AdminRenewalController {

	private static final Logger log = LoggerFactory.getLogger(AdminRenewalController.class);

	private final RenewalRunner runner;

	public AdminRenewalController(RenewalRunner runner) {
		this.runner = runner;
	}

	@PostMapping("/run")
	public RenewalRunResponse runOnce() {
		log.info("AdminRenewalController: forcing renewal run");

		var res = runner.runOnce();

		log.info("AdminRenewalController: result processed={}, renewed={}, failed={}, suspended={}", res.processed(),
				res.renewed(), res.failed(), res.suspended());

		return res;
	}
}