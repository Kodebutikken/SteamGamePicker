package com.kodebutikken.steamgamepicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SteamGamePickerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamGamePickerApplication.class, args);
    }

}
