//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.06 at 05:41:42 PM CEST 
//


package gov.nih.nlm.ncbi.eutils.generated.esearch;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Count"/>
 *         &lt;element ref="{}RetMax"/>
 *         &lt;element ref="{}RetStart"/>
 *         &lt;element ref="{}IdList"/>
 *         &lt;element ref="{}TranslationSet"/>
 *         &lt;element ref="{}TranslationStack"/>
 *         &lt;element ref="{}QueryTranslation"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "count",
    "retMax",
    "retStart",
    "idList",
    "translationSet",
    "translationStack",
    "queryTranslation"
})
@XmlRootElement(name = "eSearchResult")
public class ESearchResult {

    @XmlElement(name = "Count", required = true)
    protected BigInteger count;
    @XmlElement(name = "RetMax", required = true)
    protected BigInteger retMax;
    @XmlElement(name = "RetStart", required = true)
    protected BigInteger retStart;
    @XmlElement(name = "IdList", required = true)
    protected IdList idList;
    @XmlElement(name = "TranslationSet", required = true)
    protected TranslationSet translationSet;
    @XmlElement(name = "TranslationStack", required = true)
    protected TranslationStack translationStack;
    @XmlElement(name = "QueryTranslation", required = true)
    protected String queryTranslation;

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCount(BigInteger value) {
        this.count = value;
    }

    /**
     * Gets the value of the retMax property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRetMax() {
        return retMax;
    }

    /**
     * Sets the value of the retMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRetMax(BigInteger value) {
        this.retMax = value;
    }

    /**
     * Gets the value of the retStart property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRetStart() {
        return retStart;
    }

    /**
     * Sets the value of the retStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRetStart(BigInteger value) {
        this.retStart = value;
    }

    /**
     * Gets the value of the idList property.
     * 
     * @return
     *     possible object is
     *     {@link IdList }
     *     
     */
    public IdList getIdList() {
        return idList;
    }

    /**
     * Sets the value of the idList property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdList }
     *     
     */
    public void setIdList(IdList value) {
        this.idList = value;
    }

    /**
     * Gets the value of the translationSet property.
     * 
     * @return
     *     possible object is
     *     {@link TranslationSet }
     *     
     */
    public TranslationSet getTranslationSet() {
        return translationSet;
    }

    /**
     * Sets the value of the translationSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link TranslationSet }
     *     
     */
    public void setTranslationSet(TranslationSet value) {
        this.translationSet = value;
    }

    /**
     * Gets the value of the translationStack property.
     * 
     * @return
     *     possible object is
     *     {@link TranslationStack }
     *     
     */
    public TranslationStack getTranslationStack() {
        return translationStack;
    }

    /**
     * Sets the value of the translationStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link TranslationStack }
     *     
     */
    public void setTranslationStack(TranslationStack value) {
        this.translationStack = value;
    }

    /**
     * Gets the value of the queryTranslation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryTranslation() {
        return queryTranslation;
    }

    /**
     * Sets the value of the queryTranslation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryTranslation(String value) {
        this.queryTranslation = value;
    }

}