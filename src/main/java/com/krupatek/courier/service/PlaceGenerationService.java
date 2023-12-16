package com.krupatek.courier.service;

import com.krupatek.courier.model.PlaceGeneration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

@Service
public interface PlaceGenerationService {
    Optional<PlaceGeneration> findByCityName(String cityName);

    public Page<PlaceGeneration> findByCityNameStartsWithOrderByCityName(int offset, int limit, String cityName);
    List<PlaceGeneration> findAll();
    PlaceGeneration saveAndFlush(PlaceGeneration placeGeneration);
    SortedSet<String> findDistinctCityName();
    List<PlaceGeneration> findAllByPlaceCode(String value);
    void updateCityWithZone(String cityName, String placeCode);
    long nextPlaceId();

    void delete(PlaceGeneration placeGeneration);
}
