package com.innov4africa.service_aggregation.controller;

import com.innov4africa.service_aggregation.model.HistoryItem;
import com.innov4africa.service_aggregation.model.HistoryResponse;
import com.innov4africa.service_aggregation.model.IShopPaymentRequest;
import com.innov4africa.service_aggregation.model.IShopPaymentResponse;
import com.innov4africa.service_aggregation.model.LogoutResponse;
import com.innov4africa.service_aggregation.model.Notification;
import com.innov4africa.service_aggregation.model.NotificationResponse;
import com.innov4africa.service_aggregation.model.SDEPaymentRequest;
import com.innov4africa.service_aggregation.model.SDEPaymentResponse;
import com.innov4africa.service_aggregation.model.SenelecPaymentRequest;
import com.innov4africa.service_aggregation.model.SenelecPaymentResponse;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.model.SmsPayRequest;
import com.innov4africa.service_aggregation.model.SmsPayResponse;
import com.innov4africa.service_aggregation.model.SoldeResponse;
import com.innov4africa.service_aggregation.model.Transaction;
import com.innov4africa.service_aggregation.model.TransactionResponse;
import com.innov4africa.service_aggregation.model.TransferRequest;
import com.innov4africa.service_aggregation.model.TransferResponse;
import com.innov4africa.service_aggregation.model.UO;
import com.innov4africa.service_aggregation.model.UOResponse;
import com.innov4africa.service_aggregation.model.WoyofalPaymentRequest;
import com.innov4africa.service_aggregation.model.WoyofalPaymentResponse;
import com.innov4africa.service_aggregation.service.IPayService;
import com.innov4africa.service_aggregation.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ipay")
public class IPayController {

    private static final Logger logger = LoggerFactory.getLogger(IPayController.class);
    
