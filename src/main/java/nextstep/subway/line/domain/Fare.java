package nextstep.subway.line.domain;

import java.util.Objects;

public class Fare {
    private static final int MIN_FARE = 0;
    private static final int DEFAULT_FARE = 1_250;
    private static final int ADDITIONAL_FARE = 100;
    private static final Distance DEFAULT_MAX_DISTANCE = Distance.of(10);
    private static final Distance SHORT_MAX_DISTANCE = Distance.of(50);
    private static final Distance SHORT_ADDITIONAL_DISTANCE = Distance.of(5);
    private static final Distance LONG_ADDITIONAL_DISTANCE = Distance.of(8);

    private final int fare;

    private Fare(int fare) {
        validFare(fare);
        this.fare = fare;
    }

    public static Fare of(int fare) {
        return new Fare(fare);
    }

    private void validFare(int fare) {
        if (fare < MIN_FARE) {
          throw new IllegalArgumentException("요금은 0원 이상이어야만 합니다.");
        }
    }

    public static Fare calculateFare(Distance totDistance) {
        return Fare.of(calculate(totDistance));
    }

    private static int calculate(Distance totDistance) {
        if (totDistance.isEqualAndLess(DEFAULT_MAX_DISTANCE)) {
            return DEFAULT_FARE;
        }
        if (totDistance.isEqualAndLess(SHORT_MAX_DISTANCE)) {
            return shortCalculate(totDistance);
        }
        return longCalculate(totDistance);
    }

    private static int longCalculate(Distance totDistance) {
        return shortCalculate(SHORT_MAX_DISTANCE) + ADDITIONAL_FARE
                * totDistance.subDistance(SHORT_MAX_DISTANCE).rateDistance(LONG_ADDITIONAL_DISTANCE);
    }

    private static int shortCalculate(Distance totDistance) {
        return DEFAULT_FARE + ADDITIONAL_FARE * totDistance.subDistance(DEFAULT_MAX_DISTANCE).rateDistance(SHORT_ADDITIONAL_DISTANCE);
    }

    public int value() {
        return fare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Fare fare1 = (Fare) o;
        return fare == fare1.fare;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fare);
    }
}
