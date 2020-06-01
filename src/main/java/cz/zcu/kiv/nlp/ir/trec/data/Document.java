package cz.zcu.kiv.nlp.ir.trec.data;

import java.util.Date;

/**
 * Created by Tigi on 8.1.2015.
 *
 * Rozhraní reprezentuje dokument, který je možné indexovat a vyhledávat.
 *
 * Implementujte toto rozhranní.
 *
 * Pokud potřebujete můžete do rozhranní přidat metody, ale signaturu stávajících metod neměnte.
 *
 */
public interface Document {

    String getText();

    String getId();

    String getTitle();

    Date getDate();

    void setId(String id);

    void setText(String text);

    void setTitle(String title);

}
