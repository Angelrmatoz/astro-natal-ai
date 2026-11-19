package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.astronatalai.backend.exception.InvalidReadingRequestException;
import org.junit.jupiter.api.Test;

class ReadingTypeTest {

    @Test
    void buildsDayRange() {
        ReadingType.Range range = ReadingType.DAY.range(LocalDate.of(2026, 11, 14));

        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 11, 14));
        assertThat(range.end()).isEqualTo(LocalDate.of(2026, 11, 14));
    }

    @Test
    void buildsMondayToSundayWeekRange() {
        ReadingType.Range range = ReadingType.WEEK.range(LocalDate.of(2026, 11, 14));

        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(range.end()).isEqualTo(LocalDate.of(2026, 11, 15));
    }

    @Test
    void buildsCalendarMonthRange() {
        ReadingType.Range range = ReadingType.MONTH.range(LocalDate.of(2026, 11, 14));

        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(range.end()).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    void buildsNatalRangeOnBirthDate() {
        ReadingType.Range range = ReadingType.NATAL.range(LocalDate.of(1990, 5, 1));

        assertThat(range.start()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(range.end()).isEqualTo(LocalDate.of(1990, 5, 1));
    }

    @Test
    void parsesCaseInsensitiveTypes() {
        assertThat(ReadingType.from("DAY")).isEqualTo(ReadingType.DAY);
        assertThat(ReadingType.from("week")).isEqualTo(ReadingType.WEEK);
        assertThat(ReadingType.from("Natal")).isEqualTo(ReadingType.NATAL);
    }

    @Test
    void rejectsUnknownType() {
        assertThatThrownBy(() -> ReadingType.from("year"))
                .isInstanceOf(InvalidReadingRequestException.class);
    }
}
