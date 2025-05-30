package com.innov4africa.service_aggregation.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.innov4africa.service_aggregation.model.Transaction;

import reactor.core.publisher.Mono;

@Service
public class TransactionAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAggregationService.class);

    @Autowired
    private IPayService ipayService;

    @Autowired
    private IBankingService iBankingService;

    /**
     * Gets the last 3 transactions from all services
     */
    public Mono<List<Transaction>> getRecentTransactions(String telephone, String ipayToken, String email) {
        return Mono.zip(
                getIPayTransactions(telephone, ipayToken),
                getIBankingTransactions(email))
            .map(tuple -> {
                List<Transaction> allTransactions = new ArrayList<>();
                allTransactions.addAll(tuple.getT1());
                allTransactions.addAll(tuple.getT2());

                // Sort by date desc and take top 3
                return allTransactions.stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            });
    }

    /**
     * Gets all transactions with optional filtering
     */
    public Mono<List<Transaction>> getAllTransactions(
            String telephone, 
            String ipayToken, 
            String email,
            String filter) {
        
        return Mono.zip(
                getIPayTransactions(telephone, ipayToken),
                getIBankingTransactions(email))
            .map(tuple -> {
                List<Transaction> allTransactions = new ArrayList<>();
                allTransactions.addAll(tuple.getT1());
                allTransactions.addAll(tuple.getT2());

                // Apply filters
                if (filter != null && !filter.isBlank()) {
                    return allTransactions.stream()
                        .filter(t -> filterTransaction(t, filter))
                        .sorted(Comparator.comparing(Transaction::getDate).reversed())
                        .collect(Collectors.toList());
                }

                return allTransactions.stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .collect(Collectors.toList());
            });
    }

    /**
     * Gets details for a single transaction
     */
    public Mono<Transaction> getTransactionDetails(String transactionId, String source) {
        if ("ipay".equals(source)) {
            // To be implemented - get details from iPay
            return Mono.empty();
        } else if ("ibanking".equals(source)) {
            // To be implemented - get details from iBanking
            return Mono.empty();
        }
        return Mono.empty();
    }

    private Mono<List<Transaction>> getIPayTransactions(String telephone, String ipayToken) {
        return ipayService.getOperationCompte(ipayToken, telephone)
            .map(this::parseIPayTransactions)
            .onErrorResume(e -> {
                logger.error("Error getting iPay transactions", e);
                return Mono.just(new ArrayList<>());
            });
    }

    private Mono<List<Transaction>> getIBankingTransactions(String email) {
        // To be implemented when iBanking is ready
        return Mono.just(new ArrayList<>());
    }

    private List<Transaction> parseIPayTransactions(String xmlResponse) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlResponse)));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            String error = xpath.evaluate("//return/error", doc);

            if ("0".equals(error)) {
                NodeList operationNodes = (NodeList) xpath.evaluate("//return/operations", doc, XPathConstants.NODESET);
                
                for (int i = 0; i < operationNodes.getLength(); i++) {
                    try {
                        String date = xpath.evaluate("date", operationNodes.item(i));
                        String montant = xpath.evaluate("montant", operationNodes.item(i));
                        String typeOperation = xpath.evaluate("typeOperation", operationNodes.item(i));
                        String typeTransaction = xpath.evaluate("typeTransaction", operationNodes.item(i));
                        String idTransaction = xpath.evaluate("idTransaction", operationNodes.item(i));
                        String soldeCompte = xpath.evaluate("soldeCompte", operationNodes.item(i));

                        Transaction transaction = new Transaction(
                            date,
                            montant,
                            typeOperation,
                            typeTransaction,
                            idTransaction,
                            soldeCompte,
                            "ipay"
                        );
                        transactions.add(transaction);
                    } catch (Exception e) {
                        logger.warn("Erreur lors du parsing d'une transaction: {}", e.getMessage());
                    }
                }
            } else {
                String message = xpath.evaluate("//return/message", doc);
                logger.warn("Erreur iPay: {}", message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors du parsing XML des transactions: {}", e.getMessage());
        }
        return transactions;
    }
    
    private boolean filterTransaction(Transaction t, String filter) {
        return switch (filter.toUpperCase()) {
            case "ENCAISSEMENT", "CREDIT" -> "CREDIT".equals(t.getTypeOperation());
            case "DECAISSEMENT", "DEBIT" -> "DEBIT".equals(t.getTypeOperation());
            case "FACTURES" -> t.getTypeTransaction() != null && 
                (t.getTypeTransaction().contains("FACTURE") || 
                 t.getTypeTransaction().contains("SENELEC") || 
                 t.getTypeTransaction().contains("SDE"));
            default -> true;
        };
    }
}
