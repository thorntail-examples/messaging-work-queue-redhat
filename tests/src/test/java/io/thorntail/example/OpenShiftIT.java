/*
 *
 *  Copyright 2018-2019 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.thorntail.example;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Arquillian.class)
public class OpenShiftIT {
    @RouteURL(value = "thorntail-messaging-work-queue-frontend")
    @AwaitRoute(path = "/api/data") // /health returns OK too soon, we've got a resource adapter and an app to deploy
    private String url;

    @Test
    public void shouldHandleRequest() {
        // issue a request
        String requestId =
                given()
                        .baseUri(url)
                .when()
                        .body("{\"text\":\"test-message\",\"uppercase\":true,\"reverse\":true}")
                        .contentType("application/json")
                        .post("/api/send-request")
                .then()
                        .statusCode(200)
                .extract()
                        .body().asString();

        // wait for the request to be handled
        await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .baseUri(url)
            .when()
                    .queryParam("request", requestId)
                    .get("/api/receive-response")
            .then()
                    .statusCode(200)
                    .body("requestId", equalTo(requestId))
                    .body("workerId", not(isEmptyString()))
                    .body("text", equalTo("EGASSEM-TSET"));
        });

        JsonPath responseJson =
                given()
                        .baseUri(url)
                .when()
                        .queryParam("request", requestId)
                        .get("/api/receive-response")
                .thenReturn()
                        .jsonPath();

        String workerId = responseJson.getString("workerId");
        String text = responseJson.getString("text");

        // verify data
        await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .baseUri(url)
            .when()
                    .get("/api/data")
            .then()
                    .statusCode(200)
                    .body("requestIds", hasItem(requestId))
                    .body("responses.%s.requestId", withArgs(requestId), equalTo(requestId))
                    .body("responses.%s.workerId", withArgs(requestId), equalTo(workerId))
                    .body("responses.%s.text", withArgs(requestId), equalTo(text))
                    .body("workers.%s.workerId", withArgs(workerId), equalTo(workerId))
                    .body("workers.%s.timestamp", withArgs(workerId), notNullValue())
                    .body("workers.%s.requestsProcessed", withArgs(workerId), notNullValue())
                    .body("workers.%s.processingErrors", withArgs(workerId), notNullValue());
        });
    }
}
