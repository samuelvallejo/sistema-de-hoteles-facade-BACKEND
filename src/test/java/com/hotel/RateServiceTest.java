package com.hotel;

import com.hotel.factory.RoomFactory;
import com.hotel.model.*;
import com.hotel.service.RateService;
import com.hotel.strategy.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class RateServiceTest {
    @ParameterizedTest
    @CsvSource({"SINGLE,LOW,200.00", "SINGLE,HIGH,300.00", "DOUBLE,LOW,300.00",
            "DOUBLE,HIGH,450.00", "SUITE,LOW,500.00", "SUITE,HIGH,750.00"})
    void calculatesBothSeasonsForEveryRoomType(RoomType type, Season season, String expected) {
        RateService rates = new RateService(List.of(new HighSeasonPricingStrategy(), new LowSeasonPricingStrategy()));
        RateQuote quote = rates.quote(new RoomFactory().create(1, type), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 3), season);
        assertThat(quote.total()).isEqualByComparingTo(expected);
        assertThat(quote.baseTotal().add(quote.seasonSurcharge())).isEqualByComparingTo(quote.total());
    }
}
