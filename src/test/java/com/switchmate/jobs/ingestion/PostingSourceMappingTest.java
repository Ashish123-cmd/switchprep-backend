package com.switchmate.jobs.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PostingSourceMappingTest {

	@Test
	void mapsLeverPostingFromSavedResponse() throws IOException {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		JobSourceHttpClient httpClient = new JobSourceHttpClient(builder.build());
		LeverJobSource source = new LeverJobSource(
				httpClient, "sample", new KeywordSkillExtractor(List.of("Java", "Spring Boot")),
				new ExperienceRangeExtractor());
		server.expect(requestTo("https://api.lever.co/v0/postings/sample?mode=json"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(sample("lever-postings.json"), MediaType.APPLICATION_JSON));

		List<RawJob> jobs = source.fetchJobs();

		assertEquals(1, jobs.size());
		assertEquals("sample:lever-posting-123", jobs.get(0).externalId());
		assertEquals("Senior Java Engineer", jobs.get(0).title());
		assertEquals("sample", jobs.get(0).company());
		assertEquals("Remote", jobs.get(0).location());
		assertEquals(Set.of("Java", "Spring Boot"), Set.copyOf(jobs.get(0).skills()));
		assertEquals(3, jobs.get(0).minExperience());
		assertNull(jobs.get(0).maxExperience());
		server.verify();
	}

	@Test
	void mapsAshbyPostingAndExcludesUnlistedJob() throws IOException {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		JobSourceHttpClient httpClient = new JobSourceHttpClient(builder.build());
		AshbyJobSource source = new AshbyJobSource(
				httpClient, "sample", new KeywordSkillExtractor(List.of("Java", "PostgreSQL")),
				new ExperienceRangeExtractor());
		server.expect(requestTo("https://api.ashbyhq.com/posting-api/job-board/sample"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(sample("ashby-job-board.json"), MediaType.APPLICATION_JSON));

		List<RawJob> jobs = source.fetchJobs();

		assertEquals(1, jobs.size());
		assertEquals("sample:https://jobs.ashbyhq.com/sample/ashby-posting-456", jobs.get(0).externalId());
		assertEquals("Backend Engineer", jobs.get(0).title());
		assertEquals("New York, NY", jobs.get(0).location());
		assertEquals(Set.of("Java", "PostgreSQL"), Set.copyOf(jobs.get(0).skills()));
		assertEquals(2, jobs.get(0).minExperience());
		assertEquals(5, jobs.get(0).maxExperience());
		server.verify();
	}

	private String sample(String fileName) throws IOException {
		try (var input = getClass().getResourceAsStream("/samples/" + fileName)) {
			if (input == null) {
				throw new IllegalStateException("Missing saved sample response: " + fileName);
			}
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}