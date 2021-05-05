package com.dgut.covid19statistics.controller;

import com.dgut.covid19statistics.domain.Record;
import com.dgut.covid19statistics.service.ConfirmGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/counts/confirmed")
public class ConfirmedController {

    private final ConfirmGlobalService confirmGlobalService;

    @GetMapping("/inc/{date}/{limit}")
    public List<Record> increaseCountByAllCountry(@PathVariable LocalDate date, @PathVariable Integer limit) {
        return confirmGlobalService.increaseCountByAllCountry(date, limit);
    }

}
