package nextstep.subway.favorite;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.favorite.ui.dto.FavoriteRequest;
import nextstep.subway.favorite.ui.dto.FavoriteResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_됨;
import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.지하철_노선에_지하철역_등록되어_있음;
import static nextstep.subway.member.MemberAcceptanceTest.회원_등록되어_있음;
import static nextstep.subway.station.StationAcceptanceTest.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;

public class FavoriteAcceptanceTest extends AcceptanceTest {
    // background
    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private String 사용자이메일;
    private String 암호;
    private Integer 나이;
    private String token;

    /**
     * 교대역    --- *2호선*(10m) ---   강남역
     * |                               |
     * *3호선(3m)*                      *신분당선*(10m)
     * |                               |
     * 남부터미널역  --- *3호선*(7m) ---   양재
     */
    @BeforeEach
    void setup() {
        super.setUp();

        사용자이메일 = "test@nextstep.com";
        암호 = "password";
        나이 = 32;

        강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = 지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = 지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음(
                new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10, BigDecimal.ZERO))
                .as(LineResponse.class);
        이호선 = 지하철_노선_등록되어_있음(
                new LineRequest("이호선", "bg-red-600", 교대역.getId(), 강남역.getId(), 10, BigDecimal.ZERO))
                .as(LineResponse.class);
        삼호선 = 지하철_노선_등록되어_있음(
                new LineRequest("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 10, BigDecimal.ZERO))
                .as(LineResponse.class);

        지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);

        회원_등록되어_있음(사용자이메일, 암호, 나이);
        token = 로그인_됨(사용자이메일, 암호);
    }

    @DisplayName("즐겨찾기를 관리한다.")
    @Test
    void manageFavoritesTest() {
        StationResponse source = 강남역;
        StationResponse target = 남부터미널역;

        // when
        ExtractableResponse<Response> createResponse = 즐겨찾기_추가_요청(token, source, target);
        // then
        Long createdId = 즐겨찾기_추가_요청_성공(createResponse);

        // when
        ExtractableResponse<Response> getResponse = 즐겨찾기_목록_조회_요청(token);
        // then
        즐겨찾기_목록_조회_성공(getResponse, source, target);

        // when
        ExtractableResponse<Response> deleteResponse = 즐겨찾기_삭제_요청(token, createdId);
        // then
        즐겨찾기_삭제_성공(deleteResponse);
    }
    
    @DisplayName("시나리오2: 사용자가 실수로 똑같은 즐겨찾기를 두번 등록한다.")
    @Test
    void addFavoriteTwiceTest() {
        // given
        즐겨찾기_추가되어_있음(token, 강남역, 남부터미널역);

        // when
        ExtractableResponse<Response> response = 즐겨찾기_추가_요청(token, 강남역, 남부터미널역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("시나리오3: 내 즐겨찾기가 아닌 즐겨찾기를 삭제할 수 없음")
    @Test
    void cannotDeleteFavoriteTest() {
        String newUserEmail = "newuser@nextstep.com";
        String newUserPassword = "password";
        Integer newUserAge = 5;
        // given
        Long registeredFavoriteId = 즐겨찾기_추가되어_있음(token, 강남역, 남부터미널역);
        회원_등록되어_있음(newUserEmail, newUserPassword, newUserAge);
        String token2 = 로그인_됨(newUserEmail, newUserPassword);

        // when
        ExtractableResponse<Response> response = 즐겨찾기_삭제_요청(token2, registeredFavoriteId);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    public static ExtractableResponse<Response> 즐겨찾기_삭제_요청(String token, Long id) {
        return RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().delete("/favorites/" + id)
                .then().log().all()
                .extract();
    }

    public static void 즐겨찾기_삭제_성공(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static ExtractableResponse<Response> 즐겨찾기_목록_조회_요청(String token) {
        return RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().get("/favorites")
                .then().log().all()
                .extract();
    }

    public static void 즐겨찾기_목록_조회_성공(
            ExtractableResponse<Response> response, StationResponse source, StationResponse target
    ) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<FavoriteResponse> favorites = response.jsonPath().getList(".", FavoriteResponse.class);
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getSource().getName()).isEqualTo(source.getName());
        assertThat(favorites.get(0).getTarget().getName()).isEqualTo(target.getName());
    }
    
    public static Long 즐겨찾기_추가되어_있음(String token, StationResponse source, StationResponse target) {
        ExtractableResponse<Response> response = 즐겨찾기_추가_요청(token, source, target);
        즐겨찾기_추가_요청_성공(response);
        return Long.parseLong(response.header("Location").split("/")[2]);
    }

    public static Long 즐겨찾기_추가_요청_성공(final ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        return Long.parseLong(response.header("Location").split("/")[2]);
    }

    public static ExtractableResponse<Response> 즐겨찾기_추가_요청(
            String token, StationResponse source, StationResponse target
    ) {
        return RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new FavoriteRequest(source.getId(), target.getId()))
                .when().post("/favorites")
                .then().log().all()
                .extract();
    }
}