    @Autowired
    private IPayService ipayService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint de diagnostic pour afficher les informations extraites du JWT
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Les informations du token pour débogage
     */
    @GetMapping("/token-info")
    public Mono<ResponseEntity<Map<String, String>>> getTokenInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, String> authInfo = extractAuthInfo(authHeader);
        if (authInfo == null) {
            Map<String, String> errorInfo = new HashMap<>();
            errorInfo.put("error", "Token invalide ou non fourni");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorInfo));
        }
        
        logger.info("Informations extraites du JWT: {}", authInfo);
        return Mono.just(ResponseEntity.ok(authInfo));
    }

    @GetMapping("/solde")
    public Mono<ResponseEntity<SoldeResponse>> getSolde(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token d'authentification manquant", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Format de token invalide", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token invalide ou expiré", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token incomplet", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande de solde pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay
        return ipayService.getSolde(telephone, ipayToken)
            .flatMap(this::handleSoapResponse)
            .onErrorResume(this::handleError);
    }

    @GetMapping("/uo")
    public Mono<ResponseEntity<UOResponse>> getUOByCellular(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return buildUnauthorizedResponse(
                new UOResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande d'UO pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay
        return ipayService.getUOByCellular(ipayToken, telephone)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("0".equals(error)) {
                        // Si uo existe dans la réponse
                        String uoError = xpath.evaluate("//return/uo/error", doc);
                        if (!uoError.isEmpty()) {
                            UO uo = new UO(
                                uoError,
                                xpath.evaluate("//return/uo/id", doc),
                                xpath.evaluate("//return/uo/nom", doc),
                                xpath.evaluate("//return/uo/numTel", doc),
                                xpath.evaluate("//return/uo/prenom", doc),
                                xpath.evaluate("//return/uo/type", doc)
                            );
                            return Mono.just(ResponseEntity.ok(
                                new UOResponse("success", message, uo, 
                                    List.of(new ServiceStatus("i-pay", true, "UO trouvé")))
                            ));
                        }
                        return Mono.just(ResponseEntity.ok(
                            new UOResponse("success", message, null, 
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body(
                            new UOResponse("error", message, null, 
                                List.of(new ServiceStatus("i-pay", false, message)))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse SOAP", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new UOResponse("error", "Erreur technique", null, 
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new UOResponse("error", "Service indisponible", null, 
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
                ));
            });
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<HistoryResponse>> getHistorySolde(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Vérifications du token (comme pour les autres endpoints)
        if (authHeader == null || authHeader.isBlank()) {
            return buildUnauthorizedResponse(
                new HistoryResponse("error", "Token manquant", null, 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
        }

        if (!authHeader.startsWith("Bearer ")) {
            return buildUnauthorizedResponse(
                new HistoryResponse("error", "Format token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
        }

        String jwt = authHeader.substring(7);
        if (!jwtUtil.validateToken(jwt)) {
            return buildUnauthorizedResponse(
                new HistoryResponse("error", "Token invalide/expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
        }

        String ipayToken = jwtUtil.extractIpayToken(jwt);
        String accountIdIPay = jwtUtil.extractAccountIdIPay(jwt);
        
        if (ipayToken == null || accountIdIPay == null) {
            logger.warn("Token ne contient pas les informations requises - ipayToken: {}, accountIdIPay: {}", ipayToken, accountIdIPay);
            return buildUnauthorizedResponse(
                new HistoryResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
        }
        
        logger.info("Demande d'historique du solde pour le compte: {}", accountIdIPay);

        return ipayService.getHistorySolde(ipayToken, accountIdIPay)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);

                    if ("0".equals(error)) {
                        List<HistoryItem> historyItems = new ArrayList<>();
                        
                        // Récupérer tous les nœuds 'histories' dans la réponse
                        NodeList historiesNodes = (NodeList) xpath.evaluate(
                            "//return/histories", doc, XPathConstants.NODESET);
                        
                        logger.info("Nombre d'entrées d'historique: {}", historiesNodes.getLength());
                        
                        // Parcourir chaque nœud 'histories' et extraire les données
                        for (int i = 0; i < historiesNodes.getLength(); i++) {
                            org.w3c.dom.Node node = historiesNodes.item(i);
                            
                            String date = xpath.evaluate("date", node);
                            String id = xpath.evaluate("id", node);
                            String solde = xpath.evaluate("solde", node);
                            
                            historyItems.add(new HistoryItem(date, id, solde));
                        }
                        
                        // Si aucun historique n'a été trouvé
                        if (historyItems.isEmpty()) {
                            return Mono.just(ResponseEntity.ok(
                                new HistoryResponse("success", "Aucune opération", historyItems,
                                    List.of(new ServiceStatus("i-pay", true, "Historique vide")))
                            ));
                        }
                        
                        return Mono.just(ResponseEntity.ok(
                            new HistoryResponse("success", "Historique récupéré", historyItems,
                                List.of(new ServiceStatus("i-pay", true, "Historique récupéré")))
                        ));
                    } else {
                        String message = xpath.evaluate("//return/message", doc);
                        logger.warn("Erreur lors de la récupération de l'historique: {}", message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new HistoryResponse("error", message, null,
                                List.of(new ServiceStatus("i-pay", false, message)))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour l'historique", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new HistoryResponse("error", "Erreur technique: " + e.getMessage(), null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour l'historique", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new HistoryResponse("error", "Service indisponible", null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
                ));
            });
    }

    /**
     * Endpoint pour déconnecter un utilisateur (logout)
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse indiquant le succès ou l'échec de la déconnexion
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<LogoutResponse>> deconnexionUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de déconnexion sans header Authorization");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token d'authentification manquant",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide lors de la déconnexion: {}", authHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Format de token invalide",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré lors de la déconnexion");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token invalide ou expiré",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 4. Extraction du token IPay
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (ipayToken == null) {
            logger.warn("Token ne contient pas le token IPay");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token incomplet",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        logger.info("Demande de déconnexion avec le token IPay: {}", ipayToken);
        
        // 5. Appel du service IPay pour la déconnexion
        return ipayService.deconnexionUser(ipayToken)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String code = xpath.evaluate("//return/code", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("1".equals(code)) {
                        logger.info("Déconnexion réussie");
                        return Mono.just(ResponseEntity.ok(
                            new LogoutResponse("success", "Déconnexion réussie", 
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    } else {
                        logger.warn("Échec de déconnexion: {} - {}", code, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new LogoutResponse("error", message,
                                List.of(new ServiceStatus("i-pay", false, "Échec de déconnexion")))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement de la réponse de déconnexion", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new LogoutResponse("error", "Erreur technique",
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service de déconnexion", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new LogoutResponse("error", "Service indisponible",
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
                ));
            });
    }

    @GetMapping("/operations")
    public Mono<ResponseEntity<TransactionResponse>> getOperations(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès aux transactions sans header Authorization");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new TransactionResponse("error", "Token d'authentification manquant", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour les transactions: {}", authHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new TransactionResponse("error", "Format de token invalide", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour les transactions");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new TransactionResponse("error", "Token invalide ou expiré", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new TransactionResponse("error", "Token incomplet", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        logger.info("Demande des transactions pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay avec téléphone au lieu de userId
        return ipayService.getOperationCompte(ipayToken, telephone)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("0".equals(error)) {
                        List<Transaction> transactions = new ArrayList<>();
                        
                        try {
                            NodeList operationNodes = (NodeList) xpath.evaluate("//return/operations", doc, XPathConstants.NODESET);
                            for (int i = 0; i < operationNodes.getLength(); i++) {
                                String date = xpath.evaluate("date", operationNodes.item(i));
                                String montant = xpath.evaluate("montant", operationNodes.item(i));
                                String typeOperation = xpath.evaluate("typeOperation", operationNodes.item(i));
                                String typeTransaction = xpath.evaluate("typeTransaction", operationNodes.item(i));
                                String idTransaction = xpath.evaluate("idTransaction", operationNodes.item(i));
                                String soldeCompte = xpath.evaluate("soldeCompte", operationNodes.item(i));
                                
                                transactions.add(new Transaction(
                                    date, 
                                    montant, 
                                    typeOperation, 
                                    typeTransaction, 
                                    idTransaction, 
                                    soldeCompte, 
                                    "ipay"
                                ));
                            }
                        } catch (Exception e) {
                            logger.warn("Erreur lors du parsing des transactions: {}", e.getMessage());
                        }
                        
                        return Mono.just(ResponseEntity.ok(
                            new TransactionResponse(
                                "success", 
                                message, 
                                transactions.size(), 
                                transactions, 
                                List.of(new ServiceStatus("i-pay", true, "Transactions récupérées"))
                            )
                        ));
                    } else {
                        logger.warn("Erreur IPay lors de la récupération des transactions: {}", message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new TransactionResponse(
                                "error",
                                message,
                                0,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour les transactions", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new TransactionResponse(
                            "error",
                            "Erreur technique",
                            0,
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour les transactions", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new TransactionResponse(
                        "error",
                        "Service indisponible",
                        0,
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour récupérer les notifications d'un utilisateur
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse contenant la liste des notifications
     */

    @GetMapping("/notifications")
    public Mono<ResponseEntity<NotificationResponse>> getNotifications(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès aux notifications sans header Authorization");
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour les notifications: {}", authHeader);
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour les notifications");
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String userId = jwtUtil.extractUserId(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        String telephone = jwtUtil.extractTelephone(jwt);
        
        if (ipayToken == null) {
            logger.warn("Token ne contient pas le token IPay: {}", ipayToken);
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande des notifications pour l'utilisateur ID: {}", userId);
        
        // 5. Appel du service IPay - getAllNotif récupérera l'userId du repository si nécessaire
        return ipayService.getAllNotif(ipayToken, userId)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    
                    if ("0".equals(error)) {
                        // Utiliser XPathConstants.NODESET pour récupérer tous les nœuds notifications
                        NodeList notificationNodes = (NodeList) xpath.evaluate(
                            "//return/notifications", doc, XPathConstants.NODESET);
                        
                        List<Notification> notificationList = new ArrayList<>();
                        
                        // Parcourir chaque nœud de notification
                        for (int i = 0; i < notificationNodes.getLength(); i++) {
                            org.w3c.dom.Node node = notificationNodes.item(i);
                            
                            String id = xpath.evaluate("id", node);
                            String date = xpath.evaluate("date", node);
                            String message = xpath.evaluate("libelle", node); // libelle dans XML -> message dans notre modèle
                            String status = xpath.evaluate("lu", node);
                            
                            notificationList.add(new Notification(id, date, message, "notification", status));
                        }
                        
                        logger.info("Récupération de {} notifications", notificationList.size());
                        
                        return Mono.just(ResponseEntity.ok(
                            new NotificationResponse(
                                "success",
                                "",
                                notificationList,
                                List.of(new ServiceStatus("i-pay", true, "Notifications récupérées"))
                            )
                        ));
                    } else {
                        String message = xpath.evaluate("//return/message", doc);
                        logger.warn("Erreur lors de la récupération des notifications: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new NotificationResponse(
                                "error",
                                message,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour les notifications", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new NotificationResponse(
                            "error",
                            "Erreur technique",
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour les notifications", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new NotificationResponse(
                        "error",
                        "Service indisponible",
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    @PostMapping("/transfer")
    public Mono<ResponseEntity<TransferResponse>> transferFunds(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TransferRequest request) {
        
        Map<String, String> authInfo = extractAuthInfo(authHeader);
        if (authInfo == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new TransferResponse("error", "Authentification requise", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }
        
        String ipayToken = authInfo.get("ipayToken");
        String accountIdIPay = authInfo.get("accountIdIPay");
       
    
        
        if (accountIdIPay == null) {
            logger.warn("ID de compte non disponible pour le transfert");
            return Mono.just(ResponseEntity.badRequest().body(
                new TransferResponse("error", "Information de compte manquante", null,
                    List.of(new ServiceStatus("i-pay", false, "Informations incomplètes")))
            ));
        }
        
        logger.info("Transfert de {} vers le compte {} (compte source: {})",
                request.getMontant(), request.getIdAccountBeneficiary(), accountIdIPay);
        
        return ipayService.w2wVirementAccount(
                ipayToken, 
                request.getMontant(),
                request.getCommission() != null ? request.getCommission() : "0",
                accountIdIPay,  // ID du compte émetteur récupéré du JWT 
                request.getIdAccountBeneficiary(), 
                request.getObjet() != null ? request.getObjet() : "",
                request.getCommissionRetrait() != null ? request.getCommissionRetrait() : "0"
            )
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String reference = xpath.evaluate("//return/reference", doc);
                    
                    if ("0".equals(error)) {
                        return Mono.just(ResponseEntity.ok(
                            new TransferResponse("success", message, reference,
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            new TransferResponse("error", message, null,
                                List.of(new ServiceStatus("i-pay", false, message)))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement de la réponse de transfert", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new TransferResponse("error", "Erreur technique: " + e.getMessage(), null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors du transfert", e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new TransferResponse("error", "Erreur technique: " + e.getMessage(), null,
                        List.of(new ServiceStatus("i-pay", false, "Service indisponible")))
                ));
            });
    }

    @PostMapping("/sms-pay")
    public Mono<ResponseEntity<SmsPayResponse>> sendSmsPayCode(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SmsPayRequest request) {
        
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'envoi de code de paiement sans header Authorization");
            return buildUnauthorizedResponse(
                new SmsPayResponse("error", "Token d'authentification manquant",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour l'envoi de code de paiement: {}", authHeader);
            return buildUnauthorizedResponse(
                new SmsPayResponse("error", "Format de token invalide",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour l'envoi de code de paiement");
            return buildUnauthorizedResponse(
                new SmsPayResponse("error", "Token invalide ou expiré",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims nécessaires
        String telephone = jwtUtil.extractTelephone(jwt);
        String nom = jwtUtil.extractNom(jwt);
        String prenom = jwtUtil.extractPrenom(jwt);
        
        if (telephone == null) {
            logger.warn("Token ne contient pas le numéro de téléphone de l'utilisateur");
            return buildUnauthorizedResponse(
                new SmsPayResponse("error", "Token incomplet",
                    List.of(new ServiceStatus("i-pay", false, "Informations utilisateur manquantes")))
            );
        }
        
        // Si nom ou prénom manquant, utiliser des valeurs par défaut
        nom = (nom != null) ? nom : "Utilisateur";
        prenom = (prenom != null) ? prenom : "";

        // 5. Validation des données de la requête
        String beneficiaireTel = request.getBeneficiaireTel();
        String montant = request.getMontant();
        
        if (beneficiaireTel == null || beneficiaireTel.isBlank()) {
            logger.warn("Numéro de téléphone du bénéficiaire manquant");
            return Mono.just(ResponseEntity.badRequest().body(
                new SmsPayResponse("error", "Numéro de téléphone du bénéficiaire manquant",
                    List.of(new ServiceStatus("i-pay", false, "Données invalides")))
            ));
        }
        
        if (montant == null || montant.isBlank()) {
            montant = "0"; // Valeur par défaut si non fournie
        } else {
            try {
                // Vérification que le montant est un nombre positif
                double amount = Double.parseDouble(montant);
                if (amount < 0) {
                    return Mono.just(ResponseEntity.badRequest().body(
                        new SmsPayResponse("error", "Montant invalide",
                            List.of(new ServiceStatus("i-pay", false, "Montant doit être positif ou nul")))
                    ));
                }
            } catch (NumberFormatException e) {
                return Mono.just(ResponseEntity.badRequest().body(
                    new SmsPayResponse("error", "Format de montant invalide",
                        List.of(new ServiceStatus("i-pay", false, "Montant doit être un nombre")))
                ));
            }
        }

        // 6. Initialisation des valeurs par défaut pour les champs optionnels
        String numeros = request.getNumeros() != null ? request.getNumeros() : "?";
        
        logger.info("Demande d'envoi de code de paiement par SMS de {} à {}, montant: {}", 
            telephone, beneficiaireTel, montant);
        
        // 7. Appel du service IPay
        return ipayService.smsPayCodeGFact(telephone, beneficiaireTel, montant, numeros, nom, prenom)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("0".equals(error)) {
                        logger.info("Envoi de code de paiement réussi");
                        return Mono.just(ResponseEntity.ok(
                            new SmsPayResponse(
                                "success", 
                                message, 
                                List.of(new ServiceStatus("i-pay", true, "Code envoyé avec succès"))
                            )
                        ));
                    } else {
                        logger.warn("Échec de l'envoi de code de paiement: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new SmsPayResponse(
                                "error",
                                message,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour l'envoi de code de paiement", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new SmsPayResponse(
                            "error",
                            "Erreur technique",
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour l'envoi de code de paiement", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new SmsPayResponse(
                        "error",
                        "Service indisponible",
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
 * Endpoint pour effectuer un paiement de facture SDE
 * @param authHeader Le header d'autorisation contenant le JWT
 * @param request La requête contenant les détails du paiement SDE
 * @return Une réponse indiquant le succès ou l'échec du paiement
 */
    @PostMapping("/sde-payment")
    public Mono<ResponseEntity<SDEPaymentResponse>> paySDE(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SDEPaymentRequest request) {
        
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de paiement SDE sans header Authorization");
            return buildUnauthorizedResponse(
                new SDEPaymentResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour le paiement SDE: {}", authHeader);
            return buildUnauthorizedResponse(
                new SDEPaymentResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour le paiement SDE");
            return buildUnauthorizedResponse(
                new SDEPaymentResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims nécessaires
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new SDEPaymentResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 5. Validation des données de la requête
        if (request.getNumeroPolice() == null || request.getNumeroPolice().isBlank() ||
            request.getNumeroFacture() == null || request.getNumeroFacture().isBlank() ||
            request.getReferenceClient() == null || request.getReferenceClient().isBlank() ||
            request.getMontant() == null || request.getMontant().isBlank()) {
            
            logger.warn("Données de paiement SDE incomplètes");
            return Mono.just(ResponseEntity.badRequest().body(
                new SDEPaymentResponse("error", "Données de paiement incomplètes", null,
                    List.of(new ServiceStatus("i-pay", false, "Données invalides")))
            ));
        }
        
        try {
            // Vérification que le montant est un nombre positif
            double amount = Double.parseDouble(request.getMontant());
            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().body(
                    new SDEPaymentResponse("error", "Montant invalide", null,
                        List.of(new ServiceStatus("i-pay", false, "Montant doit être positif")))
                ));
            }
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.badRequest().body(
                new SDEPaymentResponse("error", "Format de montant invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Montant doit être un nombre")))
            ));
        }

        // 6. Initialisation des valeurs par défaut pour les champs optionnels
        String commission = request.getCommission() != null ? request.getCommission() : "0";
        String commagent = request.getCommagent() != null ? request.getCommagent() : "0";
        String cellular = request.getCellular() != null ? request.getCellular() : telephone;
        
        logger.info("Demande de paiement SDE - Police: {}, Facture: {}, Référence: {}, Montant: {}", 
            request.getNumeroPolice(), request.getNumeroFacture(), request.getReferenceClient(), request.getMontant());
        
        // 7. Appel du service IPay
        return ipayService.paiementSDE(
                ipayToken, 
                request.getNumeroPolice(), 
                request.getNumeroFacture(), 
                request.getReferenceClient(),
                request.getMontant(), 
                commission, 
                cellular, 
                commagent)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String reference = xpath.evaluate("//return/reference", doc);

                    if ("0".equals(error)) {
                        logger.info("Paiement SDE réussi, référence: {}", reference);
                        return Mono.just(ResponseEntity.ok(
                            new SDEPaymentResponse(
                                "success", 
                                message, 
                                reference, 
                                List.of(new ServiceStatus("i-pay", true, "Paiement effectué"))
                            )
                        ));
                    } else {
                        logger.warn("Échec du paiement SDE: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new SDEPaymentResponse(
                                "error",
                                message,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour le paiement SDE", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new SDEPaymentResponse(
                            "error",
                            "Erreur technique",
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour le paiement SDE", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new SDEPaymentResponse(
                        "error",
                        "Service indisponible",
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour effectuer un paiement de facture Senelec
     * @param authHeader Le header d'autorisation contenant le JWT
     * @param request La requête contenant les détails du paiement Senelec
     * @return Une réponse indiquant le succès ou l'échec du paiement
     */
    @PostMapping("/senelec-payment")
    public Mono<ResponseEntity<SenelecPaymentResponse>> paySenelec(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SenelecPaymentRequest request) {
        
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de paiement Senelec sans header Authorization");
            return buildUnauthorizedResponse(
                new SenelecPaymentResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour le paiement Senelec: {}", authHeader);
            return buildUnauthorizedResponse(
                new SenelecPaymentResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour le paiement Senelec");
            return buildUnauthorizedResponse(
                new SenelecPaymentResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims nécessaires
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new SenelecPaymentResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 5. Validation des données de la requête
        if (request.getNumeroPolice() == null || request.getNumeroPolice().isBlank() ||
            request.getNumeroFacture() == null || request.getNumeroFacture().isBlank() ||
            request.getMontant() == null || request.getMontant().isBlank()) {
            
            logger.warn("Données de paiement Senelec incomplètes");
            return Mono.just(ResponseEntity.badRequest().body(
                new SenelecPaymentResponse("error", "Données de paiement incomplètes", null,
                    List.of(new ServiceStatus("i-pay", false, "Données invalides")))
            ));
        }
        
        try {
            // Vérification que le montant est un nombre positif
            double amount = Double.parseDouble(request.getMontant());
            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().body(
                    new SenelecPaymentResponse("error", "Montant invalide", null,
                        List.of(new ServiceStatus("i-pay", false, "Montant doit être positif")))
                ));
            }
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.badRequest().body(
                new SenelecPaymentResponse("error", "Format de montant invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Montant doit être un nombre")))
            ));
        }

        // 6. Initialisation des valeurs par défaut pour les champs optionnels
        String commission = request.getCommission() != null ? request.getCommission() : "0";
        String commagent = request.getCommagent() != null ? request.getCommagent() : "0";
        String cellular = request.getCellular() != null ? request.getCellular() : telephone;
        
        logger.info("Demande de paiement Senelec - Police: {}, Facture: {}, Montant: {}", 
            request.getNumeroPolice(), request.getNumeroFacture(), request.getMontant());
        
        // 7. Appel du service IPay
        return ipayService.paiementSenelec(
                ipayToken, 
                request.getNumeroPolice(), 
                request.getNumeroFacture(),
                request.getMontant(), 
                commission, 
                cellular, 
                commagent)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String reference = xpath.evaluate("//return/reference", doc);

                    if ("0".equals(error)) {
                        logger.info("Paiement Senelec réussi, référence: {}", reference);
                        return Mono.just(ResponseEntity.ok(
                            new SenelecPaymentResponse(
                                "success", 
                                message, 
                                reference,
                                List.of(new ServiceStatus("i-pay", true, "Paiement effectué"))
                            )
                        ));
                    } else {
                        logger.warn("Échec du paiement Senelec: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new SenelecPaymentResponse(
                                "error",
                                message,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour le paiement Senelec", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new SenelecPaymentResponse(
                            "error",
                            "Erreur technique",
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour le paiement Senelec", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new SenelecPaymentResponse(
                        "error",
                        "Service indisponible",
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour effectuer un paiement Woyofal
     * @param authHeader Le header d'autorisation contenant le JWT
     * @param request La requête contenant les détails du paiement Woyofal
     * @return Une réponse indiquant le succès ou l'échec du paiement
     */
    @PostMapping("/woyofal-payment")
    public Mono<ResponseEntity<WoyofalPaymentResponse>> payWoyofal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody WoyofalPaymentRequest request) {
        
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de paiement Woyofal sans header Authorization");
            return buildUnauthorizedResponse(
                new WoyofalPaymentResponse("error", "Token d'authentification manquant", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour le paiement Woyofal: {}", authHeader);
            return buildUnauthorizedResponse(
                new WoyofalPaymentResponse("error", "Format de token invalide", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour le paiement Woyofal");
            return buildUnauthorizedResponse(
                new WoyofalPaymentResponse("error", "Token invalide ou expiré", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims nécessaires
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new WoyofalPaymentResponse("error", "Token incomplet", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 5. Validation des données de la requête
        if (request.getNumeroPolice() == null || request.getNumeroPolice().isBlank() ||
            request.getNumeroTelephone() == null || request.getNumeroTelephone().isBlank() ||
            request.getMontant() == null || request.getMontant().isBlank()) {
            
            logger.warn("Données de paiement Woyofal incomplètes");
            return Mono.just(ResponseEntity.badRequest().body(
                new WoyofalPaymentResponse("error", "Données de paiement incomplètes", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Données invalides")))
            ));
        }
        
        try {
            // Vérification que le montant est un nombre positif
            double amount = Double.parseDouble(request.getMontant());
            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().body(
                    new WoyofalPaymentResponse("error", "Montant invalide", null, null, null,
                        List.of(new ServiceStatus("i-pay", false, "Montant doit être positif")))
                ));
            }
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.badRequest().body(
                new WoyofalPaymentResponse("error", "Format de montant invalide", null, null, null,
                    List.of(new ServiceStatus("i-pay", false, "Montant doit être un nombre")))
            ));
        }

        // 6. Initialisation des valeurs par défaut pour les champs optionnels
        String frais = request.getFrais() != null ? request.getFrais() : "0";
        String commission = request.getCommission() != null ? request.getCommission() : "0";
        String cellular = request.getCellular() != null ? request.getCellular() : telephone;
        
        logger.info("Demande de paiement Woyofal - Police: {}, Téléphone: {}, Montant: {}", 
            request.getNumeroPolice(), request.getNumeroTelephone(), request.getMontant());
        
        // 7. Appel du service IPay
        return ipayService.paiementWoyofal(
                ipayToken, 
                request.getNumeroPolice(), 
                request.getNumeroTelephone(), 
                request.getMontant(),
                frais,
                commission, 
                cellular)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String reference = xpath.evaluate("//return/reference", doc);
                    String code = xpath.evaluate("//return/code", doc);
                    String montantEnergy = xpath.evaluate("//return/montantEnergy", doc);

                    if ("0".equals(error)) {
                        logger.info("Paiement Woyofal réussi, code: {}, référence: {}", code, reference);
                        return Mono.just(ResponseEntity.ok(
                            new WoyofalPaymentResponse(
                                "success", 
                                message, 
                                reference,
                                code,
                                montantEnergy,
                                List.of(new ServiceStatus("i-pay", true, "Rechargement effectué"))
                            )
                        ));
                    } else {
                        logger.warn("Échec du paiement Woyofal: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new WoyofalPaymentResponse(
                                "error",
                                message,
                                null,
                                null,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour le paiement Woyofal", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new WoyofalPaymentResponse(
                            "error",
                            "Erreur technique",
                            null,
                            null,
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour le paiement Woyofal", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new WoyofalPaymentResponse(
                        "error",
                        "Service indisponible",
                        null,
                        null,
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour effectuer un paiement iShop
     * @param authHeader Le header d'autorisation contenant le JWT (optionnel)
     * @param request La requête contenant les détails du paiement iShop
     * @return Une réponse indiquant le succès ou l'échec du paiement
     */
    @PostMapping("/ishop-payment")
    public Mono<ResponseEntity<IShopPaymentResponse>> payIShop(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody IShopPaymentRequest request) {
        
        // Note: Pour iShop, l'authentification est optionnelle
        // car il s'agit d'un paiement par code qui peut être utilisé par n'importe qui
        
        // 1. Validation des données de la requête
        if (request.getNumeros() == null || request.getNumeros().isBlank() ||
            request.getMontant() == null || request.getMontant().isBlank() ||
            request.getOrder() == null || request.getOrder().isBlank() ||
            request.getCode() == null || request.getCode().isBlank()) {
            
            logger.warn("Données de paiement iShop incomplètes");
            return Mono.just(ResponseEntity.badRequest().body(
                new IShopPaymentResponse("error", "Données de paiement incomplètes", null,
                    List.of(new ServiceStatus("i-pay", false, "Données invalides")))
            ));
        }
        
        try {
            // Vérification que le montant est un nombre positif
            double amount = Double.parseDouble(request.getMontant());
            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().body(
                    new IShopPaymentResponse("error", "Montant invalide", null,
                        List.of(new ServiceStatus("i-pay", false, "Montant doit être positif")))
                ));
            }
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.badRequest().body(
                new IShopPaymentResponse("error", "Format de montant invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Montant doit être un nombre")))
            ));
        }
        
        logger.info("Demande de paiement iShop - Commande: {}, Montant: {}", 
            request.getOrder(), request.getMontant());
        
        // 2. Appel du service IPay
        return ipayService.paymentIshop(
                request.getNumeros(), 
                request.getMontant(), 
                request.getOrder(), 
                request.getCode())
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String reference = xpath.evaluate("//return/reference", doc);

                    if ("0".equals(error)) {
                        logger.info("Paiement iShop réussi, référence: {}", reference);
                        return Mono.just(ResponseEntity.ok(
                            new IShopPaymentResponse(
                                "success", 
                                message, 
                                reference,
                                List.of(new ServiceStatus("i-pay", true, "Paiement effectué"))
                            )
                        ));
                    } else {
                        logger.warn("Échec du paiement iShop: {} - {}", error, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new IShopPaymentResponse(
                                "error",
                                message,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour le paiement iShop", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new IShopPaymentResponse(
                            "error",
                            "Erreur technique",
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour le paiement iShop", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new IShopPaymentResponse(
                        "error",
                        "Service indisponible",
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Extrait les informations d'authentification du JWT
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Un objet contenant les informations d'authentification ou null en cas d'erreur
     */
    private Map<String, String> extractAuthInfo(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Header d'autorisation manquant");
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return null;
        }

        String jwt = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return null;
        }

        Map<String, String> authInfo = new HashMap<>();
        authInfo.put("ipayToken", jwtUtil.extractIpayToken(jwt));
        authInfo.put("telephone", jwtUtil.extractTelephone(jwt));
        authInfo.put("userId", jwtUtil.extractUserId(jwt));
        authInfo.put("accountIdIPay", jwtUtil.extractAccountIdIPay(jwt));
        
        return authInfo;
    }

    private Mono<ResponseEntity<SoldeResponse>> handleSoapResponse(String xmlResponse) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlResponse)));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            String error = xpath.evaluate("//return/error", doc);
            String message = xpath.evaluate("//return/message", doc);
            String montant = xpath.evaluate("//return/montant", doc);

            if ("0".equals(error)) {
                logger.info("Solde récupéré avec succès: {}", montant);
                return Mono.just(ResponseEntity.ok(
                    new SoldeResponse(
                        "success",
                        message,
                        montant,
                        List.of(new ServiceStatus("i-pay", true, "Solde récupéré"))
                )));
            } else {
                logger.warn("Erreur IPay: {}", message);
                return Mono.just(ResponseEntity.badRequest().body(
                    new SoldeResponse(
                        "error",
                        message,
                        "0.00",
                        List.of(new ServiceStatus("i-pay", false, message)))
                ));
            }
        } catch (Exception e) {
            logger.error("Erreur de traitement XML", e);
            return buildErrorResponse("Erreur de traitement de la réponse");
        }
    }

    private Mono<ResponseEntity<SoldeResponse>> handleError(Throwable e) {
        logger.error("Erreur lors de l'appel IPay", e);
        return buildErrorResponse("Erreur du service IPay");
    }

    private <T> Mono<ResponseEntity<T>> buildUnauthorizedResponse(T response) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
    }

    private Mono<ResponseEntity<SoldeResponse>> buildErrorResponse(String message) {
        return Mono.just(ResponseEntity.internalServerError().body(
            new SoldeResponse(
                "error",
                message,
                "0.00",
                List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
            )
        ));
    }
}
