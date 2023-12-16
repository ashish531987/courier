package com.krupatek.courier.service;

import com.krupatek.courier.model.PlaceGeneration;
import com.krupatek.courier.repository.PlaceGenerationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

@Service
public class PlaceGenerationServiceImpl implements PlaceGenerationService {

    @Autowired
    PlaceGenerationRepository placeGenerationRepository;

    @Override
    public Optional<PlaceGeneration> findByCityName(String cityName) {
        return placeGenerationRepository.findByCityName(cityName);
    }

    @Override
    public Page<PlaceGeneration> findByCityNameStartsWithOrderByCityName(int offset, int limit, String cityName) {
        Pageable pageable = PageRequest.of(offset, limit);
        return placeGenerationRepository.findByCityNameStartsWithOrderByCityName(cityName, pageable);
    }

    @Override
        public List<PlaceGeneration> findAll() {
            return placeGenerationRepository.findAll();
    }


    @Override
    public PlaceGeneration saveAndFlush(PlaceGeneration placeGeneration){
        return placeGenerationRepository.saveAndFlush(placeGeneration);
    }

    @Override
    public SortedSet<String> findDistinctCityName() {
        return placeGenerationRepository.findDistinctCityNameOrderByCityName();
    }

    @Override
    public List<PlaceGeneration> findAllByPlaceCode(String placeCode) {
        return placeGenerationRepository.findAllByPlaceCodeOrderByCityName(placeCode);
    }

    @Transactional
    @Override
    public void updateCityWithZone(String cityName, String placeCode) {
        placeGenerationRepository.updateCityWithZone(cityName, placeCode);
    }

    @Override
    public long nextPlaceId(){
        return placeGenerationRepository.findMaxPlaceId()+1;
    }

    @Override
    public void delete(PlaceGeneration placeGeneration) {
        placeGenerationRepository.delete(placeGeneration);
    }
}
