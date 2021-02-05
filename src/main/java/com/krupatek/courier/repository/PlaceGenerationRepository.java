package com.krupatek.courier.repository;

import com.krupatek.courier.model.PlaceGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

@Repository
public interface PlaceGenerationRepository extends JpaRepository<PlaceGeneration, Integer> {






    Optional<PlaceGeneration> findByCityName(String cityName);

    @Query(value = "SELECT distinct(city_name) FROM easynew.place_generation ORDER BY city_name;", nativeQuery = true)
    SortedSet<String> findDistinctCityNameOrderByCityName();

    List<PlaceGeneration> findAllByPlaceCodeOrderByCityName(String placeCode);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE place_generation SET place_code =:placeCode WHERE city_name = :cityName", nativeQuery = true)
    void updateCityWithZone(
            @Param("cityName")
                    String cityName,
            @Param("placeCode")
                    String placeCode);

}
