@XmlSchema(
        namespace = "http://runtime.services.cash.innov.sn/",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
                @XmlNs(prefix = "run", namespaceURI = "http://runtime.services.cash.innov.sn/"),
                @XmlNs(prefix = "soap", namespaceURI = "http://schemas.xmlsoap.org/soap/envelope/")
        }
)
package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
