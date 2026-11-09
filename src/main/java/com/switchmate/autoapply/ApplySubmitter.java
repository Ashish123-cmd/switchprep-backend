package com.switchmate.autoapply;

import com.switchmate.domain.entity.Application;

public interface ApplySubmitter {

	SubmissionResult submit(Application application);
}