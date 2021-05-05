package com.dgut.covid19statistics.service;

import com.dgut.covid19statistics.domain.Record;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ConfirmGlobalService {

    List<Record> countByAllCountry(LocalDate date);

    List<Record> increaseCountByAllCountry(LocalDate date, int limit);

    Map<String, Object> countAllCountryByDates(int limit);

    Map<String, Object> loadForeignSituation();
}
