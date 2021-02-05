package com.krupatek.courier.service;

import com.krupatek.courier.model.AccountCopy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public interface AccountCopyService {

    List<AccountCopy> findAllByClientNameAndPodDateBetween(String clientName, Date startDate, Date endDate);

    List<AccountCopy> findAllByPodDateBetween(Date startDate, Date endDate);

    List<AccountCopy> findAllByClientNameAndPodDateBetweenAndType(String clientName, Date startDate, Date endDate, String type);

    AccountCopy saveAndFlush(AccountCopy accountCopy);

    List<AccountCopy> findAll();

    List<AccountCopy> findByDocNoStartsWith(String docNo);

    Page<AccountCopy> findByDocNoStartsWith(int offset, int limit,
                                            String docNo);

    Page<AccountCopy> findByDocNoStartsWithAndClientNameStartsWith(int offset, int limit, String docNo, String clientName);

    Page<AccountCopy> findByDocNoStartsWithAndClientNameStartsWithAndPodDate(int offset, int limit, String docNo, String clientName, Date podDate);

    long countByDocNoStartsWith(String docNo);

    long countByDocNoStartsWithAndClientNameStartsWith(String docNo, String clientName);

    long countByDocNoStartsWithAndClientNameStartsWithAndPodDate(String docNo, String clientName, Date podDate);

    long totalByDocNoStartsWithAndClientNameStartsWith(String docNo, String clientName);

    long totalByDocNoStartsWithAndClientNameStartsWithAndPodDate(String docNo, String clientName, Date podDate);

    List<AccountCopy> findAllByBillNo(String billNo);

    void tagBillNo(String currentSelectedItem, Date fromLocaleDate, Date fromLocaleDate1, String type, String billNo);

    void resetBillNo(String billNo);

    Optional<AccountCopy> findOneByDocNo(String docNo);

    void delete(AccountCopy accountCopy);

    void saveAll(List<AccountCopy> accountCopyList);
}
