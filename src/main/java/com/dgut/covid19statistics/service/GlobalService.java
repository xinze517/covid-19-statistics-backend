package com.dgut.covid19statistics.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GlobalService {

    List<LocalDate> getAllDate();

    LocalDate getNewestDate();

    Map<String, Map<String, Integer>> overview();

    List<LocalDate> loadShowDate(int spilt);
}
