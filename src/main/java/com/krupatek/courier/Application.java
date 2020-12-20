package com.krupatek.courier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    Logger log = LogManager.getLogger(Application.class);

    //
//    @Autowired
//    CompanyRepository companyRepository;
//
//    @Autowired
//    InvoiceService invoiceService;
//
//    @Autowired
//    ClientService clientService;
//
//    @Autowired
//    BillingService billingService;
//
//    @Autowired
//    AccountCopyService accountCopyService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @PostConstruct
//    void clientServiceTest(){
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(0, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(1, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(2, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(3, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(4, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//        clientService.findByClientNameStartsWith(5, 10, "").forEach(e -> log.info(e.getClientName()));
//        log.info("===================================================================================================");
//    }

//    @PostConstruct
//    void invoiceTest(){
//        String invoiceNo = "GST/19-20/788";
//
//        Optional<BillGeneration> billGeneration = billingService.findOne(invoiceNo);
//        List<AccountCopy> accountCopies = accountCopyService.findAllByBillNo(invoiceNo+" ");
//
//        if(billGeneration.isPresent()){
//            Client client = clientService.findAllByClientName(billGeneration.get().getClientName()).get(0);
//            Company company = companyRepository.findAll().get(0);
//            try {
//                invoiceService.generateInvoiceFor(company, client, billGeneration.get(), accountCopies, Locale.getDefault());
//            } catch (IOException e) {
//                log.info(e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }


//    @PostConstruct
//    void migration() {
//
//        long startTime = System.currentTimeMillis();
//        log.info("Migration of AccountCopy started !!!");
//        List<AccountCopy> accountCopyList = new ArrayList<>();
//
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(new FileInputStream(new File("C:\\Users\\ashis\\Downloads\\accountcopy.csv")), "UTF-16"))) {
//
//            String line = reader.readLine();
//            while (line != null) {
//                if (!line.trim().isEmpty()) {
//                    AccountCopy accountCopy = new AccountCopy();
//                    accountCopyList.add(accountCopy.adapt(line));
//                }
//                line = reader.readLine();
//            }
//
//            log.info("Saving total "+accountCopyList.size()+" records in db...");
//
//            accountCopyService.saveAll(accountCopyList);
//
//            long totalTime = (System.currentTimeMillis() - startTime)/1000;
//
//            log.info("Migration of AccountCopy completed in "+totalTime+" seconds");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
