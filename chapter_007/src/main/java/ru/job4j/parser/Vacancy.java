package ru.job4j.parser;
/**
 * Vacancy.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class Vacancy {
    /**
     * Field - stores value of name's vacancy.
     */
    private String name;
    /**
     * Field - stores value of text's vacancy.
     */
    private String text;
    /**
     * Field - stores value of hyperlink's vacancy .
     */
    private String link;
    /**
     * Field - stores date of create.
     */
    private String date;
    /**
     * Constructor for activation fields.
     */
    public Vacancy(String name, String text, String link, String date ) {
        this.name = name;
        this.text = text;
        this.link = link;
        this.date = date;
    }
    /*
     * The method returns  name of vacancy.
     * @return name -string of  field name.
     */
    public String getName () {
        return this.name;
    }
    /*
     * The method returns  text of vacancy.
     * @return text -string of  field text.
     */
    public String getText() {
        return this.text;
    }
    /*
     * The method returns hyperlink  of vacancy.
     * @return link -string of  field  hypelink.
     */
    public String getLink() {
        return this.link;
    }
    /*
     * The method returns created date  of vacancy.
     * @return date -string of  field  date.
     */
    public String getDate() {
        return date;
    }
    @Override
    public String toString() {
        return "Vacancy{" +
                "name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", link='" + link + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
