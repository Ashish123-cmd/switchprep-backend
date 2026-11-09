package com.switchmate.autoapply;

import org.springframework.stereotype.Component;

import com.switchmate.domain.entity.Application;

@Component
public class SimulatedApplySubmitter implements ApplySubmitter {

	@Override
	public SubmissionResult submit(Application application) {
		return SubmissionResult.succeeded();
	}
}