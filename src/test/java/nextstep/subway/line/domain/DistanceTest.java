package nextstep.subway.line.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DistanceTest {

    @ParameterizedTest
    @DisplayName("거리는 0보다 커야만 합니다.")
    @ValueSource(ints = {-1, -99, 0})
    void valid(int distance) {
        assertThatIllegalArgumentException().isThrownBy(() -> Distance.of(distance));
    }

    @ParameterizedTest
    @DisplayName("거리가 작음을 판단 한다")
    @MethodSource("lessParam")
    void isLess(Distance source, Distance target, boolean result) {
        assertThat(source.isLess(target)).isEqualTo(result);
    }


    @Test
    @DisplayName("거리 빼기")
    void minus() {
        Distance distance = Distance.of(4);
        distance.minusDistance(1);

        assertThat(distance.value()).isEqualTo(3);
    }

    @Test
    @DisplayName("거리들의 합인 거리를 구한다")
    void sumDistance() {
        assertThat(Distance.sumDistance(Distance.of(4), Distance.of(7)).value()).isEqualTo(11);
    }

    private static Stream<Arguments> lessParam() {
        return Stream.of(
                Arguments.of(Distance.of(3), Distance.of(5), true),
                Arguments.of(Distance.of(3), Distance.of(3), false),
                Arguments.of(Distance.of(3), Distance.of(1), false)
        );
    }

}
