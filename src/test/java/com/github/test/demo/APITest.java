package com.github.test.demo;

import com.scmp.framework.annotations.LaunchBrowser;
import com.scmp.framework.test.TestLogger;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

public class APITest {

	TestLogger logger = new TestLogger();

	@Test(groups = {"test1", "groups_to_exclude", "api"})
	@LaunchBrowser(status = false)
	public void testAPI1() {

		Response resp = RestAssured.given().
				accept("application/json")
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
				.when()
				.get("https://api.github.com/users/rayzhouzhj");

		logger.logJson(resp.prettyPrint(), "json response");

		resp.then()
				.statusCode(200)
				.body("$", hasKey("company"))
				.body("name", isA(String.class));
	}

	@Test(groups = {"test", "api"})
	@LaunchBrowser(status = false)
	public void testAPI2() {
		Response resp = RestAssured.given().
				accept("application/json")
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
				.when()
				.get("https://api.github.com/users/rayzhouzhj");

		logger.logJson(resp.prettyPrint(), "json response");

		resp.then()
				.statusCode(200)
				.body("name", equalTo("Ray Zhou"));
	}

}
