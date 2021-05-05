package com.dgut.covid19statistics.controller;

import com.dgut.covid19statistics.service.ConfirmGlobalService;
import com.dgut.covid19statistics.service.GlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/global")
public class GlobalController {

    private final GlobalService globalService;
    private final ConfirmGlobalService confirmGlobalService;

    @GetMapping("/overview")
    public Map<String, Map<String, Integer>> overview() {
        return globalService.overview();
    }

    @GetMapping("/foreignSituation")
    public Map<String, Object> loadForeignSituation() {
        return confirmGlobalService.loadForeignSituation();
    }

    @GetMapping("/showDates/{split}")
    public List<LocalDate> loadShowDate(@PathVariable Integer split) {
        return globalService.loadShowDate(split);
    }

    @GetMapping("/dates")
    public List<LocalDate> getAllDate() {
        return globalService.getAllDate();
    }

    @GetMapping("/dataset")
    public Map<String, Object> countAllCountryByDates(int limit) {
        return confirmGlobalService.countAllCountryByDates(limit);
    }
}